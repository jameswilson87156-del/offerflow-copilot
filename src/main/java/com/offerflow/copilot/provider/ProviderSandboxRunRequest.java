package com.offerflow.copilot.provider;

public record ProviderSandboxRunRequest(
        String taskType,
        String inputText,
        String providerMode,
        boolean simulateFailure,
        boolean simulateTimeout,
        String actor,
        String actorRole) {
}
