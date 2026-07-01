package com.offerflow.copilot.provider;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class LocalRuleProviderClient implements AiProviderClient {

    private static final String MODE = "local-rule";
    private static final String MODEL = "local-rule-engine v2.1";

    private final AiProviderProperties properties;

    public LocalRuleProviderClient(AiProviderProperties properties) {
        this.properties = properties;
    }

    @Override
    public ProviderResponse analyze(ProviderRequest request) {
        int durationMs = Math.max(18, Math.min(240, request.inputText().length() + 24));
        return new ProviderResponse(
                true,
                request.providerMode(),
                MODE,
                MODEL,
                "Local-rule sandbox output. No external model call was made; every result requires Human Review.",
                structuredJson(request),
                false,
                "",
                "",
                "",
                durationMs,
                request.runId(),
                List.of("human-review-required", "no-external-model-call"),
                false,
                true);
    }

    @Override
    public ProviderHealth health() {
        return new ProviderHealth(
                MODE,
                "AVAILABLE",
                true,
                false,
                LocalDateTime.now(),
                "local-rule is deterministic and does not call external services.");
    }

    @Override
    public ProviderDescriptor descriptor() {
        return new ProviderDescriptor(
                MODE,
                "local-rule",
                true,
                true,
                "disabled",
                MODEL,
                properties.timeoutMs(),
                MODE.equals(properties.providerMode()),
                false,
                false,
                "primary local fallback",
                "Default deterministic provider; no external network calls.");
    }

    private String structuredJson(ProviderRequest request) {
        return "{\"provider\":\"local-rule\","
                + "\"schemaVersion\":\"" + escape(request.schemaVersion()) + "\","
                + "\"summary\":\"Local-rule sandbox output requires Human Review.\","
                + "\"humanReviewRequired\":true,"
                + "\"copyAllowed\":false,"
                + "\"boundaryNotice\":\"No external model call was made.\"}";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
