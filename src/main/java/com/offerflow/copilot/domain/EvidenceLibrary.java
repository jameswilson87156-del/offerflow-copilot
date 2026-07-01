package com.offerflow.copilot.domain;

import java.util.List;

public record EvidenceLibrary(
        String mode,
        int total,
        List<EvidenceCategory> categories,
        List<EvidenceItem> items,
        String disclaimer) {

    public record EvidenceCategory(String key, String label, int count) {
    }

    public record EvidenceItem(
            String id,
            String projectName,
            String projectSlug,
            String summary,
            List<String> abilityTags,
            List<String> evidenceSources,
            String credibility,
            List<String> matchableRequirements,
            String humanReviewStatus,
            String updatedAt,
            EvidenceDetail detail) {
    }

    public record EvidenceDetail(
            List<String> relatedSkills,
            List<String> suitableRoles,
            List<String> interviewAnswers,
            List<String> riskBoundaries,
            List<SourceStep> sourceChain) {
    }

    public record SourceStep(String label, String status, String note) {
    }
}
