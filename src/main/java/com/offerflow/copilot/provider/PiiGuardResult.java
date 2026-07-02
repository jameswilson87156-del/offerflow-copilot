package com.offerflow.copilot.provider;

import java.util.List;

public record PiiGuardResult(
        boolean blocked,
        List<String> riskFlags,
        String reason) {

    public PiiGuardResult {
        riskFlags = riskFlags == null ? List.of() : List.copyOf(riskFlags);
    }
}
