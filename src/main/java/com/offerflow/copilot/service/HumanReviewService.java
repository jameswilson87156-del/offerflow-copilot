package com.offerflow.copilot.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.offerflow.copilot.domain.HumanReviewCenter;
import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.persistence.entity.HumanReviewAuditEventEntity;
import com.offerflow.copilot.persistence.entity.HumanReviewItemEntity;
import com.offerflow.copilot.persistence.repository.HumanReviewAuditEventRepository;
import com.offerflow.copilot.persistence.repository.HumanReviewItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class HumanReviewService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<String> COMPLIANCE_PRINCIPLES = List.of(
            "不输出 Offer 概率",
            "不做实时面试作弊",
            "不虚构真实客户",
            "不保存真实隐私",
            "不夸大模型能力");

    private final HumanReviewItemRepository humanReviewItemRepository;
    private final HumanReviewAuditEventRepository auditEventRepository;
    private final JsonCodec jsonCodec;
    private final MatchReportVersionService matchReportVersionService;

    public HumanReviewService(
            HumanReviewItemRepository humanReviewItemRepository,
            HumanReviewAuditEventRepository auditEventRepository,
            JsonCodec jsonCodec,
            MatchReportVersionService matchReportVersionService) {
        this.humanReviewItemRepository = humanReviewItemRepository;
        this.auditEventRepository = auditEventRepository;
        this.jsonCodec = jsonCodec;
        this.matchReportVersionService = matchReportVersionService;
    }

    public HumanReviewCenter listReviews() {
        List<HumanReviewCenter.ReviewSummary> items = humanReviewItemRepository.findAll().stream()
                .map(this::summary)
                .toList();
        return new HumanReviewCenter(
                "mock/local-rule",
                12,
                groups(items),
                items,
                COMPLIANCE_PRINCIPLES);
    }

    public HumanReviewCenter.ReviewDetail getReview(String id) {
        return humanReviewItemRepository.findById(id)
                .map(this::detail)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Review item not found"));
    }

    public List<HumanReviewCenter.AuditEvent> auditEvents(String id) {
        if (humanReviewItemRepository.findById(id).isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Review item not found");
        }
        return auditEventRepository.findByReviewId(id).stream()
                .map(this::auditEvent)
                .toList();
    }

    @Transactional
    public HumanReviewCenter.ReviewDetail confirm(String id, String actor, String actorRole, String note) {
        HumanReviewItemEntity entity = requireEntity(id);
        String previousStatus = entity.getStatus();
        String previousRiskLevel = entity.getRiskLevel();
        entity.setStatus("Confirmed");
        entity.setHumanNote(noteOrExisting(note, entity));
        entity.setLastAction("人工已确认，可复制使用");
        entity.setUpdatedAt(actionTimestamp());
        humanReviewItemRepository.update(entity);
        audit(entity, "CONFIRM", previousStatus, previousRiskLevel, actor, actorRole, entity.getHumanNote());
        matchReportVersionService.syncFromHumanReview(entity, "CONFIRM", actor, actorRole, entity.getHumanNote());
        return detail(entity);
    }

    @Transactional
    public HumanReviewCenter.ReviewDetail returnForRevision(String id, String actor, String actorRole, String note) {
        HumanReviewItemEntity entity = requireEntity(id);
        String previousStatus = entity.getStatus();
        String previousRiskLevel = entity.getRiskLevel();
        entity.setStatus("Returned");
        entity.setHumanNote(noteOrExisting(note, entity));
        entity.setLastAction("已退回修改，复制仍被禁用");
        entity.setUpdatedAt(actionTimestamp());
        humanReviewItemRepository.update(entity);
        audit(entity, "RETURN", previousStatus, previousRiskLevel, actor, actorRole, entity.getHumanNote());
        matchReportVersionService.syncFromHumanReview(entity, "RETURN", actor, actorRole, entity.getHumanNote());
        return detail(entity);
    }

    @Transactional
    public HumanReviewCenter.ReviewDetail flagRisk(String id, String actor, String actorRole, String note) {
        HumanReviewItemEntity entity = requireEntity(id);
        String previousStatus = entity.getStatus();
        String previousRiskLevel = entity.getRiskLevel();
        entity.setRiskLevel("高风险");
        entity.setStatus("Risk Flagged");
        entity.setHumanNote(noteOrExisting(note, entity));
        entity.setLastAction("已标记风险，等待重新生成或人工改写");
        entity.setUpdatedAt(actionTimestamp());
        humanReviewItemRepository.update(entity);
        audit(entity, "FLAG_RISK", previousStatus, previousRiskLevel, actor, actorRole, entity.getHumanNote());
        matchReportVersionService.syncFromHumanReview(entity, "FLAG_RISK", actor, actorRole, entity.getHumanNote());
        return detail(entity);
    }

    private HumanReviewItemEntity requireEntity(String id) {
        return humanReviewItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Review item not found"));
    }

    private HumanReviewCenter.ReviewSummary summary(HumanReviewItemEntity entity) {
        return new HumanReviewCenter.ReviewSummary(
                entity.getId(),
                group(entity),
                entity.getTitle(),
                entity.getSourcePage(),
                entity.getRiskLevel(),
                entity.getProviderMode(),
                entity.getTraceId(),
                entity.getStatus(),
                format(entity.getUpdatedAt()));
    }

    private HumanReviewCenter.ReviewDetail detail(HumanReviewItemEntity entity) {
        return new HumanReviewCenter.ReviewDetail(
                entity.getId(),
                group(entity),
                entity.getTitle(),
                entity.getSourcePage(),
                entity.getRiskLevel(),
                entity.getProviderMode(),
                entity.getTraceId(),
                entity.getStatus(),
                format(entity.getUpdatedAt()),
                entity.getReviewer(),
                entity.getHumanNote(),
                entity.getOriginalText(),
                new HumanReviewCenter.ReviewEvidence(
                        entity.getJdSnippet(),
                        jsonCodec.readList(entity.getEvidenceRefsJson(), HumanReviewCenter.ResumeProject.class),
                        entity.getEvidenceNote()),
                jsonCodec.readList(entity.getRiskTermsJson(), String.class),
                trace(),
                COMPLIANCE_PRINCIPLES,
                "Confirmed".equals(entity.getStatus()),
                entity.getLastAction(),
                auditEventRepository.findByReviewId(entity.getId()).stream()
                        .map(this::auditEvent)
                        .toList());
    }

    private List<HumanReviewCenter.ReviewGroup> groups(List<HumanReviewCenter.ReviewSummary> items) {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("high-risk", "高风险");
        labels.put("pending", "待确认");
        labels.put("returned", "已退回");
        labels.put("confirmed", "已确认");

        List<HumanReviewCenter.ReviewGroup> groups = new ArrayList<>();
        labels.forEach((key, label) -> {
            int count = (int) items.stream().filter((item) -> key.equals(item.group())).count();
            groups.add(new HumanReviewCenter.ReviewGroup(key, label, count));
        });
        return groups;
    }

    private String group(HumanReviewItemEntity entity) {
        if ("Returned".equals(entity.getStatus())) {
            return "returned";
        }
        if ("Confirmed".equals(entity.getStatus())) {
            return "confirmed";
        }
        if ("Risk Flagged".equals(entity.getStatus())) {
            return "high-risk";
        }
        if (entity.getRiskLevel().startsWith("高")) {
            return "high-risk";
        }
        return "pending";
    }

    private void audit(
            HumanReviewItemEntity entity,
            String action,
            String previousStatus,
            String previousRiskLevel,
            String actor,
            String actorRole,
            String note) {
        HumanReviewAuditEventEntity event = new HumanReviewAuditEventEntity();
        event.setId("audit-" + UUID.randomUUID());
        event.setReviewId(entity.getId());
        event.setAction(action);
        event.setPreviousStatus(previousStatus);
        event.setNextStatus(entity.getStatus());
        event.setPreviousRiskLevel(previousRiskLevel);
        event.setNextRiskLevel(entity.getRiskLevel());
        event.setActor(defaultText(actor, "demo-reviewer"));
        event.setActorRole(defaultText(actorRole, "Human reviewer"));
        event.setHumanNote(note);
        event.setTraceId(entity.getTraceId());
        event.setTraceHash(traceHash(entity));
        event.setCreatedAt(actionTimestamp());
        auditEventRepository.save(event);
    }

    private HumanReviewCenter.AuditEvent auditEvent(HumanReviewAuditEventEntity entity) {
        return new HumanReviewCenter.AuditEvent(
                entity.getId(),
                entity.getReviewId(),
                entity.getAction(),
                actionLabel(entity.getAction()),
                entity.getPreviousStatus(),
                entity.getNextStatus(),
                entity.getPreviousRiskLevel(),
                entity.getNextRiskLevel(),
                entity.getActor(),
                entity.getActorRole(),
                entity.getHumanNote(),
                entity.getTraceId(),
                entity.getTraceHash(),
                format(entity.getCreatedAt()));
    }

    private String actionLabel(String action) {
        return switch (action) {
            case "CONFIRM" -> "确认可用";
            case "RETURN" -> "退回修改";
            case "FLAG_RISK" -> "标记风险";
            case "ADD_NOTE" -> "添加人工备注";
            case "AUTO_RISK_GUARD" -> "自动风险扫描";
            default -> action;
        };
    }

    private String traceHash(HumanReviewItemEntity entity) {
        return "audit-" + Integer.toHexString((entity.getTraceId() + ":" + entity.getId()).hashCode());
    }

    private String noteOrExisting(String note, HumanReviewItemEntity entity) {
        return note == null || note.isBlank() ? entity.getHumanNote() : note;
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String format(LocalDateTime timestamp) {
        return timestamp.format(FORMATTER);
    }

    private LocalDateTime actionTimestamp() {
        return LocalDateTime.of(2026, 7, 1, 14, 35);
    }

    private List<HumanReviewCenter.TraceStep> trace() {
        return List.of(
                new HumanReviewCenter.TraceStep("JD Input", "done", "已解析岗位要求"),
                new HumanReviewCenter.TraceStep("Resume Evidence", "done", "引用匿名化项目证据"),
                new HumanReviewCenter.TraceStep("Provider fallback", "done", "local-rule，无外部调用"),
                new HumanReviewCenter.TraceStep("Schema Validate", "done", "结构校验通过"),
                new HumanReviewCenter.TraceStep("Risk Guard", "warning", "命中风险词，需人工确认"),
                new HumanReviewCenter.TraceStep("Human Review", "current", "复制前必须确认"));
    }
}
