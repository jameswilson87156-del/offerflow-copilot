package com.offerflow.copilot.domain;

import java.util.List;

public record MatchReportDemo(
        String mode,
        ReportSummary summary,
        ScoreBreakdown score,
        List<EvidenceSource> evidenceSources,
        List<SkillGap> skillGaps,
        List<RecommendedAction> recommendedActions,
        List<TraceStep> traceEvidence,
        String disclaimer) {

    public record ReportSummary(
            String jobTitle,
            List<String> recommendedResumeVersions,
            int totalScore,
            int maximumScore,
            String status,
            String note) {
    }

    public record ScoreBreakdown(
            List<ScoreItem> items,
            String note) {
    }

    public record ScoreItem(
            String key,
            String label,
            int value,
            int maximum,
            String detail,
            String tone) {
    }

    public record EvidenceSource(
            String requirement,
            String project,
            String strength,
            List<String> evidenceTypes,
            String rationale) {
    }

    public record SkillGap(
            String skill,
            String severity,
            String reason,
            String nextAction) {
    }

    public record RecommendedAction(
            String title,
            String detail,
            String priority) {
    }

    public record TraceStep(
            String label,
            String status,
            String detail) {
    }
}
