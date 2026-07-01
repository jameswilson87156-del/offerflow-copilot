package com.offerflow.copilot.domain;

import java.util.List;

public record ApplicationTracker(
        String mode,
        List<BoardColumn> boardColumns,
        List<ApplicationRecord> applications,
        List<CommunicationLog> communicationLogs,
        List<String> riskBoundaries,
        String disclaimer) {

    public record BoardColumn(
            String key,
            String label,
            int count,
            String tone) {
    }

    public record ApplicationRecord(
            String id,
            String company,
            String role,
            String city,
            String resumeVersion,
            String sourceNote,
            String status,
            String updatedAt,
            String nextAction) {
    }

    public record CommunicationLog(
            String applicationId,
            String stage,
            String note,
            String timestamp) {
    }
}
