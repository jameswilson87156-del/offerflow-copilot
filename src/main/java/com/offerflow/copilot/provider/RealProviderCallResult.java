package com.offerflow.copilot.provider;

public record RealProviderCallResult(
        boolean success,
        String normalizedText,
        String errorCode,
        String errorMessage,
        int durationMs) {
}
