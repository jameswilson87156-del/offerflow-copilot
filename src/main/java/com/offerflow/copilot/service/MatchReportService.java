package com.offerflow.copilot.service;

import java.util.List;

import com.offerflow.copilot.domain.MatchReportDemo;
import org.springframework.stereotype.Service;

@Service
public class MatchReportService {

    public MatchReportDemo getDemoReport() {
        return new MatchReportDemo(
                "mock/local-rule",
                new MatchReportDemo.ReportSummary(
                        "Java 后端 / AI 应用开发实习生",
                        List.of("Java 后端版", "AI Coding 版"),
                        82,
                        100,
                        "Draft，需要人工复核",
                        "匹配得分只解释证据覆盖，不代表录取概率或招聘结果。"),
                score(),
                evidenceSources(),
                skillGaps(),
                recommendedActions(),
                traceEvidence(),
                "本报告由 mock/local-rule 生成，所有结论需人工复核后才能复制或投递。");
    }

    private MatchReportDemo.ScoreBreakdown score() {
        return new MatchReportDemo.ScoreBreakdown(
                List.of(
                        scoreItem("skills", "技能命中", 36, 40, "Java / Spring Boot / AI Workflow 覆盖较完整", "primary"),
                        scoreItem("evidence", "项目证据", 28, 35, "README、截图、Trace 与测试证据较清晰", "positive"),
                        scoreItem("risk", "经验风险", -4, 10, "交付与生产环境经验需要降级描述", "warning"),
                        scoreItem("interview", "面试准备", 22, 25, "追问方向明确，仍需人工整理 STAR 表达", "info")),
                "评分用于解释 JD 与证据的覆盖关系，不输出任何 Offer 或录取概率。");
    }

    private List<MatchReportDemo.EvidenceSource> evidenceSources() {
        return List.of(
                evidence("Spring Boot", "MCP Tool Gateway", "强", List.of("README", "后端测试", "Trace"), "工具注册、接口契约、异常处理与调用审计证据完整。"),
                evidence("AI Workflow", "DevFlow Copilot", "强", List.of("README", "截图", "Trace"), "任务拆解、Provider fallback、Schema Validate 与 Human Review 链路清晰。"),
                evidence("RAG / Knowledge", "Enterprise Ticket RAG Copilot", "中", List.of("README", "截图", "Trace"), "具备引用与复核链路，但离线评估样本仍需补充。"),
                evidence("CI / Deployment", "Portfolio Hub", "中", List.of("GitHub Actions", "截图", "部署记录"), "有构建与截图证据，但不代表生产环境运维经验。"));
    }

    private List<MatchReportDemo.SkillGap> skillGaps() {
        return List.of(
                gap("Redis 深度使用", "中", "已有缓存理解，但缺少复杂一致性案例。", "补充缓存穿透、热点 key、过期策略与数据一致性复盘。"),
                gap("分布式事务", "高", "当前证据集中在单体或轻量服务。", "准备本地事务、补偿事务、消息最终一致性对比说明。"),
                gap("性能优化", "中", "缺少独立压测与指标记录。", "补充接口耗时、SQL 索引、缓存命中率等可验证材料。"),
                gap("A/B Testing", "低", "岗位可能涉及实验思维，但当前项目证据较少。", "准备实验分组、指标选择和误差风险说明。"),
                gap("生产环境经验不足", "高", "作品集证据不能表述为真实生产流量。", "统一改写为作品集级演示和可复核工程边界。"));
    }

    private List<MatchReportDemo.RecommendedAction> recommendedActions() {
        return List.of(
                action("建议投递", "证据覆盖 Java 后端与 AI 应用开发核心要求，但材料需先过 Human Review。", "high"),
                action("推荐使用 Java 后端版简历", "把 Spring Boot、接口设计、Trace Evidence 放在首屏项目经历。", "high"),
                action("准备关键追问", "重点准备 Spring Boot / Trace Evidence / Provider fallback 相关追问。", "medium"),
                action("收紧能力表述", "不要夸大真实模型能力，不把 local-rule fallback 包装成真实 LLM 能力。", "high"));
    }

    private List<MatchReportDemo.TraceStep> traceEvidence() {
        return List.of(
                step("JD 输入", "success", "手动录入演示 JD"),
                step("关键词解析", "success", "抽取 Java、Spring Boot、AI Workflow、RAG"),
                step("证据检索", "success", "匹配 4 个匿名化项目证据"),
                step("评分拆解", "success", "生成 4 个评分维度"),
                step("风险校验", "warning", "识别生产环境与模型能力表述风险"),
                step("Human Review", "current", "Draft 状态，等待人工确认"));
    }

    private MatchReportDemo.ScoreItem scoreItem(String key, String label, int value, int maximum, String detail, String tone) {
        return new MatchReportDemo.ScoreItem(key, label, value, maximum, detail, tone);
    }

    private MatchReportDemo.EvidenceSource evidence(String requirement, String project, String strength, List<String> evidenceTypes, String rationale) {
        return new MatchReportDemo.EvidenceSource(requirement, project, strength, evidenceTypes, rationale);
    }

    private MatchReportDemo.SkillGap gap(String skill, String severity, String reason, String nextAction) {
        return new MatchReportDemo.SkillGap(skill, severity, reason, nextAction);
    }

    private MatchReportDemo.RecommendedAction action(String title, String detail, String priority) {
        return new MatchReportDemo.RecommendedAction(title, detail, priority);
    }

    private MatchReportDemo.TraceStep step(String label, String status, String detail) {
        return new MatchReportDemo.TraceStep(label, status, detail);
    }
}
