package com.offerflow.copilot.provider.contract;

public record ProviderValidationRequest(
        String taskType,
        String providerMode,
        String model,
        String structuredJson,
        String outputText,
        boolean simulateUnsafeClaim,
        boolean simulateMissingField,
        boolean simulateSchemaMismatch) {
}
