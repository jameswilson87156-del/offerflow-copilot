package com.offerflow.copilot.provider.contract;

public record ProviderContractViolation(
        String code,
        String message,
        String field,
        String severity,
        boolean fallbackRequired,
        boolean humanReviewRequired) {
}
