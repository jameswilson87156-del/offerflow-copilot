package com.offerflow.copilot.copy;

public record CopyPermissionAuditEvent(
        String id,
        CopyTargetType targetType,
        String targetId,
        String action,
        boolean allowed,
        String reason,
        String targetStatus,
        String humanReviewStatus,
        boolean schemaValidated,
        boolean riskGuardPassed,
        String actor,
        String actorRole,
        String traceId,
        String providerRunId,
        String boundaryNotice,
        String createdAt) {
}
