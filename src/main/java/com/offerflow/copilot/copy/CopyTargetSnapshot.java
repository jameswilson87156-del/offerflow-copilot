package com.offerflow.copilot.copy;

public record CopyTargetSnapshot(
        CopyTargetType targetType,
        String targetId,
        String targetStatus,
        String humanReviewStatus,
        boolean schemaValidated,
        boolean riskGuardPassed,
        String traceId,
        String providerRunId,
        String schemaVersion,
        String promptVersion,
        String copyText) {
}
