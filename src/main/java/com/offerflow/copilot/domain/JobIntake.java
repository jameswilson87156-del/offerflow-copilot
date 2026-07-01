package com.offerflow.copilot.domain;

import java.util.List;

public record JobIntake(
        String mode,
        List<JobSummary> items,
        String boundaryNotice) {

    public record JobSummary(
            String id,
            String title,
            String company,
            String city,
            String sourceType,
            String sourceNote,
            String status,
            int currentVersion,
            int bindingCount,
            String updatedAt) {
    }

    public record JobDetail(
            String mode,
            JobPost job,
            JdParseVersion currentParseVersion,
            List<DemoAnalysis.RequirementGroup> requirementGroups,
            List<JdEvidenceBinding> evidenceBindings,
            List<JdParseVersion> parseVersions,
            List<JdAuditEvent> auditTrail,
            String boundaryNotice) {
    }

    public record JobPost(
            String id,
            String title,
            String company,
            String city,
            String jdText,
            String sourceType,
            String sourceNote,
            boolean sanitized,
            String status,
            String createdAt,
            String updatedAt) {
    }

    public record JdParseVersion(
            String id,
            String jobId,
            int versionNo,
            String parserMode,
            String providerMode,
            String promptVersion,
            String schemaVersion,
            List<DemoAnalysis.RequirementGroup> extractedRequirements,
            List<String> keywords,
            List<String> riskTerms,
            String sanitizedText,
            String parseStatus,
            String createdAt) {
    }

    public record JdEvidenceBinding(
            String id,
            String jobId,
            String parseVersionId,
            String requirementKey,
            String requirementLabel,
            String evidenceId,
            String evidenceProject,
            String evidenceStrength,
            String bindingReason,
            String evidenceSource,
            String reviewStatus,
            String createdAt,
            String updatedAt) {
    }

    public record JdAuditEvent(
            String id,
            String jobId,
            String action,
            String actionLabel,
            String previousStatus,
            String nextStatus,
            String actor,
            String actorRole,
            List<String> changedFields,
            JdSnapshot beforeSnapshot,
            JdSnapshot afterSnapshot,
            String humanNote,
            String createdAt) {
    }

    public record JdSnapshot(
            String title,
            String company,
            String city,
            String sourceType,
            String sourceNote,
            String status,
            int currentVersion,
            int bindingCount,
            List<String> keywords,
            List<String> riskTerms) {
    }

    public record JobMutationRequest(
            String title,
            String company,
            String city,
            String jdText,
            String sourceType,
            String sourceNote,
            String actor,
            String actorRole,
            String humanNote) {
    }

    public record JobActionRequest(
            String actor,
            String actorRole,
            String humanNote) {
    }
}
