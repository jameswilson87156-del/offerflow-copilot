package com.offerflow.copilot.provider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class RealProviderDryRunService {

    public static final String BOUNDARY_NOTICE =
            "Manual real provider dry-run only; default disabled, no raw response is saved, all output requires Human Review and Copy Permission.";

    private static final String LOCAL_RULE = "local-rule";
    private static final String OPENAI_COMPATIBLE = "openai-compatible";
    private static final String DEEPSEEK = "deepseek";

    private final AiProviderProperties properties;
    private final PromptContractRegistry promptContractRegistry;
    private final RiskPolicyRegistry riskPolicyRegistry;
    private final ProviderResponseSchemaRegistry responseSchemaRegistry;
    private final ProviderResponseValidator responseValidator;
    private final LocalRuleProviderClient localRuleProviderClient;
    private final RealProviderGateway realProviderGateway;
    private final ProviderTraceRunRepository providerTraceRunRepository;
    private final TraceStepRepository traceStepRepository;
    private final JsonCodec jsonCodec;
    private final ObjectMapper objectMapper;
    private final PiiGuard piiGuard;

    public RealProviderDryRunService(
            AiProviderProperties properties,
            PromptContractRegistry promptContractRegistry,
            RiskPolicyRegistry riskPolicyRegistry,
            ProviderResponseSchemaRegistry responseSchemaRegistry,
            ProviderResponseValidator responseValidator,
            LocalRuleProviderClient localRuleProviderClient,
            RealProviderGateway realProviderGateway,
            ProviderTraceRunRepository providerTraceRunRepository,
            TraceStepRepository traceStepRepository,
            JsonCodec jsonCodec,
            ObjectMapper objectMapper,
            PiiGuard piiGuard) {
        this.properties = properties;
        this.promptContractRegistry = promptContractRegistry;
        this.riskPolicyRegistry = riskPolicyRegistry;
        this.responseSchemaRegistry = responseSchemaRegistry;
        this.responseValidator = responseValidator;
        this.localRuleProviderClient = localRuleProviderClient;
        this.realProviderGateway = realProviderGateway;
        this.providerTraceRunRepository = providerTraceRunRepository;
        this.traceStepRepository = traceStepRepository;
        this.jsonCodec = jsonCodec;
        this.objectMapper = objectMapper;
        this.piiGuard = piiGuard;
    }

    @Transactional
    public RealProviderDryRunResult dryRun(RealProviderDryRunRequest body) {
        RealProviderDryRunRequest requestBody = body == null
                ? new RealProviderDryRunRequest(null, null, null, null, null, false, false)
                : body;
        String runId = "P4F-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ProviderTaskType taskType = ProviderTaskType.fromOrDefault(requestBody.taskType());
        PromptContract contract = promptContractRegistry.get(taskType);
        RiskPolicy riskPolicy = riskPolicyRegistry.get(taskType);
        ProviderResponseSchema schema = responseSchemaRegistry.get(taskType);
        String providerMode = normalize(requestBody.providerMode());
        String inputText = valueOr(requestBody.inputText(), "Manual real provider dry-run with sanitized input.");

        PiiGuardResult piiResult = piiGuard.inspect(inputText);
        DryRunDecision preflight = preflight(requestBody, providerMode, piiResult);

        boolean externalAttempted = false;
        boolean externalBlocked = preflight.blocked();
        ProviderResponse response;
        ProviderValidatedResult validatedResult = null;
        String fallbackReason = preflight.reason();
        int durationMs = 0;
        String externalStepLabel = "External Call Blocked";
        String externalStepStatus = externalBlocked ? "BLOCKED" : "SUCCESS";
        String normalizeStatus = externalBlocked ? "BLOCKED" : "SUCCESS";
        String normalizeSummary = externalBlocked ? "No external response to normalize." : "External response normalized.";
        boolean success;

        if (externalBlocked) {
            response = fallbackResponse(runId, providerMode, taskType, contract, riskPolicy, inputText, fallbackReason);
            if (preflight.shouldValidateFallback()) {
                validatedResult = responseValidator.validate(taskType, response, contract, riskPolicy, schema);
            }
            success = preflight.successAfterFallback() && (validatedResult == null || validatedResult.valid());
        } else {
            externalAttempted = true;
            RealProviderCallResult callResult = realProviderGateway.call(callRequest(providerMode, inputText, contract, runId));
            durationMs = callResult.durationMs();
            if (callResult.success()) {
                response = externalResponse(
                        runId,
                        providerMode,
                        taskType,
                        contract,
                        schema,
                        callResult.normalizedText(),
                        durationMs);
                validatedResult = responseValidator.validate(taskType, response, contract, riskPolicy, schema);
                if (validatedResult.valid()) {
                    success = true;
                    fallbackReason = "";
                    externalStepLabel = "External Provider Call";
                    normalizeSummary = "External response normalized and raw response discarded.";
                } else {
                    fallbackReason = "External provider response failed validation: " + violationCodes(validatedResult);
                    response = fallbackResponse(runId, providerMode, taskType, contract, riskPolicy, inputText, fallbackReason);
                    success = false;
                    normalizeStatus = "BLOCKED";
                    normalizeSummary = "External response normalized, failed validation, and raw response was discarded.";
                }
            } else {
                fallbackReason = callResult.errorMessage();
                response = fallbackResponse(runId, providerMode, taskType, contract, riskPolicy, inputText, fallbackReason);
                validatedResult = responseValidator.validate(taskType, response, contract, riskPolicy, schema);
                success = validatedResult.valid();
                externalStepLabel = "External Provider Call";
                externalStepStatus = "ERROR";
                normalizeStatus = "FALLBACK";
                normalizeSummary = "No usable external response; fallback response normalized.";
            }
        }

        List<String> riskFlags = riskFlags(response, piiResult, validatedResult, externalAttempted, externalBlocked);
        boolean schemaValidated = schemaValidated(validatedResult);
        boolean riskGuardPassed = riskGuardPassed(validatedResult);
        RealProviderDryRunResult result = new RealProviderDryRunResult(
                success,
                externalAttempted,
                externalBlocked,
                providerMode,
                response.finalProvider(),
                response.model(),
                response.fallbackUsed(),
                valueOr(fallbackReason, response.fallbackReason()),
                schemaValidated,
                riskGuardPassed,
                true,
                false,
                false,
                runId,
                runId,
                riskFlags,
                BOUNDARY_NOTICE);

        persistTrace(
                result,
                inputText,
                taskType,
                contract,
                riskPolicy,
                schema,
                validatedResult,
                piiResult,
                preflight,
                externalStepLabel,
                externalStepStatus,
                normalizeStatus,
                normalizeSummary,
                Math.max(durationMs, response.durationMs()));
        return result;
    }

    private DryRunDecision preflight(RealProviderDryRunRequest request, String providerMode, PiiGuardResult piiResult) {
        if (!List.of(OPENAI_COMPATIBLE, DEEPSEEK).contains(providerMode)) {
            return DryRunDecision.blocked(
                    "Only deepseek and openai-compatible support manual real dry-run.",
                    true,
                    true,
                    request.confirmNoPii() && !piiResult.blocked());
        }
        if (!request.confirmNoPii()) {
            return DryRunDecision.blocked(
                    "confirmNoPii=false; manual dry-run requires explicit PII confirmation.",
                    false,
                    false,
                    false);
        }
        if (piiResult.blocked()) {
            return DryRunDecision.blocked(piiResult.reason(), false, false, false);
        }
        if (!request.allowExternalCall()) {
            return DryRunDecision.blocked(
                    "allowExternalCall=false; external provider call was not attempted.",
                    false,
                    false,
                    true);
        }
        if (!properties.realCallEnabled()) {
            return DryRunDecision.blocked("realCallEnabled=false; fallback to local-rule.", true, true, true);
        }
        if (!configured(providerMode)) {
            return DryRunDecision.blocked(
                    providerMode + " is not configured with base URL, API key, and model.",
                    true,
                    true,
                    true);
        }
        return DryRunDecision.allowed();
    }

    private ProviderResponse fallbackResponse(
            String runId,
            String providerMode,
            ProviderTaskType taskType,
            PromptContract contract,
            RiskPolicy riskPolicy,
            String inputText,
            String fallbackReason) {
        ProviderRequest request = new ProviderRequest(
                runId,
                providerMode,
                taskType.apiName(),
                contract.promptVersion(),
                contract.schemaVersion(),
                inputText,
                List.of("real-dry-run", "fallback"),
                riskPolicy.riskPolicyVersion(),
                properties.timeoutMs(),
                Map.of("dryRun", "true", "rawResponseSaved", "false"));
        ProviderResponse local = localRuleProviderClient.analyze(request);
        return new ProviderResponse(
                local.success(),
                providerMode,
                LOCAL_RULE,
                local.model(),
                local.outputText(),
                local.structuredJson(),
                true,
                fallbackReason,
                fallbackReason.isBlank() ? "" : "fallback_required",
                fallbackReason,
                local.durationMs(),
                runId,
                local.riskFlags(),
                false,
                true);
    }

    private ProviderResponse externalResponse(
            String runId,
            String providerMode,
            ProviderTaskType taskType,
            PromptContract contract,
            ProviderResponseSchema schema,
            String normalizedText,
            int durationMs) {
        String outputText = valueOr(normalizedText, "External provider returned an empty dry-run response.");
        return new ProviderResponse(
                true,
                providerMode,
                providerMode,
                model(providerMode),
                outputText,
                structuredJson(providerMode, schema, outputText),
                false,
                "",
                "",
                "",
                durationMs,
                runId,
                List.of("real-dry-run", "raw-response-not-saved", "human-review-required", taskType.apiName()),
                false,
                true);
    }

    private RealProviderCallRequest callRequest(String providerMode, String inputText, PromptContract contract, String runId) {
        AiProviderProperties.ExternalProvider provider = provider(providerMode);
        return new RealProviderCallRequest(
                providerMode,
                provider.getBaseUrl(),
                provider.getApiKey(),
                provider.getModel(),
                inputText,
                contract,
                properties.timeoutMs(),
                runId);
    }

    private String structuredJson(String providerMode, ProviderResponseSchema schema, String outputText) {
        try {
            objectMapper.readTree(outputText);
            return outputText;
        } catch (Exception ignored) {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("provider", providerMode);
            fields.put("schemaVersion", schema.schemaVersion());
            fields.put("summary", summarize(outputText));
            fields.put("humanReviewRequired", true);
            fields.put("copyAllowed", false);
            fields.put("boundaryNotice", "Manual real provider dry-run; raw response is not saved and output remains review-gated.");
            return jsonCodec.write(fields);
        }
    }

    private void persistTrace(
            RealProviderDryRunResult result,
            String inputText,
            ProviderTaskType taskType,
            PromptContract contract,
            RiskPolicy riskPolicy,
            ProviderResponseSchema schema,
            ProviderValidatedResult validatedResult,
            PiiGuardResult piiResult,
            DryRunDecision preflight,
            String externalStepLabel,
            String externalStepStatus,
            String normalizeStatus,
            String normalizeSummary,
            int durationMs) {
        LocalDateTime now = LocalDateTime.now();
        ProviderTraceRunEntity run = new ProviderTraceRunEntity();
        run.setId("provider-run-" + UUID.randomUUID());
        run.setRunId(result.runId());
        run.setJobTitle("Real Provider Dry-run: " + taskType.apiName());
        run.setProviderMode(result.providerMode());
        run.setFinalProvider(result.finalProvider());
        run.setModel(result.model());
        run.setFallbackReason(valueOr(result.fallbackReason(), "External dry-run passed validation; output still requires Human Review."));
        run.setPromptVersion(contract.promptVersion());
        run.setSchemaVersion(contract.schemaVersion());
        run.setRiskFlagsJson(jsonCodec.write(result.riskFlags()));
        run.setEvidenceCount(1);
        run.setHumanReviewStatus("Required");
        run.setDurationMs(Math.max(1, durationMs));
        run.setTraceHash(traceHash(result));
        run.setEvidenceDetailJson(jsonCodec.write(evidenceDetail(inputText, result)));
        run.setTechnicalTagsJson(jsonCodec.write(List.of(
                "Real Provider Dry-run",
                "Manual opt-in",
                "PII Guard",
                "No raw response saved",
                "Schema Validate",
                "Risk Guard",
                "Human Review",
                "Copy Permission Blocked",
                "Fallback local-rule")));
        run.setCreatedAt(now);
        providerTraceRunRepository.save(run);

        for (TraceStepEntity step : traceSteps(
                result,
                contract,
                riskPolicy,
                schema,
                validatedResult,
                piiResult,
                preflight,
                externalStepLabel,
                externalStepStatus,
                normalizeStatus,
                normalizeSummary,
                now)) {
            traceStepRepository.save(step);
        }
    }

    private List<TraceStepEntity> traceSteps(
            RealProviderDryRunResult result,
            PromptContract contract,
            RiskPolicy riskPolicy,
            ProviderResponseSchema schema,
            ProviderValidatedResult validatedResult,
            PiiGuardResult piiResult,
            DryRunDecision preflight,
            String externalStepLabel,
            String externalStepStatus,
            String normalizeStatus,
            String normalizeSummary,
            LocalDateTime now) {
        return List.of(
                step(result.runId(), 1, "actor-permission-check", "Actor Permission Check", "SUCCESS", 4,
                        "P4E Local Permission checked before service execution.", "OWNER/EDITOR dry-run permission required.", List.of("permission_audit_event"), now),
                step(result.runId(), 2, "real-call-flag-check", "Real Call Flag Check", properties.realCallEnabled() ? "SUCCESS" : "BLOCKED", 3,
                        "Read realCallEnabled flag.", "realCallEnabled=" + properties.realCallEnabled(), List.of("application.yml"), now),
                step(result.runId(), 3, "pii-guard", "PII Guard", piiResult.blocked() || !preflight.confirmedNoPii() ? "BLOCKED" : "SUCCESS", 6,
                        "Scan input for phone, email, ID card, API key, and sk- patterns.", piiResult.reason(), piiResult.riskFlags(), now),
                step(result.runId(), 4, "prompt-contract-load", "Prompt Contract Load", "SUCCESS", 5,
                        "Load contract for manual dry-run.", contract.promptVersion() + " / " + contract.schemaVersion(), List.of("prompt-contract"), now),
                step(result.runId(), 5, "risk-policy-load", "Risk Policy Load", "SUCCESS", 5,
                        "Load risk policy.", riskPolicy.riskPolicyVersion(), List.of("risk-policy"), now),
                step(result.runId(), 6, "provider-config-check", "Provider Config Check", configured(result.providerMode()) ? "SUCCESS" : "BLOCKED", 7,
                        "Check provider base URL, API key, and model without exposing secrets.", configSummary(result.providerMode()), List.of("config-check"), now),
                step(result.runId(), 7, "provider-select", "Provider Select", List.of(OPENAI_COMPATIBLE, DEEPSEEK).contains(result.providerMode()) ? "SUCCESS" : "BLOCKED", 4,
                        "Select external dry-run provider.", "providerMode=" + result.providerMode(), List.of("provider:" + result.providerMode()), now),
                step(result.runId(), 8, "external-provider-call", externalStepLabel, externalStepStatus, 1,
                        "Manual opt-in external call boundary.", result.externalCallAttempted() ? "External call attempted; key was not logged or returned." : "External call blocked before network.", List.of("manual-opt-in"), now),
                step(result.runId(), 9, "provider-response-normalize", "Provider Response Normalize", normalizeStatus, 8,
                        "Normalize provider response and discard raw response.", normalizeSummary, List.of("raw-response-not-saved"), now),
                step(result.runId(), 10, "schema-validate", "Schema Validate", result.schemaValidated() ? "SUCCESS" : "BLOCKED", 8,
                        "Validate normalized response against " + schema.schemaVersion(), validationSummary(validatedResult), List.of("schema:" + schema.schemaVersion()), now),
                step(result.runId(), 11, "risk-guard", "Risk Guard", result.riskGuardPassed() ? "SUCCESS" : "BLOCKED", 9,
                        "Scan normalized output for forbidden claims.", riskSummary(validatedResult, result), List.of("risk-policy:" + riskPolicy.riskPolicyVersion()), now),
                step(result.runId(), 12, "fallback-decision", "Fallback Decision", result.fallbackUsed() ? "FALLBACK" : "SUCCESS", 4,
                        "Choose external result or local-rule fallback.", result.fallbackUsed() ? result.fallbackReason() : "No fallback required.", List.of("fallback-policy"), now),
                step(result.runId(), 13, "human-review-required", "Human Review Required", "WARNING", 3,
                        "Dry-run output remains Draft.", "humanReviewRequired=true.", List.of("human-review"), now),
                step(result.runId(), 14, "copy-permission-blocked", "Copy Permission Blocked", "BLOCKED", 3,
                        "Dry-run output is not confirmed.", "copyAllowed=false until Human Review and Copy Permission Contract pass.", List.of("copy-permission"), now));
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

    private ProviderTraceCenter.TraceEvidenceDetail evidenceDetail(String inputText, RealProviderDryRunResult result) {
        return new ProviderTraceCenter.TraceEvidenceDetail(
                summarize(inputText),
                List.of(new ProviderTraceCenter.ResumeEvidenceRef(
                        "REAL-DRY-RUN",
                        "Manual real provider dry-run",
                        "Records manual opt-in, PII guard, provider config, fallback, validation, and review gating.",
                        List.of("provider_trace_run", "trace_step"))),
                "Raw provider response is not saved. Only normalized validation status and trace metadata are retained.",
                valueOr(result.fallbackReason(), "No fallback was required."),
                "Dry-run output requires Human Review and cannot be copied before Copy Permission Contract passes.");
    }

    private boolean schemaValidated(ProviderValidatedResult validatedResult) {
        if (validatedResult == null) {
            return false;
        }
        return validatedResult.violations().stream().noneMatch(violation ->
                violation.code().contains("schema")
                        || violation.code().contains("structured_json")
                        || violation.code().contains("required_field"));
    }

    private boolean riskGuardPassed(ProviderValidatedResult validatedResult) {
        if (validatedResult == null) {
            return false;
        }
        return validatedResult.violations().stream().noneMatch(violation ->
                violation.code().contains("forbidden_term")
                        || violation.code().contains("forbidden_claim"));
    }

    private String validationSummary(ProviderValidatedResult validatedResult) {
        if (validatedResult == null) {
            return "Validation skipped because the external call was blocked before response normalization.";
        }
        if (validatedResult.valid()) {
            return "Normalized response passed schema validation.";
        }
        return "Validation violations: " + violationCodes(validatedResult);
    }

    private String riskSummary(ProviderValidatedResult validatedResult, RealProviderDryRunResult result) {
        if (validatedResult == null) {
            return "Risk Guard blocked or skipped before output exposure.";
        }
        return "Risk flags: " + String.join(" / ", result.riskFlags());
    }

    private List<String> riskFlags(
            ProviderResponse response,
            PiiGuardResult piiResult,
            ProviderValidatedResult validatedResult,
            boolean externalAttempted,
            boolean externalBlocked) {
        LinkedHashSet<String> flags = new LinkedHashSet<>();
        flags.add("real-dry-run");
        flags.add("raw-response-not-saved");
        flags.add("human-review-required");
        flags.add("copy-permission-blocked");
        flags.add(externalAttempted ? "external-call-attempted" : "external-call-not-attempted");
        if (externalBlocked) {
            flags.add("external-call-blocked");
        }
        flags.addAll(piiResult.riskFlags());
        flags.addAll(response.riskFlags());
        if (validatedResult != null) {
            flags.addAll(validatedResult.riskFlags());
        }
        return List.copyOf(flags);
    }

    private boolean configured(String providerMode) {
        return properties.configured(provider(providerMode));
    }

    private String configSummary(String providerMode) {
        AiProviderProperties.ExternalProvider provider = provider(providerMode);
        return "baseUrlConfigured=" + hasText(provider.getBaseUrl())
                + ", apiKeyStatus=" + properties.apiKeyStatus(provider)
                + ", modelConfigured=" + hasText(provider.getModel())
                + ", rawResponseSave=" + properties.rawResponseSave();
    }

    private AiProviderProperties.ExternalProvider provider(String providerMode) {
        return DEEPSEEK.equals(providerMode) ? properties.getDeepseek() : properties.getOpenaiCompatible();
    }

    private String model(String providerMode) {
        return provider(providerMode).getModel();
    }

    private String normalize(String providerMode) {
        String mode = valueOr(providerMode, properties.providerMode()).trim().toLowerCase(java.util.Locale.ROOT);
        return mode.replace('_', '-');
    }

    private String traceHash(RealProviderDryRunResult result) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((result.runId() + ":" + result.providerMode() + ":" + result.fallbackReason())
                    .getBytes(StandardCharsets.UTF_8));
            return "trace-" + HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private String violationCodes(ProviderValidatedResult validatedResult) {
        return validatedResult.violations().stream()
                .map(ProviderContractViolation::code)
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse("none");
    }

    private String summarize(String value) {
        String text = valueOr(value, "").replaceAll("\\s+", " ").trim();
        return text.length() > 160 ? text.substring(0, 157) + "..." : text;
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record DryRunDecision(
            boolean blocked,
            String reason,
            boolean shouldValidateFallback,
            boolean successAfterFallback,
            boolean confirmedNoPii) {

        static DryRunDecision allowed() {
            return new DryRunDecision(false, "", false, false, true);
        }

        static DryRunDecision blocked(
                String reason,
                boolean shouldValidateFallback,
                boolean successAfterFallback,
                boolean confirmedNoPii) {
            return new DryRunDecision(true, reason, shouldValidateFallback, successAfterFallback, confirmedNoPii);
        }
    }
}
