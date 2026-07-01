package com.offerflow.copilot.domain;

import java.util.List;

public record ProviderTraceCenter() {

    public record ProviderSettings(
            String mode,
            String currentStatus,
            List<ProviderCard> providers,
            List<SafetyBoundary> safetyBoundaries) {
    }

    public record ProviderCard(
            String id,
            String name,
            String status,
            String baseUrlStatus,
            String model,
            String timeout,
            String lastRun,
            String fallbackPolicy,
            String boundaryNotice,
            boolean realCallEnabled,
            String rawResponseSave,
            String apiKeyStatus) {
    }

    public record SafetyBoundary(
            String title,
            String description,
            String tone) {
    }

    public record TraceIndex(
            String mode,
            String currentStatus,
            List<TraceSummary> items) {
    }

    public record TraceSummary(
            String runId,
            String jobTitle,
            String providerMode,
            String finalProvider,
            String status,
            String startedAt,
            String duration,
            int evidenceCount,
            String humanReviewStatus) {
    }

    public record TraceRun(
            String runId,
            String jobTitle,
            String providerMode,
            String finalProvider,
            String model,
            String fallbackReason,
            String promptVersion,
            String schemaVersion,
            List<String> riskFlags,
            int evidenceCount,
            String humanReviewStatus,
            String duration,
            String traceHash,
            List<PipelineStep> pipeline,
            TraceEvidenceDetail evidenceDetail,
            List<String> technicalTags) {
    }

    public record PipelineStep(
            String key,
            String label,
            String status,
            String duration,
            String inputSummary,
            String outputSummary,
            String linkedEvidence) {
    }

    public record TraceEvidenceDetail(
            String jdSnippet,
            List<ResumeEvidenceRef> resumeEvidence,
            String jsonSummary,
            String fallbackReason,
            String humanNote) {
    }

    public record ResumeEvidenceRef(
            String id,
            String title,
            String excerpt,
            List<String> sources) {
    }
}
