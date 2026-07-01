package com.offerflow.copilot.provider.contract;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProviderTaskType {
    JD_ANALYSIS("jd-analysis", "JD Analysis"),
    EVIDENCE_BINDING("evidence-binding", "Evidence Binding"),
    MATCH_REPORT("match-report", "Match Report"),
    INTERVIEW_PREP("interview-prep", "Interview Prep"),
    OPENING_MESSAGE("opening-message", "Opening Message"),
    HUMAN_REVIEW_REWRITE("human-review-rewrite", "Human Review Rewrite"),
    PROVIDER_SANDBOX("provider-sandbox", "Provider Sandbox");

    private final String apiName;
    private final String displayName;

    ProviderTaskType(String apiName, String displayName) {
        this.apiName = apiName;
        this.displayName = displayName;
    }

    @JsonValue
    public String apiName() {
        return apiName;
    }

    public String displayName() {
        return displayName;
    }

    @JsonCreator
    public static ProviderTaskType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Provider task type is required");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        return Arrays.stream(values())
                .filter(type -> type.apiName.equals(normalized)
                        || type.name().toLowerCase(Locale.ROOT).replace('_', '-').equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown provider task type: " + value));
    }

    public static ProviderTaskType fromOrDefault(String value) {
        if (value == null || value.isBlank()) {
            return PROVIDER_SANDBOX;
        }
        return from(value);
    }

    public static Stream<ProviderTaskType> stream() {
        return Arrays.stream(values());
    }
}
