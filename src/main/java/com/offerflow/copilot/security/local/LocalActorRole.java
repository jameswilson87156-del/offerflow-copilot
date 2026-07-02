package com.offerflow.copilot.security.local;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public enum LocalActorRole {
    OWNER,
    REVIEWER,
    EDITOR,
    VIEWER,
    SYSTEM;

    private static final Map<String, LocalActorRole> LEGACY_ALIASES = Map.ofEntries(
            Map.entry("HUMAN_REVIEWER", REVIEWER),
            Map.entry("HUMANREVIEWER", REVIEWER),
            Map.entry("HUMAN_REVIEW", REVIEWER),
            Map.entry("REVIEWER", REVIEWER),
            Map.entry("JD_REVIEWER", EDITOR),
            Map.entry("JD_EDITOR", EDITOR),
            Map.entry("EVIDENCE_REVIEWER", EDITOR),
            Map.entry("EVIDENCE_EDITOR", EDITOR),
            Map.entry("EDITOR", EDITOR),
            Map.entry("SYSTEM", SYSTEM),
            Map.entry("LOCAL_RULE", SYSTEM),
            Map.entry("LOCAL_RULE_SYSTEM", SYSTEM),
            Map.entry("OWNER", OWNER),
            Map.entry("VIEWER", VIEWER));

    public static Optional<LocalActorRole> from(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return Optional.empty();
        }
        String normalized = rawRole.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
        try {
            return Optional.of(LocalActorRole.valueOf(normalized));
        } catch (IllegalArgumentException ignored) {
            return Optional.ofNullable(LEGACY_ALIASES.get(normalized));
        }
    }
}
