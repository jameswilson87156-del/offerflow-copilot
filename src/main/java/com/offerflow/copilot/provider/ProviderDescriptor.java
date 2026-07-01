package com.offerflow.copilot.provider;

public record ProviderDescriptor(
        String providerMode,
        String displayName,
        boolean baseUrlConfigured,
        boolean configured,
        String apiKeyStatus,
        String model,
        int timeoutMs,
        boolean active,
        boolean realCallEnabled,
        boolean rawResponseSave,
        String fallbackPolicy,
        String boundaryNotice) {
}
