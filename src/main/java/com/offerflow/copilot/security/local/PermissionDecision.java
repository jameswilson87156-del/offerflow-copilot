package com.offerflow.copilot.security.local;

public record PermissionDecision(
        boolean allowed,
        String reason,
        String actor,
        LocalActorRole actorRole,
        PermissionAction action,
        String targetType,
        String targetId,
        String boundaryNotice) {
}
