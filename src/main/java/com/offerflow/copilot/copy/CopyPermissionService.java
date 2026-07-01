package com.offerflow.copilot.copy;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.offerflow.copilot.persistence.entity.CopyPermissionAuditEventEntity;
import com.offerflow.copilot.persistence.entity.HumanReviewItemEntity;
import com.offerflow.copilot.persistence.entity.InterviewPrepEntity;
import com.offerflow.copilot.persistence.entity.MatchReportVersionEntity;
import com.offerflow.copilot.persistence.repository.CopyPermissionAuditEventRepository;
import com.offerflow.copilot.persistence.repository.HumanReviewItemRepository;
import com.offerflow.copilot.persistence.repository.InterviewPrepRepository;
import com.offerflow.copilot.persistence.repository.MatchReportVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CopyPermissionService {

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String DEFAULT_ACTOR = "demo-user";
    private static final String DEFAULT_ACTOR_ROLE = "Human reviewer";

    private final CopyPermissionPolicy policy;
    private final CopyPermissionAuditEventRepository auditEventRepository;
    private final MatchReportVersionRepository matchReportVersionRepository;
    private final HumanReviewItemRepository humanReviewItemRepository;
    private final InterviewPrepRepository interviewPrepRepository;

    public CopyPermissionService(
            CopyPermissionPolicy policy,
            CopyPermissionAuditEventRepository auditEventRepository,
            MatchReportVersionRepository matchReportVersionRepository,
            HumanReviewItemRepository humanReviewItemRepository,
            InterviewPrepRepository interviewPrepRepository) {
        this.policy = policy;
        this.auditEventRepository = auditEventRepository;
        this.matchReportVersionRepository = matchReportVersionRepository;
        this.humanReviewItemRepository = humanReviewItemRepository;
        this.interviewPrepRepository = interviewPrepRepository;
    }

    @Transactional
    public CopyPermissionResult check(CopyPermissionRequest request) {
        CopyPermissionRequest normalizedRequest = normalizeRequest(request);
        CopyTargetSnapshot snapshot = resolveTarget(normalizedRequest);
        CopyPermissionResult result = policy.evaluate(normalizedRequest, snapshot);
        String auditEventId = "copy-audit-" + UUID.randomUUID();
        auditEventRepository.save(toEntity(auditEventId, normalizedRequest, result));
        return result.withAuditEventId(auditEventId);
    }

    public List<CopyPermissionAuditEvent> auditEvents(CopyTargetType targetType, String targetId) {
        return auditEventRepository.findByTarget(targetType, targetId).stream()
                .map(this::toDto)
                .toList();
    }

    private CopyTargetSnapshot resolveTarget(CopyPermissionRequest request) {
        return switch (request.targetType()) {
            case MATCH_REPORT -> matchReportTarget(request);
            case INTERVIEW_PREP -> interviewPrepTarget(request);
            case OPENING_MESSAGE, HUMAN_REVIEW_REWRITE, JD_ANALYSIS_SUMMARY, EVIDENCE_BINDING_SUMMARY, PROVIDER_SANDBOX_OUTPUT ->
                    draftOnlyTarget(request);
        };
    }

    private CopyTargetSnapshot matchReportTarget(CopyPermissionRequest request) {
        MatchReportVersionEntity version = matchReportVersionRepository.findById(request.targetId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Match report version not found"));
        String humanReviewStatus = humanReviewItemRepository.findById(version.getHumanReviewId())
                .map(HumanReviewItemEntity::getStatus)
                .orElse("Draft");
        boolean schemaValidated = !valueOr(request.schemaVersion(), version.getSchemaVersion()).isBlank();
        boolean riskGuardPassed = !"RISK_FLAGGED".equals(version.getStatus());
        return new CopyTargetSnapshot(
                CopyTargetType.MATCH_REPORT,
                version.getId(),
                version.getStatus(),
                humanReviewStatus,
                schemaValidated,
                riskGuardPassed,
                valueOr(request.traceId(), version.getTraceId()),
                request.providerRunId(),
                valueOr(request.schemaVersion(), version.getSchemaVersion()),
                valueOr(request.promptVersion(), version.getPromptVersion()),
                "");
    }

    private CopyTargetSnapshot interviewPrepTarget(CopyPermissionRequest request) {
        InterviewPrepEntity prep = interviewPrepRepository.findById(request.targetId())
                .or(() -> interviewPrepRepository.findDemo())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Interview prep target not found"));
        String targetStatus = normalizeReviewStatus(prep.getReviewStatus());
        boolean schemaValidated = !valueOr(request.schemaVersion(), "interview-prep-copy-schema-v1").isBlank();
        boolean riskGuardPassed = !"RISK_FLAGGED".equals(targetStatus);
        return new CopyTargetSnapshot(
                CopyTargetType.INTERVIEW_PREP,
                prep.getId(),
                targetStatus,
                displayStatus(targetStatus),
                schemaValidated,
                riskGuardPassed,
                valueOr(request.traceId(), "INTERVIEW-" + stableHash(prep.getId())),
                request.providerRunId(),
                valueOr(request.schemaVersion(), "interview-prep-copy-schema-v1"),
                valueOr(request.promptVersion(), "interview-prep-prompt-v1"),
                "");
    }

    private CopyTargetSnapshot draftOnlyTarget(CopyPermissionRequest request) {
        return new CopyTargetSnapshot(
                request.targetType(),
                request.targetId(),
                "DRAFT",
                "Draft",
                !valueOr(request.schemaVersion(), "demo-copy-schema-v1").isBlank(),
                true,
                valueOr(request.traceId(), request.targetType().name() + "-" + stableHash(request.targetId())),
                request.providerRunId(),
                valueOr(request.schemaVersion(), "demo-copy-schema-v1"),
                valueOr(request.promptVersion(), "demo-copy-prompt-v1"),
                "");
    }

    private CopyPermissionAuditEventEntity toEntity(String id, CopyPermissionRequest request, CopyPermissionResult result) {
        CopyPermissionAuditEventEntity entity = new CopyPermissionAuditEventEntity();
        entity.setId(id);
        entity.setTargetType(result.targetType().name());
        entity.setTargetId(result.targetId());
        entity.setAction(result.allowed() ? "COPY_ALLOWED" : "COPY_BLOCKED");
        entity.setAllowed(result.allowed());
        entity.setReason(result.reason());
        entity.setTargetStatus(result.targetStatus());
        entity.setHumanReviewStatus(result.humanReviewStatus());
        entity.setSchemaValidated(result.schemaValidated());
        entity.setRiskGuardPassed(result.riskGuardPassed());
        entity.setActor(valueOr(request.actor(), DEFAULT_ACTOR));
        entity.setActorRole(valueOr(request.actorRole(), DEFAULT_ACTOR_ROLE));
        entity.setTraceId(valueOr(request.traceId(), ""));
        entity.setProviderRunId(valueOr(request.providerRunId(), ""));
        entity.setBoundaryNotice(result.boundaryNotice());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    private CopyPermissionAuditEvent toDto(CopyPermissionAuditEventEntity entity) {
        return new CopyPermissionAuditEvent(
                entity.getId(),
                CopyTargetType.from(entity.getTargetType()),
                entity.getTargetId(),
                entity.getAction(),
                Boolean.TRUE.equals(entity.getAllowed()),
                entity.getReason(),
                entity.getTargetStatus(),
                entity.getHumanReviewStatus(),
                Boolean.TRUE.equals(entity.getSchemaValidated()),
                Boolean.TRUE.equals(entity.getRiskGuardPassed()),
                entity.getActor(),
                entity.getActorRole(),
                entity.getTraceId(),
                entity.getProviderRunId(),
                entity.getBoundaryNotice(),
                format(entity.getCreatedAt()));
    }

    private CopyPermissionRequest normalizeRequest(CopyPermissionRequest request) {
        CopyPermissionRequest safe = request == null
                ? new CopyPermissionRequest(CopyTargetType.MATCH_REPORT, "", "", "", "", "", "", "", "")
                : request;
        CopyTargetType targetType = safe.targetType() == null ? CopyTargetType.MATCH_REPORT : safe.targetType();
        return new CopyPermissionRequest(
                targetType,
                valueOr(safe.targetId(), ""),
                valueOr(safe.actor(), DEFAULT_ACTOR),
                valueOr(safe.actorRole(), DEFAULT_ACTOR_ROLE),
                valueOr(safe.requestedText(), ""),
                valueOr(safe.providerRunId(), ""),
                valueOr(safe.traceId(), ""),
                valueOr(safe.schemaVersion(), ""),
                valueOr(safe.promptVersion(), ""));
    }

    private String normalizeReviewStatus(String value) {
        if (value == null || value.isBlank()) {
            return "DRAFT";
        }
        String normalized = value.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
        if (normalized.contains("CONFIRMED")) {
            return "CONFIRMED";
        }
        if (normalized.contains("IN_REVIEW")) {
            return "IN_REVIEW";
        }
        if (normalized.contains("RETURN")) {
            return "RETURNED";
        }
        if (normalized.contains("RISK")) {
            return "RISK_FLAGGED";
        }
        if (normalized.contains("ARCHIVE")) {
            return "ARCHIVED";
        }
        return "DRAFT";
    }

    private String displayStatus(String status) {
        return switch (status) {
            case "CONFIRMED" -> "Confirmed";
            case "IN_REVIEW" -> "In Review";
            case "RETURNED" -> "Returned";
            case "RISK_FLAGGED" -> "Risk Flagged";
            case "ARCHIVED" -> "Archived";
            default -> "Draft";
        };
    }

    private String stableHash(String value) {
        return Integer.toHexString(valueOr(value, "demo-target").hashCode()).replace("-", "N");
    }

    private String format(LocalDateTime timestamp) {
        return timestamp == null ? "" : DISPLAY_TIME.format(timestamp);
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
