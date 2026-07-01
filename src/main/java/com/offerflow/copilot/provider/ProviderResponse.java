package com.offerflow.copilot.provider;

import java.util.List;

public record ProviderResponse(
        boolean success,
        String providerMode,
        String finalProvider,
        String model,
        String outputText,
        String structuredJson,
        boolean fallbackUsed,
        String fallbackReason,
        String errorCode,
        String errorMessage,
        int durationMs,
        String traceId,
        List<String> riskFlags,
        boolean rawResponseSaved,
        boolean humanReviewRequired) {

    public ProviderResponse {
        riskFlags = riskFlags == null ? List.of() : List.copyOf(riskFlags);
        rawResponseSaved = false;
        humanReviewRequired = true;
    }
}
