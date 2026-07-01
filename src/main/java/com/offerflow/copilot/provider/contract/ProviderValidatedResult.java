package com.offerflow.copilot.provider.contract;

import java.util.List;

public record ProviderValidatedResult(
        boolean valid,
        List<ProviderContractViolation> violations,
        String sanitizedOutput,
        boolean fallbackRequired,
        boolean humanReviewRequired,
        List<String> riskFlags,
        String schemaVersion,
        String promptVersion,
        String riskPolicyVersion) {

    public ProviderValidatedResult {
        violations = violations == null ? List.of() : List.copyOf(violations);
        riskFlags = riskFlags == null ? List.of() : List.copyOf(riskFlags);
    }
}
