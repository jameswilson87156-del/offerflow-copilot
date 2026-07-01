package com.offerflow.copilot.domain;

import java.util.List;

public final class MatchReportVersioning {

    private MatchReportVersioning() {
    }

    public record ReportDetail(
            String mode,
            String reportId,
            String versionId,
            String jobId,
            int versionNo,
            String status,
            String createdAt,
            String updatedAt,
            String generatedBy,
            String providerMode,
            String promptVersion,
            String schemaVersion,
            String traceId,
            String parseVersionId,
            int parseVersionNo,
            int evidenceBindingCount,
            String humanReviewId,
            String humanReviewStatus,
            MatchReportDemo.ReportSummary summary,
            MatchReportDemo.ScoreBreakdown score,
            List<MatchReportDemo.EvidenceSource> evidenceSources,
            List<MatchReportDemo.SkillGap> skillGaps,
            List<MatchReportDemo.RecommendedAction> recommendedActions,
            List<String> riskNotes,
            List<MatchReportDemo.TraceStep> traceEvidence,
            String disclaimer) {
    }

    public record VersionSummary(
            String id,
            String reportId,
            String jobId,
            String parseVersionId,
            int parseVersionNo,
            int versionNo,
            int score,
            String status,
            String providerMode,
            String promptVersion,
            String traceId,
            String humanReviewId,
            String humanReviewStatus,
            int evidenceBindingCount,
            String createdAt) {
    }

    public record AuditEvent(
            String id,
            String reportVersionId,
            String action,
            String actionLabel,
            String previousStatus,
            String nextStatus,
            String actor,
            String actorRole,
            List<String> changedFields,
            String humanNote,
            String traceId,
            String createdAt) {
    }

    public record CopyCheck(
            boolean allowed,
            String reason,
            String versionStatus,
            String humanReviewStatus,
            String boundaryNotice) {
    }

    public record ReportActionRequest(
            String actor,
            String actorRole,
            String humanNote,
            String note) {
    }
}
