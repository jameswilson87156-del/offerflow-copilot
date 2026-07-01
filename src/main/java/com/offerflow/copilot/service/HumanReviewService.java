package com.offerflow.copilot.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.offerflow.copilot.domain.HumanReviewCenter;
import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.persistence.entity.HumanReviewItemEntity;
import com.offerflow.copilot.persistence.repository.HumanReviewItemRepository;
import org.springframework.stereotype.Service;
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
    private final JsonCodec jsonCodec;

    public HumanReviewService(HumanReviewItemRepository humanReviewItemRepository, JsonCodec jsonCodec) {
        this.humanReviewItemRepository = humanReviewItemRepository;
        this.jsonCodec = jsonCodec;
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

    public HumanReviewCenter.ReviewDetail confirm(String id, String note) {
        HumanReviewItemEntity entity = requireEntity(id);
        entity.setStatus("Confirmed");
        entity.setHumanNote(noteOrExisting(note, entity));
        entity.setLastAction("人工已确认，可复制使用");
        entity.setUpdatedAt(actionTimestamp());
        humanReviewItemRepository.update(entity);
        return detail(entity);
    }

    public HumanReviewCenter.ReviewDetail returnForRevision(String id, String note) {
        HumanReviewItemEntity entity = requireEntity(id);
        entity.setStatus("Returned");
        entity.setHumanNote(noteOrExisting(note, entity));
        entity.setLastAction("已退回修改，复制仍被禁用");
        entity.setUpdatedAt(actionTimestamp());
        humanReviewItemRepository.update(entity);
        return detail(entity);
    }

    public HumanReviewCenter.ReviewDetail flagRisk(String id, String note) {
        HumanReviewItemEntity entity = requireEntity(id);
        entity.setRiskLevel("高风险");
        entity.setStatus("Draft");
        entity.setHumanNote(noteOrExisting(note, entity));
        entity.setLastAction("已标记风险，等待重新生成或人工改写");
        entity.setUpdatedAt(actionTimestamp());
        humanReviewItemRepository.update(entity);
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
                entity.getLastAction());
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
        if (entity.getRiskLevel().startsWith("高")) {
            return "high-risk";
        }
        return "pending";
    }

    private String noteOrExisting(String note, HumanReviewItemEntity entity) {
        return note == null || note.isBlank() ? entity.getHumanNote() : note;
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
