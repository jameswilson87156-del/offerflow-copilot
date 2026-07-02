package com.offerflow.copilot.security.local;

public record LocalActorContext(
        String actor,
        LocalActorRole actorRole,
        String source,
        String requestId) {
}
