package com.offerflow.copilot.provider;

import java.util.List;

public record RealProviderDryRunResult(
        boolean success,
        boolean externalCallAttempted,
        boolean externalCallBlocked,
        String providerMode,
        String finalProvider,
        String model,
        boolean fallbackUsed,
        String fallbackReason,
        boolean schemaValidated,
        boolean riskGuardPassed,
        boolean humanReviewRequired,
        boolean copyAllowed,
        boolean rawResponseSaved,
        String traceId,
        String runId,
        List<String> riskFlags,
        String boundaryNotice) {

    public RealProviderDryRunResult {
        riskFlags = riskFlags == null ? List.of() : List.copyOf(riskFlags);
    }
}
