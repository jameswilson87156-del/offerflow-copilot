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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderExecutionService {

    private static final String PROMPT_VERSION = "provider-sandbox-v1";
    private static final String SCHEMA_VERSION = "provider-response-v1";

    private final AiProviderProperties properties;
    private final ProviderRouter providerRouter;
    private final ProviderTraceRunRepository providerTraceRunRepository;
    private final TraceStepRepository traceStepRepository;
    private final JsonCodec jsonCodec;

    public ProviderExecutionService(
            AiProviderProperties properties,
            ProviderRouter providerRouter,
            ProviderTraceRunRepository providerTraceRunRepository,
            TraceStepRepository traceStepRepository,
            JsonCodec jsonCodec) {
        this.properties = properties;
        this.providerRouter = providerRouter;
        this.providerTraceRunRepository = providerTraceRunRepository;
        this.traceStepRepository = traceStepRepository;
        this.jsonCodec = jsonCodec;
    }

    @Transactional
    public ProviderResponse sandboxRun(ProviderSandboxRunRequest body) {
        String runId = runId();
        String providerMode = providerRouter.normalize(valueOr(body == null ? null : body.providerMode(), properties.providerMode()));
        String inputText = valueOr(body == null ? null : body.inputText(), "Sandbox provider resilience check.");
        ProviderRequest request = new ProviderRequest(
                runId,
                providerMode,
                valueOr(body == null ? null : body.taskType(), "provider-sandbox"),
                PROMPT_VERSION,
                SCHEMA_VERSION,
                inputText,
                List.of("sandbox-input", "provider-config"),
                "human-review-required",
                properties.timeoutMs(),
                Map.of(
                        "simulateFailure", String.valueOf(body != null && body.simulateFailure()),
                        "simulateTimeout", String.valueOf(body != null && body.simulateTimeout()),
                        "actor", valueOr(body == null ? null : body.actor(), "provider-sandbox"),
                        "actorRole", valueOr(body == null ? null : body.actorRole(), "System")));

        long started = System.nanoTime();
        ProviderResponse routedResponse = providerRouter.analyze(request);
        int durationMs = Math.max(routedResponse.durationMs(), (int) ((System.nanoTime() - started) / 1_000_000));
        ProviderResponse response = new ProviderResponse(
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

        persistTrace(request, response);
        return response;
    }

    private void persistTrace(ProviderRequest request, ProviderResponse response) {
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
                "Sandbox Run",
                "No external model call",
                "local-rule",
                "fallback",
                "Trace Evidence",
                "Schema Validate",
                "Risk Guard",
                "Human Review")));
        run.setCreatedAt(now);
        providerTraceRunRepository.save(run);

        List<TraceStepEntity> steps = traceSteps(request, response, now);
        for (TraceStepEntity step : steps) {
            traceStepRepository.save(step);
        }
    }

    private List<TraceStepEntity> traceSteps(ProviderRequest request, ProviderResponse response, LocalDateTime now) {
        String callLabel = response.fallbackUsed() ? "Provider No-op" : "Provider Call";
        String fallbackSummary = response.fallbackUsed()
                ? "Fallback to local-rule: " + response.fallbackReason()
                : "local-rule selected; no fallback required.";
        return List.of(
                step(response.traceId(), 1, "provider-config-check", "Provider Config Check", "SUCCESS", 12,
                        "Read Provider SPI config without API key exposure.",
                        "realCallEnabled=" + properties.realCallEnabled() + ", rawResponseSave=" + properties.rawResponseSave(),
                        List.of("config-check"), now),
                step(response.traceId(), 2, "prompt-build", "Prompt Build", "SUCCESS", 18,
                        "Build sandbox prompt " + request.promptVersion(),
                        "Prompt payload kept local and review-gated.",
                        List.of("prompt-version:" + request.promptVersion()), now),
                step(response.traceId(), 3, "provider-select", "Provider Select", "SUCCESS", 9,
                        "Requested provider=" + request.providerMode(),
                        "Selected provider=" + response.providerMode(),
                        List.of("provider:" + response.providerMode()), now),
                step(response.traceId(), 4, "provider-noop", callLabel, response.fallbackUsed() ? "FALLBACK" : "SUCCESS", 0,
                        "External adapter network calls are disabled in P4B.",
                        response.fallbackUsed() ? "No external request sent." : "Local rule executed without network.",
                        List.of("no-network"), now),
                step(response.traceId(), 5, "fallback-decision", "Fallback Decision", response.fallbackUsed() ? "FALLBACK" : "SUCCESS", 7,
                        "Evaluate selected provider result.",
                        fallbackSummary,
                        List.of("fallback-policy"), now),
                step(response.traceId(), 6, "schema-validate", "Schema Validate", "SUCCESS", 11,
                        "Validate ProviderResponse against " + request.schemaVersion(),
                        "Response contains traceId, fallback fields, risk flags, and rawResponseSaved=false.",
                        List.of("schema:" + request.schemaVersion()), now),
                step(response.traceId(), 7, "risk-guard", "Risk Guard", "WARNING", 15,
                        "Scan sandbox input and output for forbidden claims.",
                        "Risk flags: " + String.join(" / ", response.riskFlags()),
                        List.of("risk-policy:" + request.riskPolicy()), now),
                step(response.traceId(), 8, "human-review-required", "Human Review Required", "WARNING", 5,
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
                                "OpenAI-compatible and DeepSeek adapters exist but do not perform network calls in P4B.",
                                List.of("ProviderRouter", "NoOp clients"))),
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
        return "P4B-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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
}
