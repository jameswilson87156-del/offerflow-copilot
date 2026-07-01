package com.offerflow.copilot.copy;

public record CopyPermissionResult(
        boolean allowed,
        String reason,
        CopyTargetType targetType,
        String targetId,
        String targetStatus,
        String humanReviewStatus,
        boolean schemaValidated,
        boolean riskGuardPassed,
        boolean confirmed,
        String boundaryNotice,
        String auditEventId,
        String copyText) {

    public CopyPermissionResult withAuditEventId(String nextAuditEventId) {
        return new CopyPermissionResult(
                allowed,
                reason,
                targetType,
                targetId,
                targetStatus,
                humanReviewStatus,
                schemaValidated,
                riskGuardPassed,
                confirmed,
                boundaryNotice,
                nextAuditEventId,
                copyText);
    }
}
