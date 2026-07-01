package com.offerflow.copilot.copy;

public record CopyPermissionRequest(
        CopyTargetType targetType,
        String targetId,
        String actor,
        String actorRole,
        String requestedText,
        String providerRunId,
        String traceId,
        String schemaVersion,
        String promptVersion) {
}
