package com.offerflow.copilot.persistence;

import java.time.LocalDateTime;
import java.util.List;

import com.offerflow.copilot.domain.ApplicationTracker;
import com.offerflow.copilot.domain.EvidenceLibrary;
import com.offerflow.copilot.domain.HumanReviewCenter;
import com.offerflow.copilot.domain.InterviewPrepDemo;
import com.offerflow.copilot.domain.MatchReportDemo;
import com.offerflow.copilot.domain.ProviderTraceCenter;
import com.offerflow.copilot.persistence.entity.ApplicationRecordEntity;
import com.offerflow.copilot.persistence.entity.HumanReviewAuditEventEntity;
import com.offerflow.copilot.persistence.entity.HumanReviewItemEntity;
import com.offerflow.copilot.persistence.entity.InterviewPrepEntity;
import com.offerflow.copilot.persistence.entity.JobPostEntity;
import com.offerflow.copilot.persistence.entity.MatchReportEntity;
import com.offerflow.copilot.persistence.entity.ProviderTraceRunEntity;
import com.offerflow.copilot.persistence.entity.ResumeEvidenceAuditEventEntity;
import com.offerflow.copilot.persistence.entity.ResumeEvidenceEntity;
import com.offerflow.copilot.persistence.entity.TraceStepEntity;
import com.offerflow.copilot.persistence.repository.ApplicationRecordRepository;
import com.offerflow.copilot.persistence.repository.HumanReviewAuditEventRepository;
import com.offerflow.copilot.persistence.repository.HumanReviewItemRepository;
import com.offerflow.copilot.persistence.repository.InterviewPrepRepository;
import com.offerflow.copilot.persistence.repository.JobPostRepository;
import com.offerflow.copilot.persistence.repository.MatchReportRepository;
import com.offerflow.copilot.persistence.repository.ProviderTraceRunRepository;
import com.offerflow.copilot.persistence.repository.ResumeEvidenceAuditEventRepository;
import com.offerflow.copilot.persistence.repository.ResumeEvidenceRepository;
import com.offerflow.copilot.persistence.repository.TraceStepRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersistenceSeedService implements ApplicationRunner {

    public static final String DEMO_JOB_ID = "job-java-ai-intern";
    public static final String DEMO_RUN_ID = "JD-20260701-143522-9E4D";

    private static final String MODE = "mock/local-rule";
    private static final List<String> RISK_TERMS = List.of(
            "生产级", "稳定接入", "真实用户", "提升 Offer 率", "保证通过", "自动投递", "实时面试辅助");

    private final boolean seedDemoData;
    private final JsonCodec jsonCodec;
    private final ResumeEvidenceRepository resumeEvidenceRepository;
    private final JobPostRepository jobPostRepository;
    private final MatchReportRepository matchReportRepository;
    private final InterviewPrepRepository interviewPrepRepository;
    private final ApplicationRecordRepository applicationRecordRepository;
    private final HumanReviewItemRepository humanReviewItemRepository;
    private final HumanReviewAuditEventRepository humanReviewAuditEventRepository;
    private final ResumeEvidenceAuditEventRepository resumeEvidenceAuditEventRepository;
    private final ProviderTraceRunRepository providerTraceRunRepository;
    private final TraceStepRepository traceStepRepository;

    public PersistenceSeedService(
            @Value("${offerflow.persistence.seed-demo-data:true}") boolean seedDemoData,
            JsonCodec jsonCodec,
            ResumeEvidenceRepository resumeEvidenceRepository,
            JobPostRepository jobPostRepository,
            MatchReportRepository matchReportRepository,
            InterviewPrepRepository interviewPrepRepository,
            ApplicationRecordRepository applicationRecordRepository,
            HumanReviewItemRepository humanReviewItemRepository,
            HumanReviewAuditEventRepository humanReviewAuditEventRepository,
            ResumeEvidenceAuditEventRepository resumeEvidenceAuditEventRepository,
            ProviderTraceRunRepository providerTraceRunRepository,
            TraceStepRepository traceStepRepository) {
        this.seedDemoData = seedDemoData;
        this.jsonCodec = jsonCodec;
        this.resumeEvidenceRepository = resumeEvidenceRepository;
        this.jobPostRepository = jobPostRepository;
        this.matchReportRepository = matchReportRepository;
        this.interviewPrepRepository = interviewPrepRepository;
        this.applicationRecordRepository = applicationRecordRepository;
        this.humanReviewItemRepository = humanReviewItemRepository;
        this.humanReviewAuditEventRepository = humanReviewAuditEventRepository;
        this.resumeEvidenceAuditEventRepository = resumeEvidenceAuditEventRepository;
        this.providerTraceRunRepository = providerTraceRunRepository;
        this.traceStepRepository = traceStepRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (seedDemoData) {
            seedIfEmpty();
        }
    }

    @Transactional
    public void seedIfEmpty() {
        seedJobPostIfEmpty();
        seedResumeEvidenceIfEmpty();
        seedResumeEvidenceAuditEventsIfEmpty();
        seedMatchReportIfEmpty();
        seedInterviewPrepIfEmpty();
        seedApplicationsIfEmpty();
        seedHumanReviewsIfEmpty();
        seedHumanReviewAuditEventsIfEmpty();
        seedProviderTraceIfEmpty();
    }

    private void seedJobPostIfEmpty() {
        if (jobPostRepository.count() > 0) {
            return;
        }
        JobPostEntity job = new JobPostEntity();
        job.setId(DEMO_JOB_ID);
        job.setTitle("Java 后端 / AI 应用开发实习生");
        job.setCompany("匿名演示公司");
        job.setCity("上海");
        job.setJdText("熟悉 Spring Boot 开发，了解 AI 工具集成与集成方式；有 RAG、MCP 等相关实践优先。");
        job.setSourceType("manual-demo");
        job.setSourceNote("脱敏 seed demo，不来自真实招聘平台抓取。");
        job.setSanitized(true);
        job.setCreatedAt(ts(2026, 7, 1, 10, 0));
        job.setUpdatedAt(ts(2026, 7, 1, 14, 30));
        jobPostRepository.save(job);
    }

    private void seedResumeEvidenceIfEmpty() {
        if (resumeEvidenceRepository.count() > 0) {
            return;
        }
        resumeEvidenceRepository.save(evidence(
                "evidence-mcp",
                "MCP Tool Gateway",
                "mcp-tool-gateway",
                "java",
                "Spring Boot 工具网关，展示统一注册、调用审计与 Trace 证据。",
                List.of("Java", "Spring Boot 3", "JSON-RPC", "Tool Registry", "Audit Log", "Trace Evidence"),
                List.of("Java 后端", "AI Agent 工具", "Trace / Human Review"),
                List.of("README", "接口设计", "后端测试", "Trace"),
                "强",
                List.of("Spring Boot", "MCP Tool", "接口设计", "审计与异常处理"),
                "Confirmed",
                detail(
                        List.of("Java", "Spring Boot 3", "JSON-RPC", "Tool Registry", "Audit Log", "Trace Evidence"),
                        List.of("Java 后端实习生", "AI Agent 工程实习生", "后端开发工程师"),
                        List.of("如何设计统一 Tool Gateway？", "如何处理 Tool 注册、权限与调用审计？", "如何实现异常处理与 Trace 追踪？"),
                        List.of("不声明已通过真实生产流量验证", "不将演示审计等同生产级安全", "性能数字需有独立压测证据"),
                        List.of(
                                step("README", "verified", "架构与边界说明"),
                                step("真实截图", "verified", "本地运行页面"),
                                step("后端测试", "verified", "接口契约测试"),
                                step("GitHub Actions", "partial", "构建流程证据"),
                                step("人工确认", "verified", "事实与来源已复核"))),
                "证据库仅保存匿名化作品集材料，不保存真实隐私。",
                ts(2026, 6, 28, 9, 0),
                ts(2026, 7, 1, 12, 0)));
        resumeEvidenceRepository.save(evidence(
                "evidence-devflow",
                "DevFlow Copilot",
                "devflow-copilot",
                "ai-app",
                "从任务拆解到工具执行的 AI Coding Workflow 演示。",
                List.of("AI Workflow", "Prompt", "Tool Calling", "Vue 3", "Human Review"),
                List.of("AI Coding", "AI 应用开发", "Trace / Human Review"),
                List.of("README", "截图", "接口设计", "GitHub Actions"),
                "强",
                List.of("AI Workflow", "Prompt Workflow", "工程交付", "协作开发"),
                "Confirmed",
                detail(
                        List.of("AI Workflow", "Prompt", "Tool Calling", "Vue 3", "Human Review"),
                        List.of("AI Coding 实习生", "AI 应用开发实习生", "全栈开发实习生"),
                        List.of("如何拆解 AI Coding 任务？", "如何防止工具执行失控？", "Human Review 放在哪个节点？"),
                        List.of("没有真实 Provider 稳定性结论", "任务结果仍需人工复核", "不把 demo 描述为生产平台"),
                        List.of(
                                step("README", "verified", "工作流说明"),
                                step("真实截图", "verified", "任务流页面"),
                                step("后端测试", "partial", "仅覆盖关键契约"),
                                step("GitHub Actions", "verified", "构建检查"),
                                step("人工确认", "verified", "表述已复核"))),
                "规则输出默认进入 Human Review。",
                ts(2026, 6, 29, 9, 0),
                ts(2026, 6, 30, 18, 0)));
        resumeEvidenceRepository.save(evidence(
                "evidence-rag",
                "Enterprise Ticket RAG Copilot",
                "enterprise-ticket-rag",
                "rag",
                "带引用、Trace 与 Human Review 的企业工单 RAG 演示。",
                List.of("RAG", "Knowledge Retrieval", "Citation", "Trace", "Provider fallback"),
                List.of("RAG / Knowledge", "AI 应用开发", "Trace / Human Review"),
                List.of("README", "Trace", "效果评估", "截图"),
                "中",
                List.of("RAG", "知识检索", "Provider fallback", "引用追踪"),
                "Needs review",
                detail(
                        List.of("RAG", "Knowledge Retrieval", "Citation", "Trace", "Provider fallback"),
                        List.of("AI 应用开发实习生", "RAG 工程实习生", "Java 后端实习生"),
                        List.of("如何评估检索质量？", "引用如何与答案绑定？", "Provider 失败时如何回退？"),
                        List.of("没有真实客户数据", "没有大规模线上效果数据", "评测样本是匿名化演示集"),
                        List.of(
                                step("README", "verified", "RAG 链路说明"),
                                step("真实截图", "verified", "检索与引用页面"),
                                step("后端测试", "partial", "核心链路测试"),
                                step("GitHub Actions", "missing", "待补充"),
                                step("人工确认", "partial", "效果描述待复核"))),
                "匿名化演示集不代表真实企业客户。",
                ts(2026, 6, 30, 9, 0),
                ts(2026, 6, 29, 18, 0)));
        resumeEvidenceRepository.save(evidence(
                "evidence-portfolio",
                "Portfolio Hub",
                "portfolio-hub",
                "frontend",
                "作品集聚合、真实页面截图与持续构建证据。",
                List.of("Vue 3", "TypeScript", "Vite", "Playwright", "GitHub Actions", "Deployment"),
                List.of("前端工程", "CI / 部署"),
                List.of("真实截图", "GitHub Actions", "部署记录", "README"),
                "中",
                List.of("Vue 3", "前端工程", "CI", "Deployment"),
                "Confirmed",
                detail(
                        List.of("Vue 3", "TypeScript", "Vite", "Playwright", "GitHub Actions"),
                        List.of("前端开发实习生", "AI Coding 实习生", "全栈开发实习生"),
                        List.of("如何保证作品集截图可信？", "CI 如何验证构建？", "响应式页面如何验收？"),
                        List.of("演示部署不代表生产 SLA", "截图只证明特定版本页面", "无真实用户或流量数据"),
                        List.of(
                                step("README", "verified", "运行说明"),
                                step("真实截图", "verified", "Playwright 生成"),
                                step("后端测试", "missing", "纯前端项目"),
                                step("GitHub Actions", "verified", "自动构建"),
                                step("人工确认", "verified", "来源已复核"))),
                "演示部署不等于生产环境经验。",
                ts(2026, 7, 1, 9, 0),
                ts(2026, 6, 28, 18, 0)));
    }

    private void seedResumeEvidenceAuditEventsIfEmpty() {
        if (resumeEvidenceAuditEventRepository.count() > 0) {
            return;
        }
        EvidenceLibrary.EvidenceSnapshot mcpSnapshot = evidenceSnapshot(
                "MCP Tool Gateway",
                "Spring Boot 工具网关，展示统一注册、调用审计与 Trace 证据。",
                List.of("Java", "Spring Boot 3", "JSON-RPC", "Tool Registry", "Audit Log", "Trace Evidence"),
                List.of("Java 后端", "AI Agent 工具", "Trace / Human Review"),
                List.of("README", "接口设计", "后端测试", "Trace"),
                "强",
                List.of("Spring Boot", "MCP Tool", "接口设计", "审计与异常处理"),
                "证据库仅保存匿名化作品集材料，不保存真实隐私。",
                List.of("不声称真实生产流量", "不等同生产级安全", "性能数字需独立压测证据"));
        resumeEvidenceAuditEventRepository.save(evidenceAuditEvent(
                "evidence-audit-mcp-confirm",
                "evidence-mcp",
                "CONFIRM",
                "Draft",
                "Confirmed",
                List.of("status"),
                mcpSnapshot,
                mcpSnapshot,
                "人工确认 MCP Tool Gateway 只作为作品集级证据使用。",
                ts(2026, 7, 1, 12, 0)));

        EvidenceLibrary.EvidenceSnapshot devflowSnapshot = evidenceSnapshot(
                "DevFlow Copilot",
                "从任务拆解到工具执行的 AI Coding Workflow 演示。",
                List.of("AI Workflow", "Prompt", "Tool Calling", "Vue 3", "Human Review"),
                List.of("AI Coding", "AI 应用开发", "Trace / Human Review"),
                List.of("README", "截图", "接口设计", "GitHub Actions"),
                "强",
                List.of("AI Workflow", "Prompt Workflow", "工程交付", "协作开发"),
                "规则输出默认进入 Human Review。",
                List.of("不声称真实 Provider 稳定性", "结果仍需人工复核", "不描述为生产平台"));
        resumeEvidenceAuditEventRepository.save(evidenceAuditEvent(
                "evidence-audit-devflow-confirm",
                "evidence-devflow",
                "CONFIRM",
                "Draft",
                "Confirmed",
                List.of("status"),
                devflowSnapshot,
                devflowSnapshot,
                "确认保留 workflow、fallback 与 Human Review 边界说明。",
                ts(2026, 6, 30, 18, 0)));

        EvidenceLibrary.EvidenceSnapshot ragSnapshot = evidenceSnapshot(
                "Enterprise Ticket RAG Copilot",
                "带引用、Trace 与 Human Review 的企业工单 RAG 演示。",
                List.of("RAG", "Knowledge Retrieval", "Citation", "Trace", "Provider fallback"),
                List.of("RAG / Knowledge", "AI 应用开发", "Trace / Human Review"),
                List.of("README", "Trace", "效果评估", "截图"),
                "中",
                List.of("RAG", "知识检索", "Provider fallback", "引用追踪"),
                "匿名化演示集不代表真实企业客户。",
                List.of("没有真实客户数据", "没有大规模线上效果数据", "评测样本是匿名化演示集"));
        resumeEvidenceAuditEventRepository.save(evidenceAuditEvent(
                "evidence-audit-rag-draft",
                "evidence-rag",
                "UPDATE_DRAFT",
                "Draft",
                "Draft",
                List.of("evidenceSources", "boundaryNote"),
                ragSnapshot,
                ragSnapshot,
                "RAG 证据仍需补充离线评测与边界说明，暂不确认为 Confirmed。",
                ts(2026, 6, 29, 18, 0)));

        EvidenceLibrary.EvidenceSnapshot portfolioSnapshot = evidenceSnapshot(
                "Portfolio Hub",
                "作品集聚合、真实页面截图与持续构建证据。",
                List.of("Vue 3", "TypeScript", "Vite", "Playwright", "GitHub Actions"),
                List.of("前端工程", "CI / 部署"),
                List.of("真实截图", "GitHub Actions", "部署记录", "README"),
                "中",
                List.of("Vue 3", "前端工程", "CI", "Deployment"),
                "演示部署不等于生产环境经验。",
                List.of("演示部署不代表生产 SLA", "截图只证明特定版本页面", "无真实用户或流量数据"));
        resumeEvidenceAuditEventRepository.save(evidenceAuditEvent(
                "evidence-audit-portfolio-confirm",
                "evidence-portfolio",
                "CONFIRM",
                "Draft",
                "Confirmed",
                List.of("status"),
                portfolioSnapshot,
                portfolioSnapshot,
                "确认只表达截图、CI 与作品集聚合证据，不声称生产流量。",
                ts(2026, 6, 28, 18, 0)));
    }

    private void seedMatchReportIfEmpty() {
        if (matchReportRepository.count() > 0) {
            return;
        }
        MatchReportEntity report = new MatchReportEntity();
        report.setId("match-java-ai-demo");
        report.setJobId(DEMO_JOB_ID);
        report.setMode(MODE);
        report.setScore(82);
        report.setSkillScore(36);
        report.setEvidenceScore(28);
        report.setRiskScore(-4);
        report.setInterviewScore(22);
        report.setRecommendedResume("Java 后端版 / AI Coding 版");
        report.setStatus("Draft，需要人工复核");
        report.setSummaryJson(jsonCodec.write(new MatchReportDemo.ReportSummary(
                "Java 后端 / AI 应用开发实习生",
                List.of("Java 后端版", "AI Coding 版"),
                82,
                100,
                "Draft，需要人工复核",
                "匹配得分只解释证据覆盖，不代表录取概率或招聘结果。")));
        report.setScoreJson(jsonCodec.write(new MatchReportDemo.ScoreBreakdown(
                List.of(
                        new MatchReportDemo.ScoreItem("skills", "技能命中", 36, 40, "Java / Spring Boot / AI Workflow 覆盖较完整", "primary"),
                        new MatchReportDemo.ScoreItem("evidence", "项目证据", 28, 35, "README、截图、Trace 与测试证据较清晰", "positive"),
                        new MatchReportDemo.ScoreItem("risk", "经验风险", -4, 10, "交付与生产环境经验需要降级描述", "warning"),
                        new MatchReportDemo.ScoreItem("interview", "面试准备", 22, 25, "追问方向明确，仍需人工整理 STAR 表达", "info")),
                "评分用于解释 JD 与证据的覆盖关系，不输出任何 Offer 或录取概率。")));
        report.setEvidenceSourcesJson(jsonCodec.write(List.of(
                new MatchReportDemo.EvidenceSource("Spring Boot", "MCP Tool Gateway", "强", List.of("README", "后端测试", "Trace"), "工具注册、接口契约、异常处理与调用审计证据完整。"),
                new MatchReportDemo.EvidenceSource("AI Workflow", "DevFlow Copilot", "强", List.of("README", "截图", "Trace"), "任务拆解、Provider fallback、Schema Validate 与 Human Review 链路清晰。"),
                new MatchReportDemo.EvidenceSource("RAG / Knowledge", "Enterprise Ticket RAG Copilot", "中", List.of("README", "截图", "Trace"), "具备引用与复核链路，但离线评估样本仍需补充。"),
                new MatchReportDemo.EvidenceSource("CI / Deployment", "Portfolio Hub", "中", List.of("GitHub Actions", "截图", "部署记录"), "有构建与截图证据，但不代表生产环境运维经验。"))));
        report.setSkillGapsJson(jsonCodec.write(List.of(
                new MatchReportDemo.SkillGap("Redis 深度使用", "中", "已有缓存理解，但缺少复杂一致性案例。", "补充缓存穿透、热点 key、过期策略与数据一致性复盘。"),
                new MatchReportDemo.SkillGap("分布式事务", "高", "当前证据集中在单体或轻量服务。", "准备本地事务、补偿事务、消息最终一致性对比说明。"),
                new MatchReportDemo.SkillGap("性能优化", "中", "缺少独立压测与指标记录。", "补充接口耗时、SQL 索引、缓存命中率等可验证材料。"),
                new MatchReportDemo.SkillGap("A/B Testing", "低", "岗位可能涉及实验思维，但当前项目证据较少。", "准备实验分组、指标选择和误差风险说明。"),
                new MatchReportDemo.SkillGap("生产环境经验不足", "高", "作品集证据不能表述为真实生产流量。", "统一改写为作品集级演示和可复核工程边界。"))));
        report.setRecommendedActionsJson(jsonCodec.write(List.of(
                new MatchReportDemo.RecommendedAction("建议投递", "证据覆盖 Java 后端与 AI 应用开发核心要求，但材料需先过 Human Review。", "high"),
                new MatchReportDemo.RecommendedAction("推荐使用 Java 后端版简历", "把 Spring Boot、接口设计、Trace Evidence 放在首屏项目经历。", "high"),
                new MatchReportDemo.RecommendedAction("准备关键追问", "重点准备 Spring Boot / Trace Evidence / Provider fallback 相关追问。", "medium"),
                new MatchReportDemo.RecommendedAction("收紧能力表述", "不要夸大真实模型能力，不把 local-rule fallback 包装成真实 LLM 能力。", "high"))));
        report.setTraceEvidenceJson(jsonCodec.write(List.of(
                new MatchReportDemo.TraceStep("JD 输入", "success", "手动录入演示 JD"),
                new MatchReportDemo.TraceStep("关键词解析", "success", "抽取 Java、Spring Boot、AI Workflow、RAG"),
                new MatchReportDemo.TraceStep("证据检索", "success", "匹配 4 个匿名化项目证据"),
                new MatchReportDemo.TraceStep("评分拆解", "success", "生成 4 个评分维度"),
                new MatchReportDemo.TraceStep("风险校验", "warning", "识别生产环境与模型能力表述风险"),
                new MatchReportDemo.TraceStep("Human Review", "current", "Draft 状态，等待人工确认"))));
        report.setDisclaimer("本报告由 seeded mock/local-rule 数据生成，所有结论需人工复核后才能复制或投递。");
        report.setCreatedAt(ts(2026, 7, 1, 14, 20));
        report.setUpdatedAt(ts(2026, 7, 1, 14, 35));
        matchReportRepository.save(report);
    }

    private void seedInterviewPrepIfEmpty() {
        if (interviewPrepRepository.count() > 0) {
            return;
        }
        InterviewPrepEntity prep = new InterviewPrepEntity();
        prep.setId("interview-java-ai-demo");
        prep.setJobId(DEMO_JOB_ID);
        prep.setMode(MODE);
        prep.setPositioningNotice("面试前准备与复盘，不是实时面试辅助工具。");
        prep.setFocusAreasJson(jsonCodec.write(List.of(
                new InterviewPrepDemo.FocusArea("Java / Spring Boot", "接口分层、异常处理、事务与测试", "MCP Tool Gateway"),
                new InterviewPrepDemo.FocusArea("AI Workflow", "任务拆解、工具调用、结构化输出", "DevFlow Copilot"),
                new InterviewPrepDemo.FocusArea("Trace Evidence", "每一步输入输出、证据和风险可回溯", "Provider Trace"),
                new InterviewPrepDemo.FocusArea("Human Review", "复制或投递前必须人工确认", "人工复核中心"),
                new InterviewPrepDemo.FocusArea("Provider fallback", "Provider 未配置或失败时明确降级", "local-rule fallback"),
                new InterviewPrepDemo.FocusArea("RAG 边界", "引用、评估和匿名化数据边界", "Enterprise Ticket RAG Copilot"))));
        prep.setQuestionsJson(jsonCodec.write(List.of(
                new InterviewPrepDemo.QuestionGroup("foundation", "技术基础", List.of("Spring Boot 项目中如何处理幂等、事务与异常？", "如果岗位要求 Redis，你会如何补强？")),
                new InterviewPrepDemo.QuestionGroup("project", "项目深挖", List.of("你在 MCP Tool Gateway 中如何设计 Trace Evidence？", "DevFlow Copilot 为什么需要 Human Review？")),
                new InterviewPrepDemo.QuestionGroup("ai-app", "AI 应用理解", List.of("local-rule fallback 和真实 LLM Provider 有什么区别？", "Ticket RAG 项目中 RAG 的边界是什么？")),
                new InterviewPrepDemo.QuestionGroup("engineering", "工程边界", List.of("Provider 未配置时如何避免伪装成成功？", "如何证明项目证据不是虚构的？")),
                new InterviewPrepDemo.QuestionGroup("collaboration", "协作与复盘", List.of("面试后你会如何复盘问题和补齐证据？", "当 AI 草稿存在夸大表述时你如何处理？")))));
        prep.setStarDraftJson(jsonCodec.write(new InterviewPrepDemo.StarDraft(
                "项目组合需要把 Java 后端能力和 AI Workflow 证据讲清楚，同时避免夸大外部模型能力。",
                "准备一个能解释 MCP Tool Gateway、Provider fallback 与 Human Review 的项目深挖回答。",
                "按 Trace Evidence 展示输入、规则处理、Schema Validate、Risk Guard 和人工复核链路。",
                "形成可复述的作品集级项目表达，面试后继续补充 Redis、分布式事务和性能证据。",
                "不要声称真实稳定 LLM、企业客户、真实用户或生产级能力。")));
        prep.setRiskNotesJson(jsonCodec.write(List.of(
                "不夸大生产级能力",
                "不声称真实稳定 LLM",
                "不虚构企业客户和真实用户",
                "不把 fallback 包装为模型能力")));
        prep.setReviewTimelineJson(jsonCodec.write(List.of(
                new InterviewPrepDemo.TimelineStep("prepare", "待准备", "current", "整理问题、证据与 STAR 草稿"),
                new InterviewPrepDemo.TimelineStep("answers-confirmed", "已确认答案", "upcoming", "人工确认后可用于面试前复习"),
                new InterviewPrepDemo.TimelineStep("mock-practice", "已模拟练习", "upcoming", "仅做面试前练习，不做实时辅助"),
                new InterviewPrepDemo.TimelineStep("interviewed", "已面试", "upcoming", "记录面试问题与反馈"),
                new InterviewPrepDemo.TimelineStep("reviewed", "已复盘", "upcoming", "补充证据缺口和后续行动"))));
        prep.setReviewStatus("Draft，需要人工复核");
        prep.setDisclaimer("所有内容仅用于面试前准备与复盘，不能用于实时面试辅助或作弊。");
        prep.setCreatedAt(ts(2026, 7, 1, 14, 25));
        prep.setUpdatedAt(ts(2026, 7, 1, 14, 36));
        interviewPrepRepository.save(prep);
    }

    private void seedApplicationsIfEmpty() {
        if (applicationRecordRepository.count() > 0) {
            return;
        }
        applicationRecordRepository.save(application(
                "app-java-ai",
                "科技创新公司",
                "Java AI 应用开发实习生",
                "上海",
                "Java 后端版",
                "手动记录：来自公开岗位描述摘要",
                "已发送简历",
                "等待反馈，准备 Spring Boot 与 Trace Evidence 追问",
                List.of(
                        new ApplicationTracker.CommunicationLog("app-java-ai", "初始沟通", "确认岗位关注 Java、Spring Boot 与 AI 应用项目表达。", "2026-07-01 10:12"),
                        new ApplicationTracker.CommunicationLog("app-java-ai", "已发送简历", "使用 Java 后端版简历，附作品集链接说明需人工复核。", "2026-07-01 14:30")),
                ts(2026, 7, 1, 10, 12),
                ts(2026, 7, 1, 14, 30)));
        applicationRecordRepository.save(application(
                "app-ai-coding",
                "AI 工具公司",
                "AI Coding 工具开发实习",
                "杭州",
                "AI Coding 版",
                "手动记录：作品集匹配度较高",
                "跟进中",
                "补充 Provider fallback 与 Human Review 说明",
                List.of(new ApplicationTracker.CommunicationLog("app-ai-coding", "等待反馈", "手动记录跟进，不抓取平台聊天。", "2026-07-01 13:20")),
                ts(2026, 7, 1, 13, 20),
                ts(2026, 7, 1, 13, 20)));
        applicationRecordRepository.save(application(
                "app-java-backend",
                "企业软件公司",
                "Java 后端实习",
                "北京",
                "Java 后端版",
                "手动记录：偏后端基础与 Redis",
                "已沟通",
                "补齐 Redis 与分布式事务准备材料",
                List.of(
                        new ApplicationTracker.CommunicationLog("app-java-backend", "面试安排", "待确认具体时间，准备 Redis 与事务追问。", "2026-06-30 18:10"),
                        new ApplicationTracker.CommunicationLog("app-java-backend", "面试复盘", "尚未面试，复盘区保留为空白待人工填写。", "2026-06-30 18:15")),
                ts(2026, 6, 30, 18, 10),
                ts(2026, 6, 30, 18, 10)));
    }

    private void seedHumanReviewsIfEmpty() {
        if (humanReviewItemRepository.count() > 0) {
            return;
        }
        humanReviewItemRepository.save(review(
                "review-match-java-ai", "match-report", "匹配报告：Java AI 应用开发实习生", "高", "匹配报告", "local-rule", "JD-042-REP-21F3", "Draft",
                "AI 建议候选材料匹配度高，已稳定接入真实用户场景，并能提升 Offer 率。建议突出 Spring Boot、RAG、MCP Tool Gateway 与自动投递效率。",
                "熟悉 Spring Boot 开发，了解 AI 工具集成与集成方式；希望工程化项目有可验证截图、Trace 与人工复核。",
                List.of(
                        reviewProject("MCP Tool Gateway", "Spring Boot 工具网关，包含注册、调用审计与 Trace 证据。", List.of("README", "后端测试", "Trace")),
                        reviewProject("Enterprise Ticket RAG Copilot", "带引用和复核链路的 RAG 演示，仍缺少大规模离线评测。", List.of("README", "截图", "Trace"))),
                "多处措辞把作品集演示写成真实用户和结果承诺，需要人工改写。",
                ts(2026, 7, 1, 14, 21)));
        humanReviewItemRepository.save(review(
                "review-star-mcp", "interview-prep", "STAR 回答草稿：MCP Tool Gateway 项目深挖", "中", "面试准备", "local-rule", "JD-042-STAR-9E4D", "Draft",
                "在 MCP Tool Gateway 项目中，我主导了后端开发与 AI 集成，构建了生产级服务架构，已在真实用户场景中稳定接入使用，显著提升 Offer 率。系统支持自动投递简历与实时面试辅助，并保证通过各类基础测评。",
                "熟悉 Spring Boot 开发，了解 AI 工具集成与集成方式；有 RAG、MCP 等相关实践优先。",
                List.of(
                        reviewProject("MCP Tool Gateway", "统一 Tool Registry、接口契约、异常处理与 Trace Evidence。", List.of("README", "后端测试", "Trace")),
                        reviewProject("DevFlow Copilot", "任务拆解、Provider fallback、Schema Validate、Human Review。", List.of("README", "截图", "Trace")),
                        reviewProject("Portfolio Hub", "作品集聚合与 Playwright 截图证据。", List.of("截图", "GitHub Actions"))),
                "未提供线上环境、真实用户或录取结果证据，STAR 回答必须降级为作品集级表述。",
                ts(2026, 7, 1, 14, 22)));
        humanReviewItemRepository.save(review(
                "review-opening-boss", "opening-message", "开场白建议：Boss 直聘沟通", "低", "投递跟踪", "OpenAI-compatible", "JD-042-OPEN-3C9D", "Draft",
                "您好，我正在找 Java 后端 / AI 应用开发实习机会。我的作品集包含 Spring Boot、RAG 与 Human Review 演示，能提供 README、截图和 Trace 证据，方便您快速核验。",
                "Java 后端实习生，要求 Spring Boot、接口开发、AI 应用实践；欢迎提供作品集或项目链接。",
                List.of(reviewProject("Portfolio Hub", "聚合项目截图、README 与构建记录。", List.of("截图", "README", "GitHub Actions"))),
                "开场白未承诺结果，但需要保持礼貌和证据链接边界。",
                ts(2026, 7, 1, 14, 23)));
        humanReviewItemRepository.save(review(
                "review-risk-model", "provider-risk", "风险提醒：真实模型能力表述", "高", "Provider 设置", "DeepSeek", "JD-042-RISK-7B1A", "Draft",
                "当前系统可稳定接入 DeepSeek 并提供生产级推理能力，适用于自动投递和实时面试辅助。",
                "Provider 设置仅展示 local-rule / OpenAI-compatible / DeepSeek 模式，不在本轮发起真实调用。",
                List.of(reviewProject("Provider fallback", "未配置真实 Provider，所有结果均来自 mock/local-rule。", List.of("Trace", "配置说明"))),
                "Provider 能力、自动投递和实时面试辅助均超出本轮边界。",
                ts(2026, 7, 1, 14, 24)));
        humanReviewItemRepository.save(review(
                "review-returned-devops", "match-report", "匹配报告：DevOps 工程师", "中", "匹配报告", "local-rule", "JD-042-REP-11A7", "Returned",
                "建议强调 CI / 部署经验，但不能把演示部署描述为生产 SLA。",
                "关注 CI、部署、可观测和基础设施经验。",
                List.of(reviewProject("Portfolio Hub", "GitHub Actions 和截图证据完整，生产运维证据不足。", List.of("GitHub Actions", "截图"))),
                "已退回等待补充边界说明。",
                ts(2026, 7, 1, 14, 25)));
        humanReviewItemRepository.save(review(
                "review-confirmed-resume", "resume-highlight", "简历亮点建议：多模态检索系统", "低", "简历证据库", "DeepSeek", "JD-039-HIL-5582", "Confirmed",
                "可以写为作品集级多模态检索演示，强调截图、README 与人工复核，不声明真实客户或生产流量。",
                "候选材料可展示检索、引用、评估与复核链路。",
                List.of(reviewProject("Enterprise Ticket RAG Copilot", "匿名化演示数据，支持引用与 Trace。", List.of("README", "Trace", "截图"))),
                "已人工确认，允许复制。",
                ts(2026, 7, 1, 14, 26)));
    }

    private void seedHumanReviewAuditEventsIfEmpty() {
        if (humanReviewAuditEventRepository.count() > 0) {
            return;
        }
        humanReviewAuditEventRepository.save(auditEvent(
                "audit-seed-star-risk-guard",
                "review-star-mcp",
                "AUTO_RISK_GUARD",
                "Draft",
                "Draft",
                "中",
                "中",
                "local-rule risk guard",
                "System guard",
                "命中“生产级、真实用户、提升 Offer 率、自动投递、实时面试辅助”等风险词，复制保持禁用。",
                "JD-042-STAR-9E4D",
                ts(2026, 7, 1, 14, 22)));
        humanReviewAuditEventRepository.save(auditEvent(
                "audit-seed-match-risk-guard",
                "review-match-java-ai",
                "AUTO_RISK_GUARD",
                "Draft",
                "Draft",
                "高",
                "高",
                "local-rule risk guard",
                "System guard",
                "匹配报告草稿包含真实用户和结果承诺类表述，必须进入人工复核。",
                "JD-042-REP-21F3",
                ts(2026, 7, 1, 14, 23)));
        humanReviewAuditEventRepository.save(auditEvent(
                "audit-seed-return-devops",
                "review-returned-devops",
                "RETURN",
                "Draft",
                "Returned",
                "中",
                "中",
                "demo-reviewer",
                "Human reviewer",
                "不能把演示部署描述为生产 SLA，退回补充边界说明。",
                "JD-042-REP-11A7",
                ts(2026, 7, 1, 14, 25)));
        humanReviewAuditEventRepository.save(auditEvent(
                "audit-seed-confirm-resume",
                "review-confirmed-resume",
                "CONFIRM",
                "Draft",
                "Confirmed",
                "低",
                "低",
                "demo-reviewer",
                "Human reviewer",
                "确认仅保留作品集级多模态检索演示，不声明真实客户或生产流量。",
                "JD-039-HIL-5582",
                ts(2026, 7, 1, 14, 26)));
    }

    private void seedProviderTraceIfEmpty() {
        if (providerTraceRunRepository.count() == 0) {
            ProviderTraceRunEntity run = new ProviderTraceRunEntity();
            run.setId("trace-run-java-ai-demo");
            run.setRunId(DEMO_RUN_ID);
            run.setJobTitle("Java AI 应用开发实习生");
            run.setProviderMode("local-rule fallback active");
            run.setFinalProvider("local-rule fallback");
            run.setModel("local-rule-engine v2.1");
            run.setFallbackReason("OpenAI-compatible 与 DeepSeek 均未配置；本轮边界禁止真实 Provider 调用。");
            run.setPromptVersion("v2.4.8");
            run.setSchemaVersion("v1.4.3");
            run.setRiskFlagsJson(jsonCodec.write(List.of("命中风险词：真实用户", "命中风险词：保证通过")));
            run.setEvidenceCount(12);
            run.setHumanReviewStatus("待人工确认");
            run.setDurationMs(842);
            run.setTraceHash("mock-a3f9d2c7b8e19f4d");
            run.setEvidenceDetailJson(jsonCodec.write(new ProviderTraceCenter.TraceEvidenceDetail(
                    "熟悉 Spring Boot 开发，了解 AI 工具集成与集成方式；有 RAG、MCP 等相关实践优先。",
                    List.of(
                            new ProviderTraceCenter.ResumeEvidenceRef("RES-001", "MCP Tool Gateway", "Spring Boot 工具网关，包含注册、调用审计与 Trace 证据。", List.of("README", "后端测试", "Trace")),
                            new ProviderTraceCenter.ResumeEvidenceRef("RES-012", "DevFlow Copilot", "任务拆解、Provider fallback、Schema Validate、Human Review。", List.of("README", "截图", "Trace")),
                            new ProviderTraceCenter.ResumeEvidenceRef("RES-022", "Enterprise Ticket RAG Copilot", "带引用和复核链路的 RAG 演示，仍缺少大规模离线评测。", List.of("README", "截图", "Trace"))),
                    "{\"provider\":\"local-rule fallback\",\"schema\":\"v1.4.3\",\"evidence_count\":12,\"copy_allowed\":false}",
                    "OpenAI-compatible 未配置；DeepSeek 禁用；未发起任何外部网络请求。",
                    "需要把结果承诺改成证据覆盖说明，所有输出进入 Human Review。")));
            run.setTechnicalTagsJson(jsonCodec.write(List.of(
                    "OpenAI-compatible", "DeepSeek", "local-rule fallback", "Trace Evidence", "Schema Validate",
                    "Risk Guard", "Human Review", "Spring Boot 3", "Vue 3", "TypeScript")));
            run.setCreatedAt(ts(2026, 7, 1, 14, 35));
            providerTraceRunRepository.save(run);
        }
        if (traceStepRepository.count() > 0) {
            return;
        }
        List<ProviderTraceCenter.PipelineStep> steps = List.of(
                new ProviderTraceCenter.PipelineStep("jd-input", "JD Input", "success", "62ms", "手动录入演示 JD", "抽取岗位、技能与风险要求", "JD-042"),
                new ProviderTraceCenter.PipelineStep("pii-redaction", "PII Redaction", "success", "34ms", "匿名化候选材料摘要", "未保存 PII 原文", "PII Guard"),
                new ProviderTraceCenter.PipelineStep("prompt-template", "Prompt Template", "success", "88ms", "Prompt v2.4.8", "生成结构化提示摘要", "Template Registry"),
                new ProviderTraceCenter.PipelineStep("provider-call", "Provider Call", "fallback", "0ms", "Provider 未配置", "未发起网络请求，回退 local-rule", "Provider Boundary"),
                new ProviderTraceCenter.PipelineStep("json-parse", "JSON Parse", "success", "41ms", "local-rule JSON snapshot", "解析为结构化对象", "Parser"),
                new ProviderTraceCenter.PipelineStep("schema-validate", "Schema Validate", "success", "55ms", "Schema v1.4.3", "36 条字段通过", "Schema Guard"),
                new ProviderTraceCenter.PipelineStep("risk-guard", "Risk Guard", "warning", "64ms", "禁用风险词扫描", "命中 2 项，复制仍禁用", "Risk Guard"),
                new ProviderTraceCenter.PipelineStep("evidence-binding", "Evidence Binding", "success", "97ms", "JD + Resume Evidence", "绑定 12 条证据", "RES-001 / RES-012"),
                new ProviderTraceCenter.PipelineStep("human-review", "Human Review", "warning", "401ms", "Draft 输出", "等待人工确认", "review-star-mcp"),
                new ProviderTraceCenter.PipelineStep("confirmed-result", "Confirmed Result", "warning", "0ms", "人工确认前", "尚无可复制结果", "copyAllowed=false"));
        for (int index = 0; index < steps.size(); index++) {
            ProviderTraceCenter.PipelineStep step = steps.get(index);
            TraceStepEntity entity = new TraceStepEntity();
            entity.setId("trace-step-" + (index + 1));
            entity.setRunId(DEMO_RUN_ID);
            entity.setStepOrder(index + 1);
            entity.setStepKey(step.key());
            entity.setStepName(step.label());
            entity.setStatus(step.status());
            entity.setDurationMs(parseMs(step.duration()));
            entity.setInputSummary(step.inputSummary());
            entity.setOutputSummary(step.outputSummary());
            entity.setEvidenceRefsJson(jsonCodec.write(List.of(step.linkedEvidence())));
            entity.setCreatedAt(ts(2026, 7, 1, 14, 35));
            traceStepRepository.save(entity);
        }
    }

    private ResumeEvidenceEntity evidence(
            String id,
            String name,
            String slug,
            String category,
            String summary,
            List<String> skills,
            List<String> abilityTags,
            List<String> sources,
            String strength,
            List<String> requirements,
            String reviewStatus,
            EvidenceLibrary.EvidenceDetail detail,
            String boundaryNote,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        ResumeEvidenceEntity entity = new ResumeEvidenceEntity();
        entity.setId(id);
        entity.setProjectName(name);
        entity.setProjectSlug(slug);
        entity.setCategory(category);
        entity.setSummary(summary);
        entity.setSkillsJson(jsonCodec.write(skills));
        entity.setAbilityTagsJson(jsonCodec.write(abilityTags));
        entity.setEvidenceSourcesJson(jsonCodec.write(sources));
        entity.setMatchableRequirementsJson(jsonCodec.write(requirements));
        entity.setDetailJson(jsonCodec.write(detail));
        entity.setStrength(strength);
        entity.setReviewStatus(reviewStatus);
        entity.setBoundaryNote(boundaryNote);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        return entity;
    }

    private ApplicationRecordEntity application(
            String id,
            String company,
            String roleTitle,
            String city,
            String resumeVersion,
            String sourceNote,
            String status,
            String nextAction,
            List<ApplicationTracker.CommunicationLog> timeline,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        ApplicationRecordEntity entity = new ApplicationRecordEntity();
        entity.setId(id);
        entity.setCompany(company);
        entity.setRoleTitle(roleTitle);
        entity.setCity(city);
        entity.setResumeVersion(resumeVersion);
        entity.setSourceNote(sourceNote);
        entity.setStatus(status);
        entity.setNextAction(nextAction);
        entity.setTimelineJson(jsonCodec.write(timeline));
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        return entity;
    }

    private HumanReviewItemEntity review(
            String id,
            String reviewType,
            String title,
            String riskLevel,
            String sourcePage,
            String providerMode,
            String traceId,
            String status,
            String originalText,
            String jdSnippet,
            List<HumanReviewCenter.ResumeProject> projects,
            String evidenceNote,
            LocalDateTime timestamp) {
        HumanReviewItemEntity entity = new HumanReviewItemEntity();
        entity.setId(id);
        entity.setReviewType(reviewType);
        entity.setTitle(title);
        entity.setRiskLevel(riskLevel);
        entity.setSourcePage(sourcePage);
        entity.setProviderMode(providerMode);
        entity.setTraceId(traceId);
        entity.setOriginalText(originalText);
        entity.setJdSnippet(jdSnippet);
        entity.setRiskTermsJson(jsonCodec.write(RISK_TERMS));
        entity.setEvidenceRefsJson(jsonCodec.write(projects));
        entity.setStatus(status);
        entity.setReviewer("local-reviewer");
        entity.setHumanNote("需要把“生产级”改成“作品集级”，不要声称真实用户。");
        entity.setEvidenceNote(evidenceNote);
        entity.setLastAction("Confirmed".equals(status) ? "人工已确认，可复制使用" : "等待人工确认");
        entity.setCreatedAt(timestamp);
        entity.setUpdatedAt(timestamp);
        return entity;
    }

    private HumanReviewAuditEventEntity auditEvent(
            String id,
            String reviewId,
            String action,
            String previousStatus,
            String nextStatus,
            String previousRiskLevel,
            String nextRiskLevel,
            String actor,
            String actorRole,
            String humanNote,
            String traceId,
            LocalDateTime createdAt) {
        HumanReviewAuditEventEntity entity = new HumanReviewAuditEventEntity();
        entity.setId(id);
        entity.setReviewId(reviewId);
        entity.setAction(action);
        entity.setPreviousStatus(previousStatus);
        entity.setNextStatus(nextStatus);
        entity.setPreviousRiskLevel(previousRiskLevel);
        entity.setNextRiskLevel(nextRiskLevel);
        entity.setActor(actor);
        entity.setActorRole(actorRole);
        entity.setHumanNote(humanNote);
        entity.setTraceId(traceId);
        entity.setTraceHash("audit-" + Integer.toHexString((traceId + ":" + reviewId).hashCode()));
        entity.setCreatedAt(createdAt);
        return entity;
    }

    private ResumeEvidenceAuditEventEntity evidenceAuditEvent(
            String id,
            String evidenceId,
            String action,
            String previousStatus,
            String nextStatus,
            List<String> changedFields,
            EvidenceLibrary.EvidenceSnapshot beforeSnapshot,
            EvidenceLibrary.EvidenceSnapshot afterSnapshot,
            String humanNote,
            LocalDateTime createdAt) {
        ResumeEvidenceAuditEventEntity entity = new ResumeEvidenceAuditEventEntity();
        entity.setId(id);
        entity.setEvidenceId(evidenceId);
        entity.setAction(action);
        entity.setPreviousStatus(previousStatus);
        entity.setNextStatus(nextStatus);
        entity.setActor("demo-evidence-editor");
        entity.setActorRole("Evidence reviewer");
        entity.setChangedFieldsJson(jsonCodec.write(changedFields));
        entity.setBeforeSnapshotJson(jsonCodec.write(beforeSnapshot));
        entity.setAfterSnapshotJson(jsonCodec.write(afterSnapshot));
        entity.setHumanNote(humanNote);
        entity.setCreatedAt(createdAt);
        return entity;
    }

    private EvidenceLibrary.EvidenceSnapshot evidenceSnapshot(
            String projectName,
            String summary,
            List<String> skills,
            List<String> abilityTags,
            List<String> evidenceSources,
            String strength,
            List<String> matchableRequirements,
            String boundaryNote,
            List<String> riskBoundaries) {
        return new EvidenceLibrary.EvidenceSnapshot(
                projectName,
                summary,
                skills,
                abilityTags,
                evidenceSources,
                strength,
                matchableRequirements,
                boundaryNote,
                riskBoundaries);
    }

    private EvidenceLibrary.EvidenceDetail detail(
            List<String> skills,
            List<String> roles,
            List<String> answers,
            List<String> risks,
            List<EvidenceLibrary.SourceStep> sourceChain) {
        return new EvidenceLibrary.EvidenceDetail(skills, roles, answers, risks, sourceChain);
    }

    private EvidenceLibrary.SourceStep step(String label, String status, String note) {
        return new EvidenceLibrary.SourceStep(label, status, note);
    }

    private HumanReviewCenter.ResumeProject reviewProject(String name, String excerpt, List<String> sourceTypes) {
        return new HumanReviewCenter.ResumeProject(name, excerpt, sourceTypes);
    }

    private LocalDateTime ts(int year, int month, int day, int hour, int minute) {
        return LocalDateTime.of(year, month, day, hour, minute);
    }

    private int parseMs(String value) {
        return Integer.parseInt(value.replace("ms", ""));
    }
}
