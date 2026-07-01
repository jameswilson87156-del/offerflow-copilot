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
            String status,
            String updatedAt,
            int auditCount,
            EvidenceDetail detail) {
    }

    public record EvidenceItemDetail(
            String mode,
            EvidenceItem item,
            List<EvidenceAuditEvent> auditTrail,
            String boundaryNotice) {
    }

    public record EvidenceAuditEvent(
            String id,
            String evidenceId,
            String action,
            String actionLabel,
            String previousStatus,
            String nextStatus,
            String actor,
            String actorRole,
            List<String> changedFields,
            EvidenceSnapshot beforeSnapshot,
            EvidenceSnapshot afterSnapshot,
            String humanNote,
            String traceId,
            String traceHash,
            String createdAt) {
    }

    public record EvidenceSnapshot(
            String projectName,
            String summary,
            List<String> skills,
            List<String> abilityTags,
            List<String> evidenceSources,
            String strength,
            List<String> matchableRequirements,
            String boundaryNote,
            List<String> riskBoundaries) {
    }

    public record EvidenceMutationRequest(
            String actor,
            String actorRole,
            String humanNote,
            String projectName,
            String summary,
            List<String> abilityTags,
            List<String> evidenceSources,
            String credibility,
            List<String> matchableRequirements,
            String boundaryNote,
            List<String> relatedSkills,
            List<String> suitableRoles,
            List<String> interviewAnswers,
            List<String> riskBoundaries,
            String targetStatus) {
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
