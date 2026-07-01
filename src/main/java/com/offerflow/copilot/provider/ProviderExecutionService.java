package com.offerflow.copilot.provider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.offerflow.copilot.domain.ProviderTraceCenter;
import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.persistence.entity.ProviderTraceRunEntity;
import com.offerflow.copilot.persistence.entity.TraceStepEntity;
import com.offerflow.copilot.persistence.repository.ProviderTraceRunRepository;
import com.offerflow.copilot.persistence.repository.TraceStepRepository;
import com.offerflow.copilot.provider.contract.PromptContract;
import com.offerflow.copilot.provider.contract.PromptContractRegistry;
import com.offerflow.copilot.provider.contract.ProviderContractViolation;
import com.offerflow.copilot.provider.contract.ProviderResponseSchema;
import com.offerflow.copilot.provider.contract.ProviderResponseSchemaRegistry;
import com.offerflow.copilot.provider.contract.ProviderResponseValidator;
import com.offerflow.copilot.provider.contract.ProviderTaskType;
import com.offerflow.copilot.provider.contract.ProviderValidatedResult;
import com.offerflow.copilot.provider.contract.RiskPolicy;
import com.offerflow.copilot.provider.contract.RiskPolicyRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderExecutionService {

    private final AiProviderProperties properties;
    private final ProviderRouter providerRouter;
    private final PromptContractRegistry promptContractRegistry;
    private final RiskPolicyRegistry riskPolicyRegistry;
    private final ProviderResponseSchemaRegistry responseSchemaRegistry;
    private final ProviderResponseValidator responseValidator;
    private final ProviderTraceRunRepository providerTraceRunRepository;
    private final TraceStepRepository traceStepRepository;
    private final JsonCodec jsonCodec;

    public ProviderExecutionService(
            AiProviderProperties properties,
            ProviderRouter providerRouter,
            PromptContractRegistry promptContractRegistry,
            RiskPolicyRegistry riskPolicyRegistry,
            ProviderResponseSchemaRegistry responseSchemaRegistry,
            ProviderResponseValidator responseValidator,
            ProviderTraceRunRepository providerTraceRunRepository,
            TraceStepRepository traceStepRepository,
            JsonCodec jsonCodec) {
        this.properties = properties;
        this.providerRouter = providerRouter;
        this.promptContractRegistry = promptContractRegistry;
        this.riskPolicyRegistry = riskPolicyRegistry;
        this.responseSchemaRegistry = responseSchemaRegistry;
        this.responseValidator = responseValidator;
        this.providerTraceRunRepository = providerTraceRunRepository;
        this.traceStepRepository = traceStepRepository;
        this.jsonCodec = jsonCodec;
    }

    @Transactional
    public ProviderResponse sandboxRun(ProviderSandboxRunRequest body) {
        String runId = runId();
        String providerMode = providerRouter.normalize(valueOr(body == null ? null : body.providerMode(), properties.providerMode()));
        String inputText = valueOr(body == null ? null : body.inputText(), "Sandbox provider resilience check.");
        ProviderTaskType taskType = ProviderTaskType.fromOrDefault(body == null ? null : body.taskType());
        PromptContract contract = promptContractRegistry.get(taskType);
        RiskPolicy riskPolicy = riskPolicyRegistry.get(taskType);
        ProviderResponseSchema responseSchema = responseSchemaRegistry.get(taskType);
        ProviderRequest request = new ProviderRequest(
                runId,
                providerMode,
                taskType.apiName(),
                contract.promptVersion(),
                contract.schemaVersion(),
                inputText,
                List.of("sandbox-input", "provider-config"),
                riskPolicy.riskPolicyVersion(),
                properties.timeoutMs(),
                Map.of(
                        "simulateFailure", String.valueOf(body != null && body.simulateFailure()),
                        "simulateTimeout", String.valueOf(body != null && body.simulateTimeout()),
                        "actor", valueOr(body == null ? null : body.actor(), "provider-sandbox"),
                        "actorRole", valueOr(body == null ? null : body.actorRole(), "System"),
                        "promptVersion", contract.promptVersion(),
                        "schemaVersion", contract.schemaVersion(),
                        "riskPolicyVersion", riskPolicy.riskPolicyVersion()));

        long started = System.nanoTime();
        ProviderResponse routedResponse = providerRouter.analyze(request);
        int durationMs = Math.max(routedResponse.durationMs(), (int) ((System.nanoTime() - started) / 1_000_000));
        ProviderResponse candidateResponse = new ProviderResponse(
                routedResponse.success(),
                routedResponse.providerMode(),
                routedResponse.finalProvider(),
                routedResponse.model(),
                routedResponse.outputText(),
                routedResponse.structuredJson(),
                routedResponse.fallbackUsed(),
                routedResponse.fallbackReason(),
                routedResponse.errorCode(),
                routedResponse.errorMessage(),
                durationMs,
                runId,
                riskFlags(inputText, routedResponse),
                false,
                true);
        ProviderValidatedResult validatedResult = responseValidator.validate(
                taskType,
                candidateResponse,
                contract,
                riskPolicy,
                responseSchema);
        ProviderResponse response = responseWithValidation(candidateResponse, validatedResult);

        persistTrace(request, response, contract, riskPolicy, responseSchema, validatedResult);
        return response;
    }

    private ProviderResponse responseWithValidation(ProviderResponse response, ProviderValidatedResult validatedResult) {
        if (validatedResult.valid()) {
            return new ProviderResponse(
                    response.success(),
                    response.providerMode(),
                    response.finalProvider(),
                    response.model(),
                    response.outputText(),
                    response.structuredJson(),
                    response.fallbackUsed(),
                    response.fallbackReason(),
                    response.errorCode(),
                    response.errorMessage(),
                    response.durationMs(),
                    response.traceId(),
                    validatedResult.riskFlags(),
                    false,
                    true);
        }
        String validationReason = "Provider contract validation failed: " + violationCodes(validatedResult);
        String fallbackReason = response.fallbackReason().isBlank()
                ? validationReason
                : response.fallbackReason() + " " + validationReason;
        return new ProviderResponse(
                false,
                response.providerMode(),
                "local-rule",
                response.model(),
                validatedResult.sanitizedOutput(),
                response.structuredJson(),
                true,
                fallbackReason,
                "contract_violation",
                validationReason,
                response.durationMs(),
                response.traceId(),
                validatedResult.riskFlags(),
                false,
                true);
    }

    private void persistTrace(
            ProviderRequest request,
            ProviderResponse response,
            PromptContract contract,
            RiskPolicy riskPolicy,
            ProviderResponseSchema responseSchema,
            ProviderValidatedResult validatedResult) {
        LocalDateTime now = LocalDateTime.now();
        ProviderTraceRunEntity run = new ProviderTraceRunEntity();
        run.setId("provider-run-" + UUID.randomUUID());
        run.setRunId(response.traceId());
        run.setJobTitle(labelFor(request.taskType()));
        run.setProviderMode(response.providerMode());
        run.setFinalProvider(response.finalProvider());
        run.setModel(response.model());
        run.setFallbackReason(response.fallbackReason().isBlank() ? "local-rule selected; no fallback required." : response.fallbackReason());
        run.setPromptVersion(request.promptVersion());
        run.setSchemaVersion(request.schemaVersion());
        run.setRiskFlagsJson(jsonCodec.write(response.riskFlags()));
        run.setEvidenceCount(request.evidenceRefs().size());
        run.setHumanReviewStatus("Required");
        run.setDurationMs(response.durationMs());
        run.setTraceHash(traceHash(response));
        run.setEvidenceDetailJson(jsonCodec.write(evidenceDetail(request, response)));
        run.setTechnicalTagsJson(jsonCodec.write(List.of(
                "Provider SPI",
                "Prompt Contract",
                "Response Validator",
                "Sandbox Run",
                "No external model call",
                "local-rule",
                "fallback",
                "Trace Evidence",
                "Schema Contract Validate",
                "Risk Policy Guard",
                "Human Review")));
        run.setCreatedAt(now);
        providerTraceRunRepository.save(run);

        List<TraceStepEntity> steps = traceSteps(request, response, contract, riskPolicy, responseSchema, validatedResult, now);
        for (TraceStepEntity step : steps) {
            traceStepRepository.save(step);
        }
    }

    private List<TraceStepEntity> traceSteps(
            ProviderRequest request,
            ProviderResponse response,
            PromptContract contract,
            RiskPolicy riskPolicy,
            ProviderResponseSchema responseSchema,
            ProviderValidatedResult validatedResult,
            LocalDateTime now) {
        String callLabel = response.fallbackUsed() ? "Provider No-op" : "Provider Call";
        String fallbackSummary = response.fallbackUsed()
                ? "Fallback to local-rule: " + response.fallbackReason()
                : "local-rule selected; no fallback required.";
        String validateStatus = validatedResult.valid() ? "SUCCESS" : "ERROR";
        String riskStatus = validatedResult.valid() ? "WARNING" : "BLOCKED";
        return List.of(
                step(response.traceId(), 1, "provider-config-check", "Provider Config Check", "SUCCESS", 12,
                        "Read Provider SPI config without API key exposure.",
                        "realCallEnabled=" + properties.realCallEnabled() + ", rawResponseSave=" + properties.rawResponseSave(),
                        List.of("config-check"), now),
                step(response.traceId(), 2, "prompt-contract-load", "Prompt Contract Load", "SUCCESS", 10,
                        "Load contract for taskType=" + request.taskType(),
                        contract.promptVersion() + " / " + contract.schemaVersion(),
                        List.of("prompt-contract:" + contract.promptVersion()), now),
                step(response.traceId(), 3, "risk-policy-load", "Risk Policy Load", "SUCCESS", 8,
                        "Load risk policy " + riskPolicy.riskPolicyVersion(),
                        "forbiddenClaims=" + riskPolicy.forbiddenClaims().size() + ", humanReviewRequired=" + riskPolicy.requireHumanReview(),
                        List.of("risk-policy:" + riskPolicy.riskPolicyVersion()), now),
                step(response.traceId(), 4, "prompt-build", "Prompt Build", "SUCCESS", 18,
                        "Build sandbox prompt " + request.promptVersion(),
                        "Prompt payload kept local and review-gated.",
                        List.of("prompt-version:" + request.promptVersion()), now),
                step(response.traceId(), 5, "provider-select", "Provider Select", "SUCCESS", 9,
                        "Requested provider=" + request.providerMode(),
                        "Selected provider=" + response.providerMode(),
                        List.of("provider:" + response.providerMode()), now),
                step(response.traceId(), 6, "provider-noop", callLabel, response.fallbackUsed() ? "FALLBACK" : "SUCCESS", 0,
                        "External adapter network calls are disabled in P4B.",
                        response.fallbackUsed() ? "No external request sent." : "Local rule executed without network.",
                        List.of("no-network"), now),
                step(response.traceId(), 7, "fallback-decision", "Fallback Decision", response.fallbackUsed() ? "FALLBACK" : "SUCCESS", 7,
                        "Evaluate selected provider result.",
                        fallbackSummary,
                        List.of("fallback-policy"), now),
                step(response.traceId(), 8, "provider-response-validate", "Provider Response Validate", validateStatus, 14,
                        "Run ProviderResponseValidator.",
                        validatedResult.valid() ? "ProviderResponse passed contract validation." : violationSummary(validatedResult),
                        List.of("validator:" + responseSchema.schemaName()), now),
                step(response.traceId(), 9, "schema-contract-validate", "Schema Contract Validate", validateStatus, 11,
                        "Validate ProviderResponse against " + request.schemaVersion(),
                        "Required fields: " + String.join(" / ", responseSchema.requiredFields()),
                        List.of("schema:" + request.schemaVersion()), now),
                step(response.traceId(), 10, "risk-policy-guard", "Risk Policy Guard", riskStatus, 15,
                        "Scan sandbox input and output for forbidden claims.",
                        "Risk flags: " + String.join(" / ", response.riskFlags()),
                        List.of("risk-policy:" + request.riskPolicy()), now),
                step(response.traceId(), 11, "contract-violation-check", "Contract Violation Check", validatedResult.valid() ? "SUCCESS" : "BLOCKED", 6,
                        "Check contract violations before exposing result.",
                        validatedResult.valid() ? "No contract violations." : violationSummary(validatedResult),
                        List.of("contract-violations"), now),
                step(response.traceId(), 12, "human-review-required", "Human Review Required", "WARNING", 5,
                        "All outputs remain Draft before human confirmation.",
                        "humanReviewRequired=true; copy remains gated.",
                        List.of("human-review"), now));
    }

    private TraceStepEntity step(
            String runId,
            int order,
            String key,
            String name,
            String status,
            int durationMs,
            String inputSummary,
            String outputSummary,
            List<String> evidenceRefs,
            LocalDateTime createdAt) {
        TraceStepEntity entity = new TraceStepEntity();
        entity.setId("provider-step-" + UUID.randomUUID());
        entity.setRunId(runId);
        entity.setStepOrder(order);
        entity.setStepKey(key);
        entity.setStepName(name);
        entity.setStatus(status);
        entity.setDurationMs(durationMs);
        entity.setInputSummary(inputSummary);
        entity.setOutputSummary(outputSummary);
        entity.setEvidenceRefsJson(jsonCodec.write(evidenceRefs));
        entity.setCreatedAt(createdAt);
        return entity;
    }

    private List<String> riskFlags(String inputText, ProviderResponse response) {
        java.util.LinkedHashSet<String> flags = new java.util.LinkedHashSet<>(response.riskFlags());
        flags.add("human-review-required");
        flags.add("raw-response-not-saved");
        if (containsAny(inputText, "自动投递", "Offer 概率", "录取概率", "保证通过", "实时面试")) {
            flags.add("forbidden-claim-detected");
        }
        return List.copyOf(flags);
    }

    private ProviderTraceCenter.TraceEvidenceDetail evidenceDetail(ProviderRequest request, ProviderResponse response) {
        return new ProviderTraceCenter.TraceEvidenceDetail(
                summarize(request.inputText()),
                List.of(
                        new ProviderTraceCenter.ResumeEvidenceRef(
                                "SANDBOX-001",
                                "Provider SPI sandbox",
                                "Records provider config checks, fallback decisions, and human-review gating.",
                                List.of("provider_trace_run", "trace_step")),
                        new ProviderTraceCenter.ResumeEvidenceRef(
                                "SANDBOX-002",
                                "No-op external adapters",
                                "OpenAI-compatible and DeepSeek adapters exist but do not perform network calls in P4C.",
                                List.of("ProviderRouter", "NoOp clients")),
                        new ProviderTraceCenter.ResumeEvidenceRef(
                                "SANDBOX-003",
                                "Provider contract validation",
                                "PromptContract, RiskPolicy, and ProviderResponseSchema are loaded before response exposure.",
                                List.of("PromptContractRegistry", "ProviderResponseValidator"))),
                response.structuredJson(),
                response.fallbackReason().isBlank() ? "No fallback: local-rule was selected." : response.fallbackReason(),
                "Sandbox output is only a local demo artifact and must be reviewed by a human.");
    }

    private String traceHash(ProviderResponse response) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((response.traceId() + ":" + response.finalProvider() + ":" + response.fallbackReason())
                    .getBytes(StandardCharsets.UTF_8));
            return "trace-" + HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private String runId() {
        return "P4C-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String labelFor(String taskType) {
        return "Provider Sandbox: " + taskType;
    }

    private String summarize(String inputText) {
        String trimmed = valueOr(inputText, "Sandbox provider run").replaceAll("\\s+", " ").trim();
        return trimmed.length() > 160 ? trimmed.substring(0, 157) + "..." : trimmed;
    }

    private boolean containsAny(String text, String... needles) {
        if (text == null) {
            return false;
        }
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String violationCodes(ProviderValidatedResult validatedResult) {
        return validatedResult.violations().stream()
                .map(ProviderContractViolation::code)
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse("unknown_contract_violation");
    }

    private String violationSummary(ProviderValidatedResult validatedResult) {
        return validatedResult.violations().stream()
                .map(violation -> violation.code() + ":" + violation.field())
                .distinct()
                .reduce((left, right) -> left + " / " + right)
                .orElse("No contract violations.");
    }
}
