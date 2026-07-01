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
                "P4B uses local-rule/no-op providers only; no real external model calls are made.");
    }
}
