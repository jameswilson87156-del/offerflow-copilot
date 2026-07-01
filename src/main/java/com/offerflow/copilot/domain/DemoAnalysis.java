package com.offerflow.copilot.domain;

import java.util.List;

public record DemoAnalysis(
        Job job,
        List<RequirementGroup> requirementGroups,
        List<EvidenceMatch> evidenceMatches,
        ScoreBreakdown score,
        InterviewPreparation interviewPreparation,
        HumanReview humanReview,
        List<TimelineStep> timeline,
        List<ResumeVersion> recommendedResumes,
        List<String> technicalTags,
        String disclaimer) {

    public record Job(String title, String company, String source, String updatedAt) {
    }

    public record RequirementGroup(String key, String label, String tone, List<Requirement> items) {
    }

    public record Requirement(
            String id,
            String title,
            String description,
            String level,
            List<String> keywords) {
    }

    public record EvidenceMatch(
            String requirement,
            String requirementDetail,
            String project,
            String projectSlug,
            String strength,
            String rationale,
            List<String> evidenceTypes) {
    }

    public record ScoreBreakdown(
            int total,
            int maximum,
            List<ScoreItem> items,
            String note) {
    }

    public record ScoreItem(
            String key,
            String label,
            int value,
            int maximum,
            String description,
            String tone) {
    }

    public record InterviewPreparation(
            List<String> followUpQuestions,
            StarDraft starDraft,
            List<String> riskReminders) {
    }

    public record StarDraft(String situation, String task, String action, String result) {
    }

    public record HumanReview(String aiOutputStatus, String humanStatus, boolean copyAllowed, String instruction) {
    }

    public record TimelineStep(String key, String label, String status, String detail, String timestamp) {
    }

    public record ResumeVersion(String name, String focus, boolean recommended) {
    }
}
