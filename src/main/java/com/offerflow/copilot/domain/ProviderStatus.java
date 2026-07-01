package com.offerflow.copilot.domain;

public record ProviderStatus(
        String mode,
        boolean openaiCompatibleReady,
        boolean deepSeekReady,
        boolean realCallEnabled,
        String fallback) {

    public static ProviderStatus localRule() {
        return new ProviderStatus("local-rule", false, false, false, "local-rule");
    }
}
