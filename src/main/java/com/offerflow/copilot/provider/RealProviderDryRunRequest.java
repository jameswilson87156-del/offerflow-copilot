package com.offerflow.copilot.provider;

public record RealProviderDryRunRequest(
        String providerMode,
        String taskType,
        String inputText,
        String actor,
        String actorRole,
        boolean allowExternalCall,
        boolean confirmNoPii) {
}
