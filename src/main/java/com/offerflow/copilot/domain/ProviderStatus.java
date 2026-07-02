package com.offerflow.copilot.domain;

public record ProviderStatus(
        String mode,
        boolean openaiCompatibleReady,
        boolean deepSeekReady,
        boolean realCallEnabled,
        boolean rawResponseSave,
        String fallback,
        String boundaryNotice) {

    public static ProviderStatus localRule() {
        return new ProviderStatus(
                "local-rule",
                false,
                false,
                false,
                false,
                "local-rule",
                "Default mode uses local-rule/no-op providers; P4F real dry-run requires manual opt-in and remains review-gated.");
    }
}
