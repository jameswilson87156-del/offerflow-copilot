package com.offerflow.copilot.domain;

import java.util.List;

public record HumanReviewCenter(
        String mode,
        int pendingReviewCount,
        List<ReviewGroup> groups,
        List<ReviewSummary> items,
        List<String> compliancePrinciples) {

    public record ReviewGroup(String key, String label, int count) {
    }

    public record ReviewSummary(
            String id,
            String group,
            String title,
            String sourcePage,
            String riskLevel,
            String providerMode,
            String traceId,
            String status,
            String updatedAt) {
    }

    public record ReviewDetail(
            String id,
            String group,
            String title,
            String sourcePage,
            String riskLevel,
            String providerMode,
            String traceId,
            String status,
            String updatedAt,
            String reviewer,
            String humanNote,
            String aiSuggestion,
            ReviewEvidence evidence,
            List<String> riskTerms,
            List<TraceStep> traceEvidence,
            List<String> compliancePrinciples,
            boolean copyAllowed,
            String lastAction) {
    }

    public record ReviewEvidence(
            String jdSnippet,
            List<ResumeProject> resumeProjects,
            String evidenceNote) {
    }

    public record ResumeProject(
            String name,
            String excerpt,
            List<String> sourceTypes) {
    }

    public record TraceStep(
            String label,
            String status,
            String detail) {
    }
}
