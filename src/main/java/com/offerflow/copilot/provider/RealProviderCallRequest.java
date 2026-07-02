package com.offerflow.copilot.provider;

import com.offerflow.copilot.provider.contract.PromptContract;

public record RealProviderCallRequest(
        String providerMode,
        String baseUrl,
        String apiKey,
        String model,
        String inputText,
        PromptContract contract,
        int timeoutMs,
        String runId) {
}
