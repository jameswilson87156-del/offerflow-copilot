package com.offerflow.copilot.domain;

import java.util.List;

public record InterviewPrepDemo(
        String mode,
        String jobTitle,
        String positioningNotice,
        List<FocusArea> focusAreas,
        List<QuestionGroup> questionGroups,
        StarDraft starDraft,
        List<String> riskReminders,
        List<TimelineStep> reviewTimeline,
        String disclaimer) {

    public record FocusArea(String label, String detail, String evidence) {
    }

    public record QuestionGroup(
            String key,
            String label,
            List<String> questions) {
    }

    public record StarDraft(
            String situation,
            String task,
            String action,
            String result,
            String riskNote) {
    }

    public record TimelineStep(
            String key,
            String label,
            String status,
            String detail) {
    }
}
