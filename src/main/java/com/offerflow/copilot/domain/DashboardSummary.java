package com.offerflow.copilot.domain;

public record DashboardSummary(
        int pendingJobDescriptions,
        int pendingHumanReviews,
        int recentApplications,
        ProviderStatus provider) {
}
