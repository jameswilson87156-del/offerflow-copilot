package com.offerflow.copilot.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.offerflow.copilot.copy.CopyPermissionRequest;
import com.offerflow.copilot.copy.CopyPermissionResult;
import com.offerflow.copilot.copy.CopyPermissionService;
import com.offerflow.copilot.copy.CopyTargetType;
import com.offerflow.copilot.domain.HumanReviewCenter;
import com.offerflow.copilot.domain.MatchReportDemo;
import com.offerflow.copilot.domain.MatchReportVersioning;
import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.persistence.PersistenceSeedService;
import com.offerflow.copilot.persistence.entity.HumanReviewItemEntity;
import com.offerflow.copilot.persistence.entity.JdEvidenceBindingEntity;
import com.offerflow.copilot.persistence.entity.JdParseVersionEntity;
import com.offerflow.copilot.persistence.entity.JobPostEntity;
import com.offerflow.copilot.persistence.entity.MatchReportAuditEventEntity;
import com.offerflow.copilot.persistence.entity.MatchReportVersionEntity;
import com.offerflow.copilot.persistence.entity.ResumeEvidenceEntity;
import com.offerflow.copilot.persistence.repository.HumanReviewItemRepository;
import com.offerflow.copilot.persistence.repository.JdEvidenceBindingRepository;
import com.offerflow.copilot.persistence.repository.JdParseVersionRepository;
import com.offerflow.copilot.persistence.repository.JobPostRepository;
import com.offerflow.copilot.persistence.repository.MatchReportAuditEventRepository;
import com.offerflow.copilot.persistence.repository.MatchReportVersionRepository;
import com.offerflow.copilot.persistence.repository.ResumeEvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MatchReportVersionService {

    private static final String MODE = "mock/local-rule";
    private static final String GENERATED_BY = "local-rule scoring";
    private static final String DEFAULT_ACTOR = "local-rule report generator";
    private static final String DEFAULT_ACTOR_ROLE = "System";
    private static final String PROMPT_VERSION = "match-report-local-rule-v1";
    private static final String SCHEMA_VERSION = "match-report-version-v1";
    private static final String COPY_BOUNDARY_NOTICE =
            "匹配报告只有 Confirmed 后才允许复制使用；Human Review 是正式使用前的安全门。当前 scoring 是 local-rule，不做录用结果预测，也不承诺 Offer 结果。";
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<String> RISK_TERMS = List.of(
            "生产级", "稳定接入", "真实用户", "结果提升承诺", "结果承诺", "自动投递", "实时面试辅助");

    private final JsonCodec jsonCodec;
    private final MatchReportVersionRepository versionRepository;
    private final MatchReportAuditEventRepository auditEventRepository;
    private final JobPostRepository jobPostRepository;
    private final JdParseVersionRepository jdParseVersionRepository;
    private final JdEvidenceBindingRepository jdEvidenceBindingRepository;
    private final ResumeEvidenceRepository resumeEvidenceRepository;
    private final HumanReviewItemRepository humanReviewItemRepository;
    private final CopyPermissionService copyPermissionService;

    public MatchReportVersionService(
            JsonCodec jsonCodec,
            MatchReportVersionRepository versionRepository,
            MatchReportAuditEventRepository auditEventRepository,
            JobPostRepository jobPostRepository,
            JdParseVersionRepository jdParseVersionRepository,
            JdEvidenceBindingRepository jdEvidenceBindingRepository,
            ResumeEvidenceRepository resumeEvidenceRepository,
            HumanReviewItemRepository humanReviewItemRepository,
            CopyPermissionService copyPermissionService) {
        this.jsonCodec = jsonCodec;
        this.versionRepository = versionRepository;
        this.auditEventRepository = auditEventRepository;
        this.jobPostRepository = jobPostRepository;
        this.jdParseVersionRepository = jdParseVersionRepository;
        this.jdEvidenceBindingRepository = jdEvidenceBindingRepository;
        this.resumeEvidenceRepository = resumeEvidenceRepository;
        this.humanReviewItemRepository = humanReviewItemRepository;
        this.copyPermissionService = copyPermissionService;
    }

    public MatchReportVersioning.ReportDetail getDemoReport() {
        MatchReportVersionEntity version = versionRepository.findLatestByJobId(PersistenceSeedService.DEMO_JOB_ID)
                .or(() -> versionRepository.findLatest())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Match report version demo data not found"));
        return toDetail(version);
    }

    public List<MatchReportVersioning.VersionSummary> listVersions(String jobId) {
        getJob(jobId);
        return versionRepository.findByJobId(jobId).stream()
                .map(this::toSummary)
                .toList();
    }

    public MatchReportVersioning.ReportDetail getVersion(String versionId) {
        return toDetail(requireVersion(versionId));
    }

    public List<MatchReportVersioning.AuditEvent> auditEvents(String versionId) {
        requireVersion(versionId);
        return auditEventRepository.findByReportVersionId(versionId).stream()
                .map(this::toAuditEvent)
                .toList();
    }

    @Transactional
    public MatchReportVersioning.CopyCheck copyCheck(String versionId, MatchReportVersioning.ReportActionRequest request) {
        MatchReportVersionEntity entity = requireVersion(versionId);
        CopyPermissionResult permission = copyPermissionService.check(new CopyPermissionRequest(
                CopyTargetType.MATCH_REPORT,
                entity.getId(),
                actor(request),
                actorRole(request),
                "",
                "",
                entity.getTraceId(),
                entity.getSchemaVersion(),
                entity.getPromptVersion()));
        MatchReportVersioning.CopyCheck result = new MatchReportVersioning.CopyCheck(
                permission.allowed(),
                permission.reason(),
                permission.targetStatus(),
                permission.humanReviewStatus(),
                permission.boundaryNotice(),
                permission.targetType().name(),
                permission.targetId(),
                permission.schemaValidated(),
                permission.riskGuardPassed(),
                permission.confirmed(),
                permission.auditEventId());
        audit(entity, permission.allowed() ? "COPY_ENABLED" : "COPY_BLOCKED", entity.getStatus(), entity.getStatus(),
                List.of("copyPermission"), actor(request), actorRole(request), note(request, permission.reason()),
                LocalDateTime.now(), result);
        return result;
    }

    @Transactional
    public MatchReportVersioning.ReportDetail restore(String versionId, MatchReportVersioning.ReportActionRequest request) {
        MatchReportVersionEntity entity = requireVersion(versionId);
        String previousStatus = entity.getStatus();
        if (!List.of("ARCHIVED", "RETURNED", "RISK_FLAGGED").contains(previousStatus)) {
            throw new ResponseStatusException(BAD_REQUEST, "Only archived, returned, or risk-flagged match reports can be restored");
        }
        LocalDateTime now = LocalDateTime.now();
        entity.setStatus("DRAFT");
        entity.setUpdatedAt(now);
        versionRepository.update(entity);
        updateReviewStatus(entity, "Draft", "报告版本已恢复为 Draft，需重新送入人工复核。");
        audit(entity, "RESTORE_VERSION", previousStatus, "DRAFT", List.of("status", "humanReviewStatus"),
                actor(request), actorRole(request), note(request, "恢复报告版本为 Draft，复制仍需人工确认。"), now);
        return toDetail(entity);
    }

    @Transactional
    public void syncFromHumanReview(
            HumanReviewItemEntity reviewItem,
            String reviewAction,
            String actor,
            String actorRole,
            String humanNote) {
        if (!isMatchReportReview(reviewItem.getReviewType())) {
            return;
        }
        SyncDecision decision = syncDecision(reviewAction);
        if (decision == null) {
            return;
        }
        versionRepository.findByHumanReviewId(reviewItem.getId()).ifPresent((entity) -> {
            LocalDateTime now = reviewItem.getUpdatedAt() == null ? LocalDateTime.now() : reviewItem.getUpdatedAt();
            String previousStatus = entity.getStatus();
            String nextStatus = "ARCHIVED".equals(previousStatus) ? "ARCHIVED" : decision.nextStatus();
            if (!previousStatus.equals(nextStatus)) {
                entity.setStatus(nextStatus);
                entity.setUpdatedAt(now);
                versionRepository.update(entity);
            }
            audit(entity, decision.auditAction(), previousStatus, nextStatus, List.of("status", "humanReviewStatus"),
                    valueOr(actor, "demo-reviewer"), valueOr(actorRole, "Human reviewer"),
                    valueOr(humanNote, decision.note()), now);
        });
    }

    @Transactional
    public MatchReportVersioning.ReportDetail generate(String jobId, MatchReportVersioning.ReportActionRequest request) {
        JobPostEntity job = getJob(jobId);
        JdParseVersionEntity parseVersion = jdParseVersionRepository.findLatestByJobId(jobId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Generate match report requires a JD parse version"));
        List<JdEvidenceBindingEntity> bindings = jdEvidenceBindingRepository.findByParseVersionId(parseVersion.getId());
        int versionNo = versionRepository.nextVersionNo(jobId);
        String reportId = versionRepository.findLatestByJobId(jobId)
                .map(MatchReportVersionEntity::getReportId)
                .orElse("match-" + stableHash(jobId));
        String versionId = reportId + "-v" + versionNo;
        String traceId = traceId(versionId);
        String reviewId = "review-" + versionId;
        LocalDateTime now = LocalDateTime.now();

        LocalRuleReport report = buildLocalRuleReport(job, parseVersion, bindings);
        MatchReportVersionEntity entity = new MatchReportVersionEntity();
        entity.setId(versionId);
        entity.setReportId(reportId);
        entity.setJobId(jobId);
        entity.setParseVersionId(parseVersion.getId());
        entity.setVersionNo(versionNo);
        entity.setScore(report.score());
        entity.setSkillScore(report.skillScore());
        entity.setEvidenceScore(report.evidenceScore());
        entity.setRiskScore(report.riskScore());
        entity.setInterviewScore(report.interviewScore());
        entity.setRecommendedResume("Java 后端版 / AI Coding 版");
        entity.setStatus("DRAFT");
        entity.setSummaryJson(jsonCodec.write(report.summary()));
        entity.setScoreBreakdownJson(jsonCodec.write(report.scoreBreakdown()));
        entity.setEvidenceRefsJson(jsonCodec.write(report.evidenceSources()));
        entity.setSkillGapsJson(jsonCodec.write(report.skillGaps()));
        entity.setRecommendedActionsJson(jsonCodec.write(report.recommendedActions()));
        entity.setRiskNotesJson(jsonCodec.write(report.riskNotes()));
        entity.setGeneratedBy(GENERATED_BY);
        entity.setProviderMode("local-rule");
        entity.setPromptVersion(PROMPT_VERSION);
        entity.setSchemaVersion(SCHEMA_VERSION);
        entity.setTraceId(traceId);
        entity.setHumanReviewId(reviewId);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        versionRepository.save(entity);

        createOrUpdateReviewItem(entity, job, parseVersion, bindings, "Draft", now);
        audit(entity, "GENERATE_LOCAL_RULE", "NONE", "DRAFT", List.of("score", "summary", "evidenceRefs"),
                actor(request), actorRole(request), note(request, "使用 local-rule scoring 生成匹配报告版本。"), now);
        audit(entity, "CREATE_DRAFT", "NONE", "DRAFT", List.of("status", "humanReviewId"),
                actor(request), actorRole(request), "创建 Draft 报告版本并进入 Human Review 队列。", now);
        return toDetail(entity);
    }

    @Transactional
    public MatchReportVersioning.ReportDetail sendToReview(String versionId, MatchReportVersioning.ReportActionRequest request) {
        MatchReportVersionEntity entity = requireVersion(versionId);
        String previousStatus = entity.getStatus();
        if ("ARCHIVED".equals(previousStatus)) {
            throw new ResponseStatusException(BAD_REQUEST, "Archived match report versions are read-only; restore before sending to review");
        }
        entity.setStatus("IN_REVIEW");
        entity.setUpdatedAt(LocalDateTime.now());
        versionRepository.update(entity);
        updateReviewStatus(entity, "In Review", "已送入人工复核，确认前不可复制或外发。");
        audit(entity, "SEND_TO_REVIEW", previousStatus, "IN_REVIEW", List.of("status"),
                actor(request), actorRole(request), note(request, "报告版本送入人工复核。"), entity.getUpdatedAt());
        return toDetail(entity);
    }

    @Transactional
    public MatchReportVersioning.ReportDetail archive(String versionId, MatchReportVersioning.ReportActionRequest request) {
        MatchReportVersionEntity entity = requireVersion(versionId);
        String previousStatus = entity.getStatus();
        entity.setStatus("ARCHIVED");
        entity.setUpdatedAt(LocalDateTime.now());
        versionRepository.update(entity);
        updateReviewStatus(entity, "Archived", "报告版本已归档，需重新生成或恢复后再使用。");
        audit(entity, "ARCHIVE", previousStatus, "ARCHIVED", List.of("status"),
                actor(request), actorRole(request), note(request, "归档匹配报告版本。"), entity.getUpdatedAt());
        return toDetail(entity);
    }

    private MatchReportVersioning.ReportDetail toDetail(MatchReportVersionEntity entity) {
        JdParseVersionEntity parseVersion = jdParseVersionRepository.findById(entity.getParseVersionId()).orElse(null);
        int bindingCount = jdEvidenceBindingRepository.findByParseVersionId(entity.getParseVersionId()).size();
        String reviewStatus = humanReviewStatus(entity);
        return new MatchReportVersioning.ReportDetail(
                MODE,
                entity.getReportId(),
                entity.getId(),
                entity.getJobId(),
                entity.getVersionNo(),
                entity.getStatus(),
                format(entity.getCreatedAt()),
                format(entity.getUpdatedAt()),
                entity.getGeneratedBy(),
                entity.getProviderMode(),
                entity.getPromptVersion(),
                entity.getSchemaVersion(),
                entity.getTraceId(),
                entity.getParseVersionId(),
                parseVersion == null ? 0 : parseVersion.getVersionNo(),
                bindingCount,
                entity.getHumanReviewId(),
                reviewStatus,
                jsonCodec.read(entity.getSummaryJson(), MatchReportDemo.ReportSummary.class),
                jsonCodec.read(entity.getScoreBreakdownJson(), MatchReportDemo.ScoreBreakdown.class),
                jsonCodec.readList(entity.getEvidenceRefsJson(), MatchReportDemo.EvidenceSource.class),
                jsonCodec.readList(entity.getSkillGapsJson(), MatchReportDemo.SkillGap.class),
                jsonCodec.readList(entity.getRecommendedActionsJson(), MatchReportDemo.RecommendedAction.class),
                jsonCodec.readList(entity.getRiskNotesJson(), String.class),
                traceEvidence(entity.getStatus(), bindingCount),
                "这是匹配分析，不是结果承诺；所有建议需经人工复核后使用。当前为 local-rule demo，不调用真实 LLM。");
    }

    private MatchReportVersioning.VersionSummary toSummary(MatchReportVersionEntity entity) {
        JdParseVersionEntity parseVersion = jdParseVersionRepository.findById(entity.getParseVersionId()).orElse(null);
        int bindingCount = jdEvidenceBindingRepository.findByParseVersionId(entity.getParseVersionId()).size();
        String reviewStatus = humanReviewStatus(entity);
        return new MatchReportVersioning.VersionSummary(
                entity.getId(),
                entity.getReportId(),
                entity.getJobId(),
                entity.getParseVersionId(),
                parseVersion == null ? 0 : parseVersion.getVersionNo(),
                entity.getVersionNo(),
                entity.getScore(),
                entity.getStatus(),
                entity.getProviderMode(),
                entity.getPromptVersion(),
                entity.getTraceId(),
                entity.getHumanReviewId(),
                reviewStatus,
                bindingCount,
                format(entity.getCreatedAt()));
    }

    private MatchReportVersioning.AuditEvent toAuditEvent(MatchReportAuditEventEntity entity) {
        return new MatchReportVersioning.AuditEvent(
                entity.getId(),
                entity.getReportVersionId(),
                entity.getAction(),
                actionLabel(entity.getAction()),
                entity.getPreviousStatus(),
                entity.getNextStatus(),
                entity.getActor(),
                entity.getActorRole(),
                jsonCodec.readList(entity.getChangedFieldsJson(), String.class),
                entity.getHumanNote(),
                entity.getTraceId(),
                entity.getCopyAllowed(),
                entity.getCopyReason(),
                entity.getVersionStatus(),
                entity.getHumanReviewStatus(),
                entity.getBoundaryNotice(),
                format(entity.getCreatedAt()));
    }

    private LocalRuleReport buildLocalRuleReport(
            JobPostEntity job,
            JdParseVersionEntity parseVersion,
            List<JdEvidenceBindingEntity> bindings) {
        int bindingCount = bindings.size();
        int skillScore = Math.min(40, 24 + bindingCount * 3);
        int evidenceScore = Math.min(35, 20 + bindingCount * 2);
        int riskScore = -4;
        int interviewScore = 22;
        int total = skillScore + evidenceScore + riskScore + interviewScore;
        List<MatchReportDemo.EvidenceSource> evidenceSources = bindings.stream()
                .map(this::toEvidenceSource)
                .toList();
        if (evidenceSources.isEmpty()) {
            evidenceSources = List.of(new MatchReportDemo.EvidenceSource(
                    "待人工确认",
                    "暂无绑定证据",
                    "弱",
                    List.of("Human Review"),
                    "当前 JD 尚未绑定简历证据，报告只能作为 Draft 草稿。"));
        }

        MatchReportDemo.ReportSummary summary = new MatchReportDemo.ReportSummary(
                job.getTitle(),
                List.of("Java 后端版", "AI Coding 版"),
                total,
                100,
                "Draft，需要人工复核",
                "匹配得分只解释 JD 与简历证据覆盖，不代表录用结果。");
        MatchReportDemo.ScoreBreakdown scoreBreakdown = new MatchReportDemo.ScoreBreakdown(
                List.of(
                        new MatchReportDemo.ScoreItem("skills", "技能命中", skillScore, 40, "基于 JD parse keywords 与 evidence binding 数量的 local-rule 覆盖判断", "primary"),
                        new MatchReportDemo.ScoreItem("evidence", "项目证据", evidenceScore, 35, "绑定证据来自脱敏简历证据库，需人工确认可复制表述", "positive"),
                        new MatchReportDemo.ScoreItem("risk", "经验风险", riskScore, 10, "生产环境、真实用户、结果承诺等表述仍需降级处理", "warning"),
                        new MatchReportDemo.ScoreItem("interview", "面试准备", interviewScore, 25, "追问方向明确，但 STAR 表达需要人工整理", "info")),
                "评分用于解释 JD 与证据的覆盖关系，不输出任何录用结果预测。");
        return new LocalRuleReport(
                total,
                skillScore,
                evidenceScore,
                riskScore,
                interviewScore,
                summary,
                scoreBreakdown,
                evidenceSources,
                skillGaps(),
                recommendedActions(bindingCount),
                riskNotes(parseVersion));
    }

    private MatchReportDemo.EvidenceSource toEvidenceSource(JdEvidenceBindingEntity binding) {
        ResumeEvidenceEntity evidence = resumeEvidenceRepository.findById(binding.getEvidenceId()).orElse(null);
        List<String> evidenceTypes = evidence == null
                ? List.of(binding.getEvidenceSource())
                : jsonCodec.readList(evidence.getEvidenceSourcesJson(), String.class);
        return new MatchReportDemo.EvidenceSource(
                binding.getRequirementLabel(),
                evidence == null ? binding.getEvidenceId() : evidence.getProjectName(),
                binding.getEvidenceStrength(),
                evidenceTypes,
                binding.getBindingReason());
    }

    private void createOrUpdateReviewItem(
            MatchReportVersionEntity report,
            JobPostEntity job,
            JdParseVersionEntity parseVersion,
            List<JdEvidenceBindingEntity> bindings,
            String status,
            LocalDateTime timestamp) {
        HumanReviewItemEntity entity = humanReviewItemRepository.findById(report.getHumanReviewId()).orElseGet(HumanReviewItemEntity::new);
        entity.setId(report.getHumanReviewId());
        entity.setReviewType("MATCH_REPORT");
        entity.setTitle("匹配报告：" + job.getTitle());
        entity.setRiskLevel("中");
        entity.setSourcePage("Match Report");
        entity.setProviderMode(report.getProviderMode());
        entity.setTraceId(report.getTraceId());
        entity.setOriginalText(reviewOriginalText(report));
        entity.setJdSnippet(parseVersion.getSanitizedText());
        entity.setRiskTermsJson(jsonCodec.write(RISK_TERMS));
        entity.setEvidenceRefsJson(jsonCodec.write(reviewProjects(bindings)));
        entity.setStatus(status);
        entity.setReviewer("local-reviewer");
        entity.setHumanNote("自动生成的匹配报告版本，人工确认前不可复制或外发。");
        entity.setEvidenceNote("绑定 " + bindings.size() + " 条 JD evidence binding，来源为脱敏 seed demo data。");
        entity.setLastAction("Draft".equals(status) ? "已进入 Human Review 队列，等待人工确认" : "已送入人工复核");
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(timestamp);
            entity.setUpdatedAt(timestamp);
            humanReviewItemRepository.save(entity);
        } else {
            entity.setUpdatedAt(timestamp);
            humanReviewItemRepository.update(entity);
            return;
        }
    }

    private void updateReviewStatus(MatchReportVersionEntity report, String status, String lastAction) {
        humanReviewItemRepository.findById(report.getHumanReviewId()).ifPresent((item) -> {
            item.setStatus(status);
            item.setLastAction(lastAction);
            item.setUpdatedAt(report.getUpdatedAt());
            humanReviewItemRepository.update(item);
        });
    }

    private List<HumanReviewCenter.ResumeProject> reviewProjects(List<JdEvidenceBindingEntity> bindings) {
        return bindings.stream()
                .map((binding) -> {
                    ResumeEvidenceEntity evidence = resumeEvidenceRepository.findById(binding.getEvidenceId()).orElse(null);
                    List<String> sources = evidence == null
                            ? List.of(binding.getEvidenceSource())
                            : jsonCodec.readList(evidence.getEvidenceSourcesJson(), String.class);
                    return new HumanReviewCenter.ResumeProject(
                            evidence == null ? binding.getEvidenceId() : evidence.getProjectName(),
                            binding.getBindingReason(),
                            sources);
                })
                .toList();
    }

    private String reviewOriginalText(MatchReportVersionEntity report) {
        MatchReportDemo.ReportSummary summary = jsonCodec.read(report.getSummaryJson(), MatchReportDemo.ReportSummary.class);
        List<MatchReportDemo.RecommendedAction> actions = jsonCodec.readList(report.getRecommendedActionsJson(), MatchReportDemo.RecommendedAction.class);
        List<String> lines = new ArrayList<>();
        lines.add("综合匹配得分 " + summary.totalScore() + "/" + summary.maximumScore() + "。");
        lines.add(summary.note());
        actions.stream().map(MatchReportDemo.RecommendedAction::detail).forEach(lines::add);
        return String.join("\n", lines);
    }

    private void audit(
            MatchReportVersionEntity report,
            String action,
            String previousStatus,
            String nextStatus,
            List<String> changedFields,
            String actor,
            String actorRole,
            String humanNote,
            LocalDateTime timestamp) {
        audit(report, action, previousStatus, nextStatus, changedFields, actor, actorRole, humanNote, timestamp, null);
    }

    private void audit(
            MatchReportVersionEntity report,
            String action,
            String previousStatus,
            String nextStatus,
            List<String> changedFields,
            String actor,
            String actorRole,
            String humanNote,
            LocalDateTime timestamp,
            MatchReportVersioning.CopyCheck copyCheck) {
        MatchReportAuditEventEntity event = new MatchReportAuditEventEntity();
        event.setId("match-audit-" + UUID.randomUUID());
        event.setReportVersionId(report.getId());
        event.setAction(action);
        event.setPreviousStatus(previousStatus);
        event.setNextStatus(nextStatus);
        event.setActor(actor);
        event.setActorRole(actorRole);
        event.setChangedFieldsJson(jsonCodec.write(changedFields));
        event.setHumanNote(humanNote);
        event.setTraceId(report.getTraceId());
        if (copyCheck != null) {
            event.setCopyAllowed(copyCheck.allowed());
            event.setCopyReason(copyCheck.reason());
            event.setVersionStatus(copyCheck.versionStatus());
            event.setHumanReviewStatus(copyCheck.humanReviewStatus());
            event.setBoundaryNotice(copyCheck.boundaryNotice());
        }
        event.setCreatedAt(timestamp);
        auditEventRepository.save(event);
    }

    private String humanReviewStatus(MatchReportVersionEntity entity) {
        return humanReviewItemRepository.findById(entity.getHumanReviewId())
                .map(HumanReviewItemEntity::getStatus)
                .orElse("Draft");
    }

    private boolean isMatchReportReview(String reviewType) {
        return "MATCH_REPORT".equals(reviewType) || "match-report".equals(reviewType);
    }

    private SyncDecision syncDecision(String reviewAction) {
        return switch (reviewAction) {
            case "CONFIRM" -> new SyncDecision("CONFIRMED", "HUMAN_REVIEW_CONFIRMED", "Human Review 已确认，报告版本同步为 Confirmed。");
            case "RETURN" -> new SyncDecision("RETURNED", "HUMAN_REVIEW_RETURNED", "Human Review 已退回，报告版本同步为 Returned。");
            case "FLAG_RISK" -> new SyncDecision("RISK_FLAGGED", "HUMAN_REVIEW_FLAGGED_RISK", "Human Review 已标记风险，报告版本同步为 Risk Flagged。");
            default -> null;
        };
    }

    private MatchReportVersionEntity requireVersion(String versionId) {
        return versionRepository.findById(versionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Match report version not found"));
    }

    private JobPostEntity getJob(String jobId) {
        return jobPostRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Job not found"));
    }

    private List<MatchReportDemo.SkillGap> skillGaps() {
        return List.of(
                new MatchReportDemo.SkillGap("Redis 深度使用", "中", "已有缓存理解，但缺少复杂一致性案例。", "补充缓存穿透、热点 key、过期策略与数据一致性复盘。"),
                new MatchReportDemo.SkillGap("分布式事务", "高", "当前证据集中在单体或轻量服务。", "准备本地事务、补偿事务、消息最终一致性对比说明。"),
                new MatchReportDemo.SkillGap("性能优化", "中", "缺少独立压测与指标记录。", "补充接口耗时、SQL 索引、缓存命中率等可验证材料。"),
                new MatchReportDemo.SkillGap("A/B Testing", "低", "岗位可能涉及实验思维，但当前项目证据较少。", "准备实验分组、指标选择和误差风险说明。"),
                new MatchReportDemo.SkillGap("生产环境经验不足", "高", "作品集证据不能表述为真实生产流量。", "统一改写为作品集级演示和可复核工程边界。"));
    }

    private List<MatchReportDemo.RecommendedAction> recommendedActions(int bindingCount) {
        return List.of(
                new MatchReportDemo.RecommendedAction("建议投递", "证据覆盖 " + bindingCount + " 条 JD 要求，但材料需先过 Human Review。", "high"),
                new MatchReportDemo.RecommendedAction("推荐使用 Java 后端版简历", "把 Spring Boot、接口设计、Trace Evidence 放在首屏项目经历。", "high"),
                new MatchReportDemo.RecommendedAction("准备关键追问", "重点准备 Spring Boot / Trace Evidence / Provider fallback 相关追问。", "medium"),
                new MatchReportDemo.RecommendedAction("收紧能力表述", "不要夸大真实模型能力，不把 local-rule fallback 包装成真实 LLM 能力。", "high"));
    }

    private List<String> riskNotes(JdParseVersionEntity parseVersion) {
        List<String> parsedRisks = jsonCodec.readList(parseVersion.getRiskTermsJson(), String.class);
        List<String> notes = new ArrayList<>();
        notes.add("这是匹配分析，不是录用结果预测。");
        notes.add("所有建议需经人工复核后使用。");
        notes.add("当前 scoring 是 local-rule，不调用真实 LLM、DeepSeek 或中转站。");
        notes.add("每个版本绑定 JD parse version 与 resume evidence bindings。");
        if (parsedRisks.isEmpty()) {
            notes.add("仍需检查是否存在生产级、真实用户、自动投递等夸大表述。");
        } else {
            notes.add("JD 或输出命中风险词：" + String.join(" / ", parsedRisks));
        }
        return notes;
    }

    private List<MatchReportDemo.TraceStep> traceEvidence(String status, int bindingCount) {
        String reviewTraceStatus = switch (status) {
            case "IN_REVIEW" -> "current";
            case "CONFIRMED" -> "success";
            default -> "warning";
        };
        return List.of(
                new MatchReportDemo.TraceStep("JD parse version", "success", "读取当前最新 JD parse version"),
                new MatchReportDemo.TraceStep("Evidence bindings", "success", "绑定 " + bindingCount + " 条简历证据"),
                new MatchReportDemo.TraceStep("local-rule scoring", "success", "未调用真实 LLM 或外部 Provider"),
                new MatchReportDemo.TraceStep("Versioned asset", "success", "写入 match_report_version"),
                new MatchReportDemo.TraceStep("Risk notes", "warning", "不输出结果承诺，建议需复核"),
                new MatchReportDemo.TraceStep("Human Review", reviewTraceStatus, reviewStep(status)));
    }

    private String reviewStep(String status) {
        return switch (status) {
            case "IN_REVIEW" -> "已送入人工复核";
            case "CONFIRMED" -> "已人工确认";
            case "RETURNED" -> "已退回，需修改后恢复";
            case "RISK_FLAGGED" -> "已标记风险，不可作为正式建议";
            case "ARCHIVED" -> "已归档，不作为当前建议";
            default -> "Draft 状态，等待人工确认";
        };
    }

    private String actionLabel(String action) {
        return switch (action) {
            case "GENERATE_LOCAL_RULE" -> "local-rule 生成";
            case "CREATE_DRAFT" -> "创建 Draft";
            case "UPDATE_DRAFT" -> "更新 Draft";
            case "SEND_TO_REVIEW" -> "送入人工复核";
            case "CONFIRM" -> "确认可用";
            case "RETURN" -> "退回修改";
            case "HUMAN_REVIEW_CONFIRMED" -> "复核确认同步";
            case "HUMAN_REVIEW_RETURNED" -> "复核退回同步";
            case "HUMAN_REVIEW_FLAGGED_RISK" -> "复核风险同步";
            case "COPY_ENABLED" -> "复制许可通过";
            case "COPY_BLOCKED" -> "复制许可拦截";
            case "RESTORE_VERSION" -> "恢复版本";
            case "ARCHIVE" -> "归档版本";
            default -> action;
        };
    }

    private String actor(MatchReportVersioning.ReportActionRequest request) {
        return valueOr(request == null ? "" : request.actor(), DEFAULT_ACTOR);
    }

    private String actorRole(MatchReportVersioning.ReportActionRequest request) {
        return valueOr(request == null ? "" : request.actorRole(), DEFAULT_ACTOR_ROLE);
    }

    private String note(MatchReportVersioning.ReportActionRequest request, String fallback) {
        if (request == null) {
            return fallback;
        }
        return valueOr(valueOr(request.humanNote(), request.note()), fallback);
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String format(LocalDateTime timestamp) {
        return timestamp == null ? "" : DISPLAY_TIME.format(timestamp);
    }

    private String traceId(String versionId) {
        return "MR-" + stableHash(versionId).toUpperCase(Locale.ROOT);
    }

    private String stableHash(String value) {
        return Integer.toHexString(value.hashCode()).replace("-", "N");
    }

    private record LocalRuleReport(
            int score,
            int skillScore,
            int evidenceScore,
            int riskScore,
            int interviewScore,
            MatchReportDemo.ReportSummary summary,
            MatchReportDemo.ScoreBreakdown scoreBreakdown,
            List<MatchReportDemo.EvidenceSource> evidenceSources,
            List<MatchReportDemo.SkillGap> skillGaps,
            List<MatchReportDemo.RecommendedAction> recommendedActions,
            List<String> riskNotes) {
    }

    private record SyncDecision(String nextStatus, String auditAction, String note) {
    }
}
