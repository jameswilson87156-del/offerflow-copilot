package com.offerflow.copilot.provider;

import java.util.List;
import java.util.Map;

public record ProviderRequest(
        String runId,
        String providerMode,
        String taskType,
        String promptVersion,
        String schemaVersion,
        String inputText,
        List<String> evidenceRefs,
        String riskPolicy,
        int timeoutMs,
        Map<String, String> metadata) {

    public ProviderRequest {
        runId = textOr(runId, "provider-run-local");
        providerMode = textOr(providerMode, "local-rule");
        taskType = textOr(taskType, "provider-sandbox");
        promptVersion = textOr(promptVersion, "provider-sandbox-prompt-v2");
        schemaVersion = textOr(schemaVersion, "provider-sandbox-v1");
        inputText = textOr(inputText, "");
        evidenceRefs = evidenceRefs == null ? List.of() : List.copyOf(evidenceRefs);
        riskPolicy = textOr(riskPolicy, "human-review-required");
        timeoutMs = timeoutMs > 0 ? timeoutMs : 8000;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean simulateFailure() {
        return Boolean.parseBoolean(metadata.getOrDefault("simulateFailure", "false"));
    }

    public boolean simulateTimeout() {
        return Boolean.parseBoolean(metadata.getOrDefault("simulateTimeout", "false"));
    }

    private static String textOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
