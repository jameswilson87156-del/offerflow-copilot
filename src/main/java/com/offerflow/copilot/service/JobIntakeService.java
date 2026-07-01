package com.offerflow.copilot.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.offerflow.copilot.domain.DemoAnalysis;
import com.offerflow.copilot.domain.JobIntake;
import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.persistence.PersistenceSeedService;
import com.offerflow.copilot.persistence.entity.JdAuditEventEntity;
import com.offerflow.copilot.persistence.entity.JdEvidenceBindingEntity;
import com.offerflow.copilot.persistence.entity.JdParseVersionEntity;
import com.offerflow.copilot.persistence.entity.JobPostEntity;
import com.offerflow.copilot.persistence.entity.ResumeEvidenceEntity;
import com.offerflow.copilot.persistence.repository.JdAuditEventRepository;
import com.offerflow.copilot.persistence.repository.JdEvidenceBindingRepository;
import com.offerflow.copilot.persistence.repository.JdParseVersionRepository;
import com.offerflow.copilot.persistence.repository.JobPostRepository;
import com.offerflow.copilot.persistence.repository.ResumeEvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobIntakeService {

    private static final String MODE = "mock/local-rule";
    private static final String DEFAULT_ACTOR = "demo-jd-editor";
    private static final String DEFAULT_ACTOR_ROLE = "JD reviewer";
    private static final String PARSER_MODE = "local-rule";
    private static final String PROVIDER_MODE = "local-rule fallback";
    private static final String PROMPT_VERSION = "manual-intake-no-llm";
    private static final String SCHEMA_VERSION = "jd-intake-v1";
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");
    private static final List<String> RISK_TERMS = List.of("生产级", "稳定接入", "真实用户", "提升 Offer 率", "保证通过", "自动投递", "实时面试辅助");
    private static final Map<String, List<String>> KEYWORD_ALIASES = Map.ofEntries(
            Map.entry("Java", List.of("java", "后端")),
            Map.entry("Spring Boot", List.of("spring boot", "springboot")),
            Map.entry("MySQL", List.of("mysql", "数据库", "索引", "事务")),
            Map.entry("Redis", List.of("redis", "缓存")),
            Map.entry("RAG", List.of("rag", "知识检索", "检索增强")),
            Map.entry("MCP", List.of("mcp", "tool gateway", "工具网关")),
            Map.entry("AI Workflow", List.of("ai", "ai workflow", "workflow", "工具调用", "编排")),
            Map.entry("Prompt", List.of("prompt", "提示词")),
            Map.entry("Trace", List.of("trace", "追踪", "证据链")),
            Map.entry("Human Review", List.of("human review", "人工复核")),
            Map.entry("Vue 3", List.of("vue", "vue 3")),
            Map.entry("TypeScript", List.of("typescript", "ts")),
            Map.entry("CI", List.of("ci", "github actions", "持续集成")),
            Map.entry("Deployment", List.of("deployment", "部署", "交付"))
    );

    private final JsonCodec jsonCodec;
    private final JobPostRepository jobPostRepository;
    private final ResumeEvidenceRepository resumeEvidenceRepository;
    private final JdParseVersionRepository jdParseVersionRepository;
    private final JdEvidenceBindingRepository jdEvidenceBindingRepository;
    private final JdAuditEventRepository jdAuditEventRepository;

    public JobIntakeService(
            JsonCodec jsonCodec,
            JobPostRepository jobPostRepository,
            ResumeEvidenceRepository resumeEvidenceRepository,
            JdParseVersionRepository jdParseVersionRepository,
            JdEvidenceBindingRepository jdEvidenceBindingRepository,
            JdAuditEventRepository jdAuditEventRepository) {
        this.jsonCodec = jsonCodec;
        this.jobPostRepository = jobPostRepository;
        this.resumeEvidenceRepository = resumeEvidenceRepository;
        this.jdParseVersionRepository = jdParseVersionRepository;
        this.jdEvidenceBindingRepository = jdEvidenceBindingRepository;
        this.jdAuditEventRepository = jdAuditEventRepository;
    }

    public JobIntake listJobs() {
        List<JobIntake.JobSummary> items = jobPostRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
        return new JobIntake(MODE, items, boundaryNotice());
    }

    public JobIntake.JobDetail detail(String jobId) {
        JobPostEntity job = getJob(jobId);
        Optional<JdParseVersionEntity> latest = jdParseVersionRepository.findLatestByJobId(jobId);
        List<JdEvidenceBindingEntity> bindings = latest
                .map(version -> jdEvidenceBindingRepository.findByParseVersionId(version.getId()))
                .orElseGet(List::of);
        List<DemoAnalysis.RequirementGroup> groups = latest
                .map(this::requirementsFrom)
                .orElseGet(List::of);
        List<JobIntake.JdParseVersion> versions = jdParseVersionRepository.findByJobId(jobId).stream()
                .map(this::toParseVersion)
                .toList();
        List<JobIntake.JdAuditEvent> auditTrail = auditEvents(jobId);
        return new JobIntake.JobDetail(
                MODE,
                toJobPost(job, status(job, latest.orElse(null), bindings.size())),
                latest.map(this::toParseVersion).orElse(null),
                groups,
                bindings.stream().map(this::toBinding).toList(),
                versions,
                auditTrail,
                boundaryNotice());
    }

    @Transactional
    public JobIntake.JobDetail createJob(JobIntake.JobMutationRequest request) {
        LocalDateTime now = LocalDateTime.now();
        JobPostEntity job = new JobPostEntity();
        job.setId("job-" + slug(valueOr(request.title(), "manual-jd")) + "-" + shortId());
        job.setTitle(valueOr(request.title(), "未命名手动 JD"));
        job.setCompany(valueOr(request.company(), "未填写公司"));
        job.setCity(valueOr(request.city(), "未填写城市"));
        job.setJdText(sanitizeText(valueOr(request.jdText(), "")));
        job.setSourceType(normalizeSourceType(request.sourceType()));
        job.setSourceNote(valueOr(request.sourceNote(), "用户手动粘贴 JD，未接招聘平台 API。"));
        job.setSanitized(true);
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        jobPostRepository.save(job);

        JobIntake.JdSnapshot after = snapshot(job);
        writeAudit(job.getId(), "CREATE_JD", "None", "Draft", actor(request.actor()), actorRole(request.actorRole()),
                List.of("title", "company", "city", "jdText", "sourceType"), emptySnapshot(), after,
                note(request.humanNote(), "手动创建 JD，来源为用户粘贴文本。"), now);
        return detail(job.getId());
    }

    @Transactional
    public JobIntake.JobDetail updateJob(String jobId, JobIntake.JobMutationRequest request) {
        JobPostEntity job = getJob(jobId);
        JobIntake.JdSnapshot before = snapshot(job);
        String previousStatus = before.status();
        List<String> changedFields = new ArrayList<>();

        updateIfPresent(request.title(), job.getTitle(), "title", changedFields, job::setTitle);
        updateIfPresent(request.company(), job.getCompany(), "company", changedFields, job::setCompany);
        updateIfPresent(request.city(), job.getCity(), "city", changedFields, job::setCity);
        if (request.jdText() != null && !sanitizeText(request.jdText()).equals(job.getJdText())) {
            job.setJdText(sanitizeText(request.jdText()));
            changedFields.add("jdText");
        }
        updateIfPresent(request.sourceNote(), job.getSourceNote(), "sourceNote", changedFields, job::setSourceNote);
        if (request.sourceType() != null && !normalizeSourceType(request.sourceType()).equals(job.getSourceType())) {
            job.setSourceType(normalizeSourceType(request.sourceType()));
            changedFields.add("sourceType");
        }
        job.setSanitized(true);
        job.setUpdatedAt(LocalDateTime.now());
        jobPostRepository.update(job);

        JobIntake.JdSnapshot after = snapshot(job);
        writeAudit(jobId, "UPDATE_JD", previousStatus, after.status(), actor(request.actor()), actorRole(request.actorRole()),
                changedFields.isEmpty() ? List.of("metadata-reviewed") : changedFields,
                before, after,
                note(request.humanNote(), "更新手动粘贴 JD，重新解析前不会调用外部 Provider。"), job.getUpdatedAt());
        return detail(jobId);
    }

    @Transactional
    public JobIntake.JobDetail parseJob(String jobId, JobIntake.JobActionRequest request) {
        JobPostEntity job = getJob(jobId);
        JobIntake.JdSnapshot before = snapshot(job);
        JdParseVersionEntity version = createParseVersion(job, LocalDateTime.now());
        JobIntake.JdSnapshot after = snapshot(job);
        writeAudit(jobId, "PARSE_LOCAL_RULE", before.status(), after.status(), actor(request.actor()), actorRole(request.actorRole()),
                List.of("parseVersion", "keywords", "riskTerms"), before, after,
                note(request.humanNote(), "使用 local-rule parser 生成 JD 解析版本。"), version.getCreatedAt());
        return detail(jobId);
    }

    @Transactional
    public JobIntake.JobDetail bindEvidence(String jobId, JobIntake.JobActionRequest request) {
        JobPostEntity job = getJob(jobId);
        JdParseVersionEntity version = jdParseVersionRepository.findLatestByJobId(jobId)
                .orElseGet(() -> createParseVersion(job, LocalDateTime.now()));
        JobIntake.JdSnapshot before = snapshot(job);
        List<JdEvidenceBindingEntity> existing = jdEvidenceBindingRepository.findByParseVersionId(version.getId());
        jdEvidenceBindingRepository.deleteByJobIdAndParseVersionId(jobId, version.getId());

        List<DemoAnalysis.Requirement> requirements = flatten(requirementsFrom(version));
        List<ResumeEvidenceEntity> evidenceItems = resumeEvidenceRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (DemoAnalysis.Requirement requirement : requirements) {
            bestEvidence(requirement, evidenceItems).ifPresent(evidence -> {
                JdEvidenceBindingEntity binding = new JdEvidenceBindingEntity();
                binding.setId("bind-" + hash(version.getId() + requirement.id() + evidence.getId()));
                binding.setJobId(jobId);
                binding.setParseVersionId(version.getId());
                binding.setRequirementKey(requirement.id());
                binding.setRequirementLabel(requirement.title());
                binding.setEvidenceId(evidence.getId());
                binding.setEvidenceStrength(evidence.getStrength());
                binding.setBindingReason(bindingReason(requirement, evidence));
                binding.setEvidenceSource(first(jsonCodec.readList(evidence.getEvidenceSourcesJson(), String.class), "README"));
                binding.setReviewStatus("Draft");
                binding.setCreatedAt(now);
                binding.setUpdatedAt(now);
                jdEvidenceBindingRepository.save(binding);
            });
        }

        JobIntake.JdSnapshot after = snapshot(job);
        writeAudit(jobId, existing.isEmpty() ? "BIND_EVIDENCE" : "REBIND_EVIDENCE", before.status(), after.status(),
                actor(request.actor()), actorRole(request.actorRole()),
                List.of("evidenceBindings"), before, after,
                note(request.humanNote(), "使用 local-rule keyword matching 绑定简历证据。"), now);
        return detail(jobId);
    }

    public List<JobIntake.JdParseVersion> parseVersions(String jobId) {
        getJob(jobId);
        return jdParseVersionRepository.findByJobId(jobId).stream().map(this::toParseVersion).toList();
    }

    public List<JobIntake.JdAuditEvent> auditEvents(String jobId) {
        getJob(jobId);
        return jdAuditEventRepository.findByJobId(jobId).stream().map(this::toAuditEvent).toList();
    }

    public List<JobIntake.JdEvidenceBinding> evidenceBindings(String jobId) {
        getJob(jobId);
        return jdEvidenceBindingRepository.findByJobId(jobId).stream().map(this::toBinding).toList();
    }

    public DemoAnalysis demoAnalysis() {
        JobPostEntity job = jobPostRepository.findById(PersistenceSeedService.DEMO_JOB_ID)
                .orElseGet(() -> jobPostRepository.findAll().stream().findFirst().orElseThrow());
        Optional<JdParseVersionEntity> latest = jdParseVersionRepository.findLatestByJobId(job.getId());
        List<DemoAnalysis.RequirementGroup> groups = latest.map(this::requirementsFrom).orElseGet(this::fallbackRequirementGroups);
        List<JdEvidenceBindingEntity> bindings = latest
                .map(version -> jdEvidenceBindingRepository.findByParseVersionId(version.getId()))
                .orElseGet(List::of);
        List<DemoAnalysis.EvidenceMatch> matches = bindings.isEmpty() ? fallbackEvidenceMatches() : bindings.stream()
                .map(this::toEvidenceMatch)
                .toList();

        return new DemoAnalysis(
                new DemoAnalysis.Job(job.getTitle(), job.getCompany(), "手动粘贴 JD · H2 demo persistence", format(job.getUpdatedAt())),
                groups,
                matches,
                score(),
                interviewPreparation(),
                new DemoAnalysis.HumanReview("Draft", "Pending", false, "AI / 规则生成内容必须人工确认事实和措辞后才能复制使用。"),
                timeline(),
                List.of(
                        new DemoAnalysis.ResumeVersion("Java 后端版", "突出 Spring Boot、接口设计与工程交付", true),
                        new DemoAnalysis.ResumeVersion("AI Coding 版", "突出 AI Workflow、RAG 与工具编排", true)),
                List.of("Spring Boot 3", "Vue 3", "TypeScript", "H2", "MyBatis-Plus", "local-rule parsing", "Trace Evidence", "Human Review"),
                "这是 mock/local-rule 演示结果，不是录取概率或真实招聘结论。所有内容需要人工复核。");
    }

    private JdParseVersionEntity createParseVersion(JobPostEntity job, LocalDateTime now) {
        ParsedJd parsed = parseLocalRule(job.getJdText());
        int versionNo = jdParseVersionRepository.nextVersionNo(job.getId());
        JdParseVersionEntity version = new JdParseVersionEntity();
        version.setId(job.getId() + "-parse-v" + versionNo);
        version.setJobId(job.getId());
        version.setVersionNo(versionNo);
        version.setParserMode(PARSER_MODE);
        version.setProviderMode(PROVIDER_MODE);
        version.setPromptVersion(PROMPT_VERSION);
        version.setSchemaVersion(SCHEMA_VERSION);
        version.setExtractedRequirementsJson(jsonCodec.write(parsed.requirementGroups()));
        version.setKeywordsJson(jsonCodec.write(parsed.keywords()));
        version.setRiskTermsJson(jsonCodec.write(parsed.riskTerms()));
        version.setSanitizedText(parsed.sanitizedText());
        version.setParseStatus("success");
        version.setCreatedAt(now);
        jdParseVersionRepository.save(version);
        return version;
    }

    private ParsedJd parseLocalRule(String jdText) {
        String sanitizedText = sanitizeText(jdText);
        List<String> keywords = extractKeywords(sanitizedText);
        List<String> riskTerms = RISK_TERMS.stream().filter(term -> sanitizedText.contains(term)).toList();

        List<DemoAnalysis.Requirement> core = new ArrayList<>();
        if (containsAny(keywords, "Java", "Spring Boot")) {
            core.add(new DemoAnalysis.Requirement("req-core-java", "Java / Spring Boot", "熟悉 Java 后端与 Spring Boot API 开发", "核心", keepKnown(keywords, "Java", "Spring Boot")));
        }
        if (containsAny(keywords, "MySQL", "Redis")) {
            core.add(new DemoAnalysis.Requirement("req-core-data", "MySQL / Redis", "理解数据库、事务、索引或缓存相关基础", "核心", keepKnown(keywords, "MySQL", "Redis")));
        }
        if (core.isEmpty()) {
            core.add(new DemoAnalysis.Requirement("req-core-engineering", "工程基础", "需要结合 JD 原文人工确认核心工程要求", "核心", List.of("工程基础")));
        }

        List<DemoAnalysis.Requirement> bonus = new ArrayList<>();
        if (containsAny(keywords, "AI Workflow", "Prompt")) {
            bonus.add(new DemoAnalysis.Requirement("req-bonus-ai", "AI 应用与工具链", "涉及 RAG、MCP、Prompt Workflow 或 AI 工具集成", "加分", keepKnown(keywords, "AI Workflow", "RAG", "MCP", "Prompt", "Trace", "Human Review")));
        }
        if (containsAny(keywords, "RAG", "MCP")) {
            bonus.add(new DemoAnalysis.Requirement("req-bonus-rag-mcp", "RAG / MCP 实践", "关注知识检索、工具网关、引用追踪与证据链", "加分", keepKnown(keywords, "RAG", "MCP", "Trace")));
        }
        if (containsAny(keywords, "Vue 3", "TypeScript", "CI", "Deployment")) {
            bonus.add(new DemoAnalysis.Requirement("req-bonus-delivery", "工程交付与前端协作", "包含前端工程、持续集成、部署或交付协作要求", "加分", keepKnown(keywords, "Vue 3", "TypeScript", "CI", "Deployment")));
        }
        if (bonus.isEmpty()) {
            bonus.add(new DemoAnalysis.Requirement("req-bonus-proof", "证据完整度", "建议补充 README、截图、测试或 Trace 证据", "加分", List.of("Trace", "README")));
        }

        List<DemoAnalysis.Requirement> risk = new ArrayList<>();
        if (!riskTerms.isEmpty()) {
            risk.add(new DemoAnalysis.Requirement("req-risk-terms", "高风险表述复核", "JD 或生成内容命中风险词，需要人工确认边界", "风险", riskTerms));
        } else {
            risk.add(new DemoAnalysis.Requirement("req-risk-boundary", "经验边界复核", "检查是否要求真实生产经验、真实用户或不可验证指标", "风险", List.of("Human Review", "Trace")));
        }

        return new ParsedJd(
                List.of(
                        new DemoAnalysis.RequirementGroup("core", "核心要求", "primary", core),
                        new DemoAnalysis.RequirementGroup("bonus", "加分要求", "positive", bonus),
                        new DemoAnalysis.RequirementGroup("risk", "风险要求", "warning", risk)),
                keywords,
                riskTerms,
                sanitizedText);
    }

    private List<String> extractKeywords(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        Set<String> keywords = new LinkedHashSet<>();
        KEYWORD_ALIASES.forEach((keyword, aliases) -> {
            for (String alias : aliases) {
                if (normalized.contains(alias.toLowerCase(Locale.ROOT))) {
                    keywords.add(keyword);
                    break;
                }
            }
        });
        if (keywords.isEmpty()) {
            keywords.add("人工复核");
        }
        return List.copyOf(keywords);
    }

    private Optional<ResumeEvidenceEntity> bestEvidence(DemoAnalysis.Requirement requirement, List<ResumeEvidenceEntity> evidenceItems) {
        return evidenceItems.stream()
                .map(evidence -> new EvidenceCandidate(evidence, evidenceScore(requirement, evidence)))
                .filter(candidate -> candidate.score() > 0)
                .max(Comparator.comparingInt(EvidenceCandidate::score))
                .map(EvidenceCandidate::evidence);
    }

    private int evidenceScore(DemoAnalysis.Requirement requirement, ResumeEvidenceEntity evidence) {
        String haystack = String.join(" ",
                evidence.getProjectName(),
                evidence.getSummary(),
                evidence.getSkillsJson(),
                evidence.getAbilityTagsJson(),
                evidence.getMatchableRequirementsJson()).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String keyword : requirement.keywords()) {
            if (haystack.contains(keyword.toLowerCase(Locale.ROOT))) {
                score += 3;
            }
            for (String alias : KEYWORD_ALIASES.getOrDefault(keyword, List.of())) {
                if (haystack.contains(alias.toLowerCase(Locale.ROOT))) {
                    score += 2;
                }
            }
        }
        if ("强".equals(evidence.getStrength())) {
            score += 1;
        }
        return score;
    }

    private String bindingReason(DemoAnalysis.Requirement requirement, ResumeEvidenceEntity evidence) {
        return "local-rule keyword matching：岗位要求“" + requirement.title() + "”与证据“"
                + evidence.getProjectName() + "”在 " + String.join(" / ", requirement.keywords()) + " 上有交集，需要人工复核后使用。";
    }

    private JobIntake.JobSummary toSummary(JobPostEntity job) {
        Optional<JdParseVersionEntity> latest = jdParseVersionRepository.findLatestByJobId(job.getId());
        int bindingCount = latest.map(version -> jdEvidenceBindingRepository.findByParseVersionId(version.getId()).size()).orElse(0);
        return new JobIntake.JobSummary(
                job.getId(), job.getTitle(), job.getCompany(), job.getCity(), job.getSourceType(), job.getSourceNote(),
                status(job, latest.orElse(null), bindingCount),
                latest.map(JdParseVersionEntity::getVersionNo).orElse(0),
                bindingCount,
                format(job.getUpdatedAt()));
    }

    private JobIntake.JobPost toJobPost(JobPostEntity job, String status) {
        return new JobIntake.JobPost(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getCity(),
                job.getJdText(),
                job.getSourceType(),
                job.getSourceNote(),
                Boolean.TRUE.equals(job.getSanitized()),
                status,
                format(job.getCreatedAt()),
                format(job.getUpdatedAt()));
    }

    private JobIntake.JdParseVersion toParseVersion(JdParseVersionEntity entity) {
        return new JobIntake.JdParseVersion(
                entity.getId(),
                entity.getJobId(),
                entity.getVersionNo(),
                entity.getParserMode(),
                entity.getProviderMode(),
                entity.getPromptVersion(),
                entity.getSchemaVersion(),
                requirementsFrom(entity),
                jsonCodec.readList(entity.getKeywordsJson(), String.class),
                jsonCodec.readList(entity.getRiskTermsJson(), String.class),
                entity.getSanitizedText(),
                entity.getParseStatus(),
                format(entity.getCreatedAt()));
    }

    private JobIntake.JdEvidenceBinding toBinding(JdEvidenceBindingEntity entity) {
        ResumeEvidenceEntity evidence = resumeEvidenceRepository.findById(entity.getEvidenceId()).orElse(null);
        return new JobIntake.JdEvidenceBinding(
                entity.getId(),
                entity.getJobId(),
                entity.getParseVersionId(),
                entity.getRequirementKey(),
                entity.getRequirementLabel(),
                entity.getEvidenceId(),
                evidence == null ? entity.getEvidenceId() : evidence.getProjectName(),
                entity.getEvidenceStrength(),
                entity.getBindingReason(),
                entity.getEvidenceSource(),
                entity.getReviewStatus(),
                format(entity.getCreatedAt()),
                format(entity.getUpdatedAt()));
    }

    private JobIntake.JdAuditEvent toAuditEvent(JdAuditEventEntity entity) {
        return new JobIntake.JdAuditEvent(
                entity.getId(),
                entity.getJobId(),
                entity.getAction(),
                actionLabel(entity.getAction()),
                entity.getPreviousStatus(),
                entity.getNextStatus(),
                entity.getActor(),
                entity.getActorRole(),
                jsonCodec.readList(entity.getChangedFieldsJson(), String.class),
                jsonCodec.read(entity.getBeforeSnapshotJson(), JobIntake.JdSnapshot.class),
                jsonCodec.read(entity.getAfterSnapshotJson(), JobIntake.JdSnapshot.class),
                entity.getHumanNote(),
                format(entity.getCreatedAt()));
    }

    private DemoAnalysis.EvidenceMatch toEvidenceMatch(JdEvidenceBindingEntity binding) {
        ResumeEvidenceEntity evidence = resumeEvidenceRepository.findById(binding.getEvidenceId()).orElse(null);
        List<String> sources = evidence == null ? List.of(binding.getEvidenceSource()) : jsonCodec.readList(evidence.getEvidenceSourcesJson(), String.class);
        return new DemoAnalysis.EvidenceMatch(
                binding.getRequirementLabel(),
                binding.getBindingReason(),
                evidence == null ? binding.getEvidenceId() : evidence.getProjectName(),
                evidence == null ? binding.getEvidenceId() : evidence.getProjectSlug(),
                binding.getEvidenceStrength(),
                binding.getBindingReason(),
                sources);
    }

    private List<DemoAnalysis.RequirementGroup> requirementsFrom(JdParseVersionEntity entity) {
        return jsonCodec.readList(entity.getExtractedRequirementsJson(), DemoAnalysis.RequirementGroup.class);
    }

    private List<DemoAnalysis.Requirement> flatten(List<DemoAnalysis.RequirementGroup> groups) {
        return groups.stream().flatMap(group -> group.items().stream()).toList();
    }

    private JobIntake.JdSnapshot snapshot(JobPostEntity job) {
        Optional<JdParseVersionEntity> latest = jdParseVersionRepository.findLatestByJobId(job.getId());
        List<JdEvidenceBindingEntity> bindings = latest.map(version -> jdEvidenceBindingRepository.findByParseVersionId(version.getId())).orElseGet(List::of);
        List<String> keywords = latest.map(version -> jsonCodec.readList(version.getKeywordsJson(), String.class)).orElseGet(List::of);
        List<String> riskTerms = latest.map(version -> jsonCodec.readList(version.getRiskTermsJson(), String.class)).orElseGet(List::of);
        return new JobIntake.JdSnapshot(
                job.getTitle(),
                job.getCompany(),
                job.getCity(),
                job.getSourceType(),
                job.getSourceNote(),
                status(job, latest.orElse(null), bindings.size()),
                latest.map(JdParseVersionEntity::getVersionNo).orElse(0),
                bindings.size(),
                keywords,
                riskTerms);
    }

    private JobIntake.JdSnapshot emptySnapshot() {
        return new JobIntake.JdSnapshot("", "", "", "", "", "None", 0, 0, List.of(), List.of());
    }

    private void writeAudit(
            String jobId,
            String action,
            String previousStatus,
            String nextStatus,
            String actor,
            String actorRole,
            List<String> changedFields,
            JobIntake.JdSnapshot beforeSnapshot,
            JobIntake.JdSnapshot afterSnapshot,
            String humanNote,
            LocalDateTime createdAt) {
        JdAuditEventEntity entity = new JdAuditEventEntity();
        entity.setId("jd-audit-" + shortId());
        entity.setJobId(jobId);
        entity.setAction(action);
        entity.setPreviousStatus(previousStatus);
        entity.setNextStatus(nextStatus);
        entity.setActor(actor);
        entity.setActorRole(actorRole);
        entity.setChangedFieldsJson(jsonCodec.write(changedFields));
        entity.setBeforeSnapshotJson(jsonCodec.write(beforeSnapshot));
        entity.setAfterSnapshotJson(jsonCodec.write(afterSnapshot));
        entity.setHumanNote(humanNote);
        entity.setCreatedAt(createdAt);
        jdAuditEventRepository.save(entity);
    }

    private String status(JobPostEntity job, JdParseVersionEntity latest, int bindingCount) {
        if (latest == null) {
            return "Draft";
        }
        List<String> riskTerms = jsonCodec.readList(latest.getRiskTermsJson(), String.class);
        if (!riskTerms.isEmpty()) {
            return "Review Required";
        }
        if (bindingCount > 0) {
            return "Bound";
        }
        return "Parsed";
    }

    private JobPostEntity getJob(String jobId) {
        return jobPostRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
    }

    private String sanitizeText(String value) {
        String text = valueOr(value, "");
        text = EMAIL_PATTERN.matcher(text).replaceAll("[redacted-email]");
        text = PHONE_PATTERN.matcher(text).replaceAll("[redacted-phone]");
        return text.trim();
    }

    private String normalizeSourceType(String sourceType) {
        if (sourceType == null || sourceType.isBlank()) {
            return "MANUAL_PASTE";
        }
        return "MANUAL_PASTE".equalsIgnoreCase(sourceType) ? "MANUAL_PASTE" : "MANUAL_PASTE";
    }

    private void updateIfPresent(String candidate, String current, String field, List<String> changedFields, java.util.function.Consumer<String> setter) {
        if (candidate != null && !candidate.isBlank() && !candidate.equals(current)) {
            setter.accept(candidate);
            changedFields.add(field);
        }
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String actor(String actor) {
        return valueOr(actor, DEFAULT_ACTOR);
    }

    private String actorRole(String actorRole) {
        return valueOr(actorRole, DEFAULT_ACTOR_ROLE);
    }

    private String note(String humanNote, String fallback) {
        return valueOr(humanNote, fallback);
    }

    private String first(List<String> items, String fallback) {
        return items.isEmpty() ? fallback : items.get(0);
    }

    private String format(LocalDateTime time) {
        return time == null ? "" : DISPLAY_TIME.format(time);
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String slug(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private String hash(String value) {
        return Integer.toHexString(value.hashCode()).replace("-", "n");
    }

    private boolean containsAny(List<String> values, String... expected) {
        for (String item : expected) {
            if (values.contains(item)) {
                return true;
            }
        }
        return false;
    }

    private List<String> keepKnown(List<String> values, String... expected) {
        List<String> kept = new ArrayList<>();
        for (String item : expected) {
            if (values.contains(item)) {
                kept.add(item);
            }
        }
        return kept.isEmpty() ? List.of(expected[0]) : kept;
    }

    private String actionLabel(String action) {
        return switch (action) {
            case "CREATE_JD" -> "创建 JD";
            case "UPDATE_JD" -> "更新 JD";
            case "PARSE_LOCAL_RULE" -> "local-rule 解析";
            case "CREATE_PARSE_VERSION" -> "创建解析版本";
            case "BIND_EVIDENCE" -> "绑定证据";
            case "REBIND_EVIDENCE" -> "重新绑定证据";
            case "ARCHIVE_JD" -> "归档 JD";
            case "RESTORE_JD" -> "恢复 JD";
            default -> action;
        };
    }

    private String boundaryNotice() {
        return "JD Intake 仅支持用户手动粘贴 JD；当前解析与证据绑定均为 local-rule，不接招聘平台 API，不爬虫，不保存真实隐私。";
    }

    private DemoAnalysis.ScoreBreakdown score() {
        return new DemoAnalysis.ScoreBreakdown(
                82, 100,
                List.of(
                        new DemoAnalysis.ScoreItem("skills", "技能命中", 36, 40, "关键词与岗位要求覆盖", "primary"),
                        new DemoAnalysis.ScoreItem("evidence", "项目证据", 28, 35, "证据深度、广度与可验证性", "positive"),
                        new DemoAnalysis.ScoreItem("risk", "经验风险", -4, 10, "交付经历仍需在面试中核验", "warning"),
                        new DemoAnalysis.ScoreItem("interview", "面试准备", 22, 25, "追问覆盖与 STAR 草稿完整度", "info")),
                "综合得分用于解释证据覆盖，不代表 Offer 或录取概率。");
    }

    private DemoAnalysis.InterviewPreparation interviewPreparation() {
        return new DemoAnalysis.InterviewPreparation(
                List.of(
                        "Spring Boot 项目中如何处理幂等、事务与并发问题？",
                        "MCP Tool Gateway 的鉴权、限流和失败回退如何设计？",
                        "RAG 召回与重排策略如何评估和优化？"),
                new DemoAnalysis.StarDraft(
                        "企业工单知识分散，检索和答复缺少证据引用。",
                        "构建可追踪的 RAG Copilot 演示链路，并保留人工复核。",
                        "设计检索、引用、Trace 和离线评估结构，补充页面与接口证据。",
                        "形成可运行作品集演示；效果数字仍需基于真实评测后填写。"),
                List.of("不要夸大模型能力与线上性能。", "不要把演示数据描述为真实客户数据。", "所有陈述需有 README、代码、截图或 Trace 支撑。"));
    }

    private List<DemoAnalysis.TimelineStep> timeline() {
        return List.of(
                new DemoAnalysis.TimelineStep("not-applied", "未投递", "current", "材料整理中", "—"),
                new DemoAnalysis.TimelineStep("contacted", "已沟通", "upcoming", "等待主动沟通", "—"),
                new DemoAnalysis.TimelineStep("resume-sent", "已发送简历", "upcoming", "尚未发送", "—"),
                new DemoAnalysis.TimelineStep("interview", "已约面试", "upcoming", "尚未约面", "—"),
                new DemoAnalysis.TimelineStep("following-up", "跟进中", "upcoming", "尚未进入", "—"));
    }

    private List<DemoAnalysis.RequirementGroup> fallbackRequirementGroups() {
        return parseLocalRule("熟悉 Spring Boot 开发，了解 AI 工具集成与集成方式；有 RAG、MCP 等相关实践优先。").requirementGroups();
    }

    private List<DemoAnalysis.EvidenceMatch> fallbackEvidenceMatches() {
        return List.of(
                new DemoAnalysis.EvidenceMatch("Spring Boot", "微服务框架与接口分层", "MCP Tool Gateway", "mcp-tool-gateway", "强", "项目包含 Spring Boot 工具网关、统一异常处理与接口契约。", List.of("README", "接口设计", "Trace")),
                new DemoAnalysis.EvidenceMatch("AI Workflow", "LLM 编排与工具链", "DevFlow Copilot", "devflow-copilot", "强", "展示从任务拆解到工具执行和 Human Review 的完整工作流。", List.of("README", "截图", "接口设计", "Trace")),
                new DemoAnalysis.EvidenceMatch("RAG / Knowledge", "知识检索与问答", "Enterprise Ticket RAG Copilot", "enterprise-ticket-rag", "中", "具备检索、引用与效果评估证据，仍需补充更完整离线评测。", List.of("README", "截图", "Trace", "效果评估")),
                new DemoAnalysis.EvidenceMatch("部署 / CI / 截图", "部署与持续集成", "Portfolio Hub", "portfolio-hub", "中", "提供构建流程、GitHub Actions 与真实页面截图。", List.of("部署记录", "GitHub Actions", "截图")));
    }

    private record ParsedJd(
            List<DemoAnalysis.RequirementGroup> requirementGroups,
            List<String> keywords,
            List<String> riskTerms,
            String sanitizedText) {
    }

    private record EvidenceCandidate(ResumeEvidenceEntity evidence, int score) {
    }
}
