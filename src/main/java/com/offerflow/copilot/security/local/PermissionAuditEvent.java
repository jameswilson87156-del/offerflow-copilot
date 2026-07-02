package com.offerflow.copilot.security.local;

public record PermissionAuditEvent(
        String id,
        String actor,
        LocalActorRole actorRole,
        PermissionAction action,
        String targetType,
        String targetId,
        boolean allowed,
        String reason,
        String boundaryNotice,
        String requestId,
        String createdAt) {
}
