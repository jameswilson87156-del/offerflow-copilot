package com.offerflow.copilot.provider;

import java.time.LocalDateTime;

public record ProviderHealth(
        String providerMode,
        String status,
        boolean configured,
        boolean realCallEnabled,
        LocalDateTime lastCheckedAt,
        String reason) {
}
