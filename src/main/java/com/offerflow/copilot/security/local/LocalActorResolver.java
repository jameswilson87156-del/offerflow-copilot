package com.offerflow.copilot.security.local;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class LocalActorResolver {

    public static final String DEFAULT_ACTOR = "demo.reviewer";
    public static final LocalActorRole DEFAULT_ROLE = LocalActorRole.REVIEWER;

    public LocalActorContext resolve(
            String bodyActor,
            String bodyRole,
            String headerActor,
            String headerRole,
            String requestId) {
        String actor = firstText(bodyActor, headerActor, DEFAULT_ACTOR);
        String rawRole = firstText(bodyRole, headerRole, DEFAULT_ROLE.name());
        LocalActorRole role = LocalActorRole.from(rawRole).orElse(LocalActorRole.VIEWER);
        String source = source(bodyActor, bodyRole, headerActor, headerRole, rawRole, role);
        return new LocalActorContext(actor, role, source, firstText(requestId, "", "permission-" + UUID.randomUUID()));
    }

    private String source(
            String bodyActor,
            String bodyRole,
            String headerActor,
            String headerRole,
            String rawRole,
            LocalActorRole role) {
        String actorSource = hasText(bodyActor) || hasText(bodyRole)
                ? "request-body"
                : hasText(headerActor) || hasText(headerRole) ? "headers" : "default-demo";
        if (!LocalActorRole.from(rawRole).isPresent() && hasText(rawRole)) {
            return actorSource + "; invalid-role-fallback=VIEWER";
        }
        if (hasText(bodyRole) || hasText(headerRole)) {
            return actorSource + "; role=" + role.name();
        }
        return actorSource;
    }

    private String firstText(String primary, String secondary, String fallback) {
        if (hasText(primary)) {
            return primary.trim();
        }
        if (hasText(secondary)) {
            return secondary.trim();
        }
        return fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
