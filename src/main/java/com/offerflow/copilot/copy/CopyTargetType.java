package com.offerflow.copilot.copy;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CopyTargetType {
    MATCH_REPORT,
    INTERVIEW_PREP,
    OPENING_MESSAGE,
    HUMAN_REVIEW_REWRITE,
    JD_ANALYSIS_SUMMARY,
    EVIDENCE_BINDING_SUMMARY,
    PROVIDER_SANDBOX_OUTPUT;

    @JsonValue
    public String apiName() {
        return name();
    }

    @JsonCreator
    public static CopyTargetType from(String value) {
        if (value == null || value.isBlank()) {
            return MATCH_REPORT;
        }
        String normalized = value.trim()
                .replace('-', '_')
                .toUpperCase(Locale.ROOT);
        return CopyTargetType.valueOf(normalized);
    }
}
