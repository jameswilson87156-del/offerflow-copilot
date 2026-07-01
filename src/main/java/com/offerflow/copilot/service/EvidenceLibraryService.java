package com.offerflow.copilot.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.offerflow.copilot.domain.EvidenceCoverage;
import com.offerflow.copilot.domain.EvidenceLibrary;
import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.persistence.entity.ResumeEvidenceAuditEventEntity;
import com.offerflow.copilot.persistence.entity.ResumeEvidenceEntity;
import com.offerflow.copilot.persistence.repository.ResumeEvidenceAuditEventRepository;
import com.offerflow.copilot.persistence.repository.ResumeEvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EvidenceLibraryService {

    private static final String MODE = "mock/local-rule";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ResumeEvidenceRepository resumeEvidenceRepository;
    private final ResumeEvidenceAuditEventRepository auditEventRepository;
    private final JsonCodec jsonCodec;

    public EvidenceLibraryService(
            ResumeEvidenceRepository resumeEvidenceRepository,
            ResumeEvidenceAuditEventRepository auditEventRepository,
            JsonCodec jsonCodec) {
        this.resumeEvidenceRepository = resumeEvidenceRepository;
        this.auditEventRepository = auditEventRepository;
        this.jsonCodec = jsonCodec;
    }

    public EvidenceLibrary getLibrary() {
        List<ResumeEvidenceEntity> rows = resumeEvidenceRepository.findAll();
        return new EvidenceLibrary(
                MODE,
                rows.size(),
                categories(rows),
                rows.stream().map(this::item).toList(),
                "证据库从 H2 seeded demo data 读取；所有证据仍需人工确认，不保存真实隐私，也不代表真实客户、用户、流量或生产级效果。");
    }

    public EvidenceLibrary.EvidenceItemDetail getDetail(String id) {
        ResumeEvidenceEntity entity = requireEntity(id);
        return new EvidenceLibrary.EvidenceItemDetail(
                MODE,
                item(entity),
                auditEvents(entity.getId()),
                "证据仅用于作品集级求职材料维护，Confirmed 只代表人工确认过表达边界，不代表生产级能力。");
    }

    public List<EvidenceLibrary.EvidenceAuditEvent> auditEvents(String id) {
        if (resumeEvidenceRepository.findById(id).isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Evidence item not found");
        }
        return auditEventRepository.findByEvidenceId(id).stream()
                .map(this::auditEvent)
                .toList();
    }

    @Transactional
    public EvidenceLibrary.EvidenceItemDetail createDraft(EvidenceLibrary.EvidenceMutationRequest request) {
        ResumeEvidenceEntity entity = new ResumeEvidenceEntity();
        entity.setId("evidence-" + UUID.randomUUID().toString().substring(0, 8));
        entity.setProjectName(defaultText(request.projectName(), "Untitled Evidence Draft"));
        entity.setProjectSlug(slug(entity.getProjectName()));
        entity.setCategory(inferCategory(request.abilityTags()));
        entity.setSummary(defaultText(request.summary(), "待补充的作品集级项目证据草稿。"));
        entity.setSkillsJson(jsonCodec.write(defaultList(request.relatedSkills(), request.abilityTags())));
        entity.setAbilityTagsJson(jsonCodec.write(defaultList(request.abilityTags(), List.of("待确认能力标签"))));
        entity.setEvidenceSourcesJson(jsonCodec.write(defaultList(request.evidenceSources(), List.of("README"))));
        entity.setMatchableRequirementsJson(jsonCodec.write(defaultList(request.matchableRequirements(), List.of("待匹配岗位要求"))));
        entity.setDetailJson(jsonCodec.write(detailFromRequest(request, null)));
        entity.setStrength(defaultText(request.credibility(), "中"));
        entity.setReviewStatus("DRAFT");
        entity.setBoundaryNote(defaultText(request.boundaryNote(), "Draft 阶段不得声称真实客户、真实用户、生产级能力或招聘结果。"));
        entity.setCreatedAt(now());
        entity.setUpdatedAt(entity.getCreatedAt());
        resumeEvidenceRepository.save(entity);

        audit(
                entity,
                "CREATE_DRAFT",
                "",
                "Draft",
                request,
                List.of("projectName", "skills", "evidenceSources", "boundaryNote", "strength"),
                new EvidenceLibrary.EvidenceSnapshot(null, null, List.of(), List.of(), List.of(), null, List.of(), null, List.of()),
                snapshot(entity));
        return getDetail(entity.getId());
    }

    @Transactional
    public EvidenceLibrary.EvidenceItemDetail updateDraft(String id, EvidenceLibrary.EvidenceMutationRequest request) {
        ResumeEvidenceEntity entity = requireEntity(id);
        ensureNotArchived(entity);
        EvidenceLibrary.EvidenceSnapshot before = snapshot(entity);
        String previousStatus = displayStatus(entity.getReviewStatus());
        applyRequest(entity, request);
        entity.setReviewStatus("DRAFT");
        entity.setUpdatedAt(now());
        resumeEvidenceRepository.update(entity);
        EvidenceLibrary.EvidenceSnapshot after = snapshot(entity);

        List<String> changedFields = changedFields(before, after);
        audit(entity, "UPDATE_DRAFT", previousStatus, "Draft", request, changedFields, before, after);
        return getDetail(id);
    }

    @Transactional
    public EvidenceLibrary.EvidenceItemDetail confirm(String id, EvidenceLibrary.EvidenceMutationRequest request) {
        return transition(id, request, "CONFIRM", "CONFIRMED", List.of("status"));
    }

    @Transactional
    public EvidenceLibrary.EvidenceItemDetail returnToDraft(String id, EvidenceLibrary.EvidenceMutationRequest request) {
        return transition(id, request, "RETURN_TO_DRAFT", "DRAFT", List.of("status"));
    }

    @Transactional
    public EvidenceLibrary.EvidenceItemDetail archive(String id, EvidenceLibrary.EvidenceMutationRequest request) {
        return transition(id, request, "ARCHIVE", "ARCHIVED", List.of("status"));
    }

    @Transactional
    public EvidenceLibrary.EvidenceItemDetail restore(String id, EvidenceLibrary.EvidenceMutationRequest request) {
        String requested = normalizeStatus(request.targetStatus());
        String nextStatus = "CONFIRMED".equals(requested) ? "CONFIRMED" : "DRAFT";
        return transition(id, request, "RESTORE", nextStatus, List.of("status"));
    }

    public EvidenceCoverage getCoverage() {
        List<ResumeEvidenceEntity> rows = resumeEvidenceRepository.findAll();
        return new EvidenceCoverage(
                MODE,
                List.of(
                        coverage(rows, "Java", "强支撑", 92, "补充复杂并发案例"),
                        coverage(rows, "Spring Boot", "强支撑", 94, "补充事务压测记录"),
                        coverage(rows, "Vue 3", "中支撑", 68, "缺少组件测试证据"),
                        coverage(rows, "AI Workflow", "强支撑", 88, "需人工核验编排边界"),
                        coverage(rows, "RAG", "中支撑", 72, "缺少大规模离线评测"),
                        coverage(rows, "Trace", "强支撑", 86, "补充失败链路样例"),
                        coverage(rows, "CI", "中支撑", 74, "缺少发布审批记录"),
                        coverage(rows, "Deployment", "弱支撑", 46, "仅有演示部署，不代表生产运维")),
                "覆盖度由确定性 local-rule 根据数据库内的脱敏证据类型与人工确认状态计算，不是岗位录取概率。保持空缺比虚构证据更重要。");
    }

    private EvidenceLibrary.EvidenceItemDetail transition(
            String id,
            EvidenceLibrary.EvidenceMutationRequest request,
            String action,
            String nextRawStatus,
            List<String> changedFields) {
        ResumeEvidenceEntity entity = requireEntity(id);
        if (!"RESTORE".equals(action)) {
            ensureNotArchived(entity);
        }
        EvidenceLibrary.EvidenceSnapshot before = snapshot(entity);
        String previousStatus = displayStatus(entity.getReviewStatus());
        entity.setReviewStatus(nextRawStatus);
        entity.setUpdatedAt(now());
        resumeEvidenceRepository.update(entity);
        EvidenceLibrary.EvidenceSnapshot after = snapshot(entity);
        audit(entity, action, previousStatus, displayStatus(nextRawStatus), request, changedFields, before, after);
        return getDetail(id);
    }

    private void ensureNotArchived(ResumeEvidenceEntity entity) {
        if ("ARCHIVED".equals(entity.getReviewStatus())) {
            throw new ResponseStatusException(BAD_REQUEST, "Archived evidence is read-only; restore before editing");
        }
    }

    private void applyRequest(ResumeEvidenceEntity entity, EvidenceLibrary.EvidenceMutationRequest request) {
        if (hasText(request.projectName())) {
            entity.setProjectName(request.projectName());
            entity.setProjectSlug(slug(request.projectName()));
        }
        if (hasText(request.summary())) {
            entity.setSummary(request.summary());
        }
        if (request.abilityTags() != null) {
            entity.setAbilityTagsJson(jsonCodec.write(request.abilityTags()));
            entity.setCategory(inferCategory(request.abilityTags()));
        }
        if (request.evidenceSources() != null) {
            entity.setEvidenceSourcesJson(jsonCodec.write(request.evidenceSources()));
        }
        if (request.matchableRequirements() != null) {
            entity.setMatchableRequirementsJson(jsonCodec.write(request.matchableRequirements()));
        }
        if (hasText(request.credibility())) {
            entity.setStrength(request.credibility());
        }
        if (hasText(request.boundaryNote())) {
            entity.setBoundaryNote(request.boundaryNote());
        }
        EvidenceLibrary.EvidenceDetail currentDetail = jsonCodec.read(entity.getDetailJson(), EvidenceLibrary.EvidenceDetail.class);
        EvidenceLibrary.EvidenceDetail nextDetail = detailFromRequest(request, currentDetail);
        entity.setDetailJson(jsonCodec.write(nextDetail));
        if (request.relatedSkills() != null) {
            entity.setSkillsJson(jsonCodec.write(request.relatedSkills()));
        }
    }

    private EvidenceLibrary.EvidenceDetail detailFromRequest(
            EvidenceLibrary.EvidenceMutationRequest request,
            EvidenceLibrary.EvidenceDetail current) {
        return new EvidenceLibrary.EvidenceDetail(
                defaultList(request.relatedSkills(), current == null ? List.of() : current.relatedSkills()),
                defaultList(request.suitableRoles(), current == null ? List.of("待确认适配岗位") : current.suitableRoles()),
                defaultList(request.interviewAnswers(), current == null ? List.of("待补充可追溯回答材料") : current.interviewAnswers()),
                defaultList(request.riskBoundaries(), current == null ? List.of("不得虚构真实客户、真实用户或生产级数据") : current.riskBoundaries()),
                current == null ? defaultSourceChain() : current.sourceChain());
    }

    private List<EvidenceLibrary.SourceStep> defaultSourceChain() {
        return List.of(
                new EvidenceLibrary.SourceStep("README", "partial", "Draft 阶段待补充来源"),
                new EvidenceLibrary.SourceStep("截图/测试", "partial", "需要人工确认真实性"),
                new EvidenceLibrary.SourceStep("人工确认", "partial", "Confirmed 后才可作为简历证据使用"));
    }

    private ResumeEvidenceEntity requireEntity(String id) {
        return resumeEvidenceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Evidence item not found"));
    }

    private List<EvidenceLibrary.EvidenceCategory> categories(List<ResumeEvidenceEntity> rows) {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("all", "全部证据");
        labels.put("java", "Java 后端");
        labels.put("ai-app", "AI 应用开发");
        labels.put("agent", "AI Agent 工具");
        labels.put("rag", "RAG / Knowledge");
        labels.put("ai-coding", "AI Coding");
        labels.put("frontend", "前端工程");
        labels.put("ci", "CI / 部署");
        labels.put("trace", "Trace / Human Review");
        labels.put("archived", "Archived");
        return labels.entrySet().stream()
                .map((entry) -> new EvidenceLibrary.EvidenceCategory(
                        entry.getKey(),
                        entry.getValue(),
                        "all".equals(entry.getKey()) ? rows.size() : countCategory(rows, entry.getKey(), entry.getValue())))
                .toList();
    }

    private int countCategory(List<ResumeEvidenceEntity> rows, String key, String tag) {
        if ("archived".equals(key)) {
            return (int) rows.stream().filter((row) -> "ARCHIVED".equals(normalizeStatus(row.getReviewStatus()))).count();
        }
        return (int) rows.stream()
                .filter((row) -> readList(row.getAbilityTagsJson()).contains(tag))
                .count();
    }

    private EvidenceLibrary.EvidenceItem item(ResumeEvidenceEntity row) {
        String status = displayStatus(row.getReviewStatus());
        return new EvidenceLibrary.EvidenceItem(
                row.getId(),
                row.getProjectName(),
                row.getProjectSlug(),
                row.getSummary(),
                readList(row.getAbilityTagsJson()),
                readList(row.getEvidenceSourcesJson()),
                row.getStrength(),
                readList(row.getMatchableRequirementsJson()),
                legacyReviewStatus(status),
                status,
                row.getUpdatedAt().toLocalDate().toString(),
                auditEventRepository.findByEvidenceId(row.getId()).size(),
                jsonCodec.read(row.getDetailJson(), EvidenceLibrary.EvidenceDetail.class));
    }

    private EvidenceLibrary.EvidenceAuditEvent auditEvent(ResumeEvidenceAuditEventEntity entity) {
        return new EvidenceLibrary.EvidenceAuditEvent(
                entity.getId(),
                entity.getEvidenceId(),
                entity.getAction(),
                actionLabel(entity.getAction()),
                entity.getPreviousStatus(),
                entity.getNextStatus(),
                entity.getActor(),
                entity.getActorRole(),
                jsonCodec.readList(entity.getChangedFieldsJson(), String.class),
                jsonCodec.read(entity.getBeforeSnapshotJson(), EvidenceLibrary.EvidenceSnapshot.class),
                jsonCodec.read(entity.getAfterSnapshotJson(), EvidenceLibrary.EvidenceSnapshot.class),
                entity.getHumanNote(),
                "EVIDENCE-" + entity.getEvidenceId(),
                "audit-" + Integer.toHexString((entity.getEvidenceId() + ":" + entity.getId()).hashCode()),
                entity.getCreatedAt().format(FORMATTER));
    }

    private void audit(
            ResumeEvidenceEntity entity,
            String action,
            String previousStatus,
            String nextStatus,
            EvidenceLibrary.EvidenceMutationRequest request,
            List<String> changedFields,
            EvidenceLibrary.EvidenceSnapshot before,
            EvidenceLibrary.EvidenceSnapshot after) {
        ResumeEvidenceAuditEventEntity event = new ResumeEvidenceAuditEventEntity();
        event.setId("audit-evidence-" + UUID.randomUUID());
        event.setEvidenceId(entity.getId());
        event.setAction(action);
        event.setPreviousStatus(previousStatus == null ? "" : previousStatus);
        event.setNextStatus(nextStatus);
        event.setActor(defaultText(request.actor(), "demo-evidence-editor"));
        event.setActorRole(defaultText(request.actorRole(), "Evidence reviewer"));
        event.setChangedFieldsJson(jsonCodec.write(changedFields));
        event.setBeforeSnapshotJson(jsonCodec.write(before));
        event.setAfterSnapshotJson(jsonCodec.write(after));
        event.setHumanNote(defaultText(request.humanNote(), "demo evidence audit event"));
        event.setCreatedAt(now());
        auditEventRepository.save(event);
    }

    private EvidenceLibrary.EvidenceSnapshot snapshot(ResumeEvidenceEntity entity) {
        EvidenceLibrary.EvidenceDetail detail = jsonCodec.read(entity.getDetailJson(), EvidenceLibrary.EvidenceDetail.class);
        return new EvidenceLibrary.EvidenceSnapshot(
                entity.getProjectName(),
                entity.getSummary(),
                readList(entity.getSkillsJson()),
                readList(entity.getAbilityTagsJson()),
                readList(entity.getEvidenceSourcesJson()),
                entity.getStrength(),
                readList(entity.getMatchableRequirementsJson()),
                entity.getBoundaryNote(),
                detail.riskBoundaries());
    }

    private List<String> changedFields(EvidenceLibrary.EvidenceSnapshot before, EvidenceLibrary.EvidenceSnapshot after) {
        List<String> fields = new ArrayList<>();
        if (!Objects.equals(before.projectName(), after.projectName())) fields.add("projectName");
        if (!Objects.equals(before.summary(), after.summary())) fields.add("summary");
        if (!Objects.equals(before.skills(), after.skills())) fields.add("skills");
        if (!Objects.equals(before.abilityTags(), after.abilityTags())) fields.add("abilityTags");
        if (!Objects.equals(before.evidenceSources(), after.evidenceSources())) fields.add("evidenceSources");
        if (!Objects.equals(before.strength(), after.strength())) fields.add("strength");
        if (!Objects.equals(before.matchableRequirements(), after.matchableRequirements())) fields.add("matchableRequirements");
        if (!Objects.equals(before.boundaryNote(), after.boundaryNote())) fields.add("boundaryNote");
        if (!Objects.equals(before.riskBoundaries(), after.riskBoundaries())) fields.add("riskBoundaries");
        return fields.isEmpty() ? List.of("no field changed") : fields;
    }

    private EvidenceCoverage.CoverageItem coverage(
            List<ResumeEvidenceEntity> rows,
            String skill,
            String level,
            int score,
            String gap) {
        List<String> projects = rows.stream()
                .filter((row) -> !"ARCHIVED".equals(normalizeStatus(row.getReviewStatus())))
                .filter((row) -> supports(row, skill))
                .map(ResumeEvidenceEntity::getProjectName)
                .toList();
        return new EvidenceCoverage.CoverageItem(skill, level, score, projects, gap);
    }

    private boolean supports(ResumeEvidenceEntity row, String skill) {
        String needle = skill.toLowerCase();
        return readList(row.getSkillsJson()).stream().anyMatch((item) -> item.toLowerCase().contains(needle))
                || readList(row.getAbilityTagsJson()).stream().anyMatch((item) -> item.toLowerCase().contains(needle))
                || readList(row.getMatchableRequirementsJson()).stream().anyMatch((item) -> item.toLowerCase().contains(needle));
    }

    private String actionLabel(String action) {
        return switch (action) {
            case "CREATE_DRAFT" -> "创建草稿";
            case "UPDATE_DRAFT" -> "保存草稿";
            case "CONFIRM" -> "确认证据";
            case "RETURN_TO_DRAFT" -> "退回草稿";
            case "ARCHIVE" -> "归档证据";
            case "RESTORE" -> "恢复证据";
            default -> action;
        };
    }

    private String displayStatus(String status) {
        return switch (normalizeStatus(status)) {
            case "CONFIRMED" -> "Confirmed";
            case "RETURNED" -> "Returned";
            case "ARCHIVED" -> "Archived";
            default -> "Draft";
        };
    }

    private String legacyReviewStatus(String status) {
        return "Confirmed".equals(status) ? "Confirmed" : "Needs review";
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "DRAFT";
        }
        return switch (status.trim().toUpperCase()) {
            case "CONFIRMED", "CONFIRM" -> "CONFIRMED";
            case "RETURNED", "RETURN_TO_DRAFT" -> "RETURNED";
            case "ARCHIVED", "ARCHIVE" -> "ARCHIVED";
            case "NEEDS REVIEW", "DRAFT" -> "DRAFT";
            default -> "DRAFT";
        };
    }

    private String inferCategory(List<String> tags) {
        String corpus = String.join(" ", tags == null ? List.of() : tags).toLowerCase();
        if (corpus.contains("rag") || corpus.contains("knowledge")) return "rag";
        if (corpus.contains("vue") || corpus.contains("前端")) return "frontend";
        if (corpus.contains("java") || corpus.contains("spring")) return "java";
        if (corpus.contains("agent") || corpus.contains("tool")) return "agent";
        if (corpus.contains("coding")) return "ai-app";
        return "ai-app";
    }

    private String slug(String value) {
        String normalized = Normalizer.normalize(defaultText(value, "evidence"), Normalizer.Form.NFD)
                .replaceAll("[^A-Za-z0-9]+", "-")
                .replaceAll("(^-|-$)", "")
                .toLowerCase();
        return normalized.isBlank() ? "evidence-draft" : normalized;
    }

    private List<String> readList(String json) {
        return jsonCodec.readList(json, String.class);
    }

    private List<String> defaultList(List<String> value, List<String> fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }
}
