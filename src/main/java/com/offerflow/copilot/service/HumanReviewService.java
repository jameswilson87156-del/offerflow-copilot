package com.offerflow.copilot.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.offerflow.copilot.domain.HumanReviewCenter;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class HumanReviewService {

    private static final List<String> RISK_TERMS = List.of(
            "生产级",
            "稳定接入",
            "真实用户",
            "提升 Offer 率",
            "保证通过",
            "自动投递",
            "实时面试辅助");

    private static final List<String> COMPLIANCE_PRINCIPLES = List.of(
            "不输出 Offer 概率",
            "不做实时面试作弊",
            "不虚构真实客户",
            "不保存真实隐私",
            "不夸大模型能力");

    private final Map<String, HumanReviewCenter.ReviewDetail> reviews = new LinkedHashMap<>();

    public HumanReviewService() {
        seed();
    }

    public HumanReviewCenter listReviews() {
        List<HumanReviewCenter.ReviewSummary> items = reviews.values().stream()
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
        HumanReviewCenter.ReviewDetail review = reviews.get(id);
        if (review == null) {
            throw new ResponseStatusException(NOT_FOUND, "Review item not found");
        }
        return review;
    }

    public HumanReviewCenter.ReviewDetail confirm(String id, String note) {
        HumanReviewCenter.ReviewDetail review = getReview(id);
        return update(review, review.riskLevel(), "Confirmed", note, true, "人工已确认，可复制使用");
    }

    public HumanReviewCenter.ReviewDetail returnForRevision(String id, String note) {
        HumanReviewCenter.ReviewDetail review = getReview(id);
        return update(review, review.riskLevel(), "Returned", note, false, "已退回修改，复制仍被禁用");
    }

    public HumanReviewCenter.ReviewDetail flagRisk(String id, String note) {
        HumanReviewCenter.ReviewDetail review = getReview(id);
        return update(review, "高风险", "Draft", note, false, "已标记风险，等待重新生成或人工改写");
    }

    private HumanReviewCenter.ReviewDetail update(
            HumanReviewCenter.ReviewDetail review,
            String riskLevel,
            String status,
            String note,
            boolean copyAllowed,
            String lastAction) {
        String humanNote = (note == null || note.isBlank()) ? review.humanNote() : note;
        HumanReviewCenter.ReviewDetail updated = new HumanReviewCenter.ReviewDetail(
                review.id(),
                review.group(),
                review.title(),
                review.sourcePage(),
                riskLevel,
                review.providerMode(),
                review.traceId(),
                status,
                "2026-07-01 14:35",
                review.reviewer(),
                humanNote,
                review.aiSuggestion(),
                review.evidence(),
                review.riskTerms(),
                review.traceEvidence(),
                review.compliancePrinciples(),
                copyAllowed,
                lastAction);
        reviews.put(review.id(), updated);
        return updated;
    }

    private HumanReviewCenter.ReviewSummary summary(HumanReviewCenter.ReviewDetail detail) {
        return new HumanReviewCenter.ReviewSummary(
                detail.id(),
                detail.group(),
                detail.title(),
                detail.sourcePage(),
                detail.riskLevel(),
                detail.providerMode(),
                detail.traceId(),
                detail.status(),
                detail.updatedAt());
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

    private void seed() {
        reviews.put("review-match-java-ai", detail(
                "review-match-java-ai",
                "high-risk",
                "匹配报告：Java AI 应用开发实习生",
                "匹配报告",
                "高",
                "local-rule",
                "JD-042-REP-21F3",
                "Draft",
                "AI 建议候选材料匹配度高，已稳定接入真实用户场景，并能提升 Offer 率。建议突出 Spring Boot、RAG、MCP Tool Gateway 与自动投递效率。",
                "熟悉 Spring Boot 开发，了解 AI 工具集成与集成方式；希望工程化项目有可验证截图、Trace 与人工复核。",
                List.of(
                        project("MCP Tool Gateway", "Spring Boot 工具网关，包含注册、调用审计与 Trace 证据。", List.of("README", "后端测试", "Trace")),
                        project("Enterprise Ticket RAG Copilot", "带引用和复核链路的 RAG 演示，仍缺少大规模离线评测。", List.of("README", "截图", "Trace"))),
                "多处措辞把作品集演示写成真实用户和结果承诺，需要人工改写。"));
        reviews.put("review-star-mcp", detail(
                "review-star-mcp",
                "pending",
                "STAR 回答草稿：MCP Tool Gateway 项目深挖",
                "面试准备",
                "中",
                "local-rule",
                "JD-042-STAR-9E4D",
                "Draft",
                "在 MCP Tool Gateway 项目中，我主导了后端开发与 AI 集成，构建了生产级服务架构，已在真实用户场景中稳定接入使用，显著提升 Offer 率。系统支持自动投递简历与实时面试辅助，并保证通过各类基础测评。",
                "熟悉 Spring Boot 开发，了解 AI 工具集成与集成方式；有 RAG、MCP 等相关实践优先。",
                List.of(
                        project("MCP Tool Gateway", "统一 Tool Registry、接口契约、异常处理与 Trace Evidence。", List.of("README", "后端测试", "Trace")),
                        project("DevFlow Copilot", "任务拆解、Provider fallback、Schema Validate、Human Review。", List.of("README", "截图", "Trace")),
                        project("Portfolio Hub", "作品集聚合与 Playwright 截图证据。", List.of("截图", "GitHub Actions"))),
                "未提供线上环境、真实用户或录取结果证据，STAR 回答必须降级为作品集级表述。"));
        reviews.put("review-opening-boss", detail(
                "review-opening-boss",
                "pending",
                "开场白建议：Boss 直聘沟通",
                "投递跟踪",
                "低",
                "OpenAI-compatible",
                "JD-042-OPEN-3C9D",
                "Draft",
                "您好，我正在找 Java 后端 / AI 应用开发实习机会。我的作品集包含 Spring Boot、RAG 与 Human Review 演示，能提供 README、截图和 Trace 证据，方便您快速核验。",
                "Java 后端实习生，要求 Spring Boot、接口开发、AI 应用实践；欢迎提供作品集或项目链接。",
                List.of(project("Portfolio Hub", "聚合项目截图、README 与构建记录。", List.of("截图", "README", "GitHub Actions"))),
                "开场白未承诺结果，但需要保持礼貌和证据链接边界。"));
        reviews.put("review-risk-model", detail(
                "review-risk-model",
                "high-risk",
                "风险提醒：真实模型能力表述",
                "Provider 设置",
                "高",
                "DeepSeek",
                "JD-042-RISK-7B1A",
                "Draft",
                "当前系统可稳定接入 DeepSeek 并提供生产级推理能力，适用于自动投递和实时面试辅助。",
                "Provider 设置仅展示 local-rule / OpenAI-compatible / DeepSeek 模式，不在本轮发起真实调用。",
                List.of(project("Provider fallback", "未配置真实 Provider，所有结果均来自 mock/local-rule。", List.of("Trace", "配置说明"))),
                "Provider 能力、自动投递和实时面试辅助均超出本轮边界。"));
        reviews.put("review-returned-devops", detail(
                "review-returned-devops",
                "returned",
                "匹配报告：DevOps 工程师",
                "匹配报告",
                "中",
                "local-rule",
                "JD-042-REP-11A7",
                "Returned",
                "建议强调 CI / 部署经验，但不能把演示部署描述为生产 SLA。",
                "关注 CI、部署、可观测和基础设施经验。",
                List.of(project("Portfolio Hub", "GitHub Actions 和截图证据完整，生产运维证据不足。", List.of("GitHub Actions", "截图"))),
                "已退回等待补充边界说明。"));
        reviews.put("review-confirmed-resume", detail(
                "review-confirmed-resume",
                "confirmed",
                "简历亮点建议：多模态检索系统",
                "简历证据库",
                "低",
                "DeepSeek",
                "JD-039-HIL-5582",
                "Confirmed",
                "可以写为作品集级多模态检索演示，强调截图、README 与人工复核，不声明真实客户或生产流量。",
                "候选材料可展示检索、引用、评估与复核链路。",
                List.of(project("Enterprise Ticket RAG Copilot", "匿名化演示数据，支持引用与 Trace。", List.of("README", "Trace", "截图"))),
                "已人工确认，允许复制。"));
    }

    private HumanReviewCenter.ReviewDetail detail(
            String id,
            String group,
            String title,
            String sourcePage,
            String riskLevel,
            String providerMode,
            String traceId,
            String status,
            String aiSuggestion,
            String jdSnippet,
            List<HumanReviewCenter.ResumeProject> projects,
            String evidenceNote) {
        return new HumanReviewCenter.ReviewDetail(
                id,
                group,
                title,
                sourcePage,
                riskLevel,
                providerMode,
                traceId,
                status,
                "2026-07-01 14:21",
                "you@example.com",
                "需要把“生产级”改成“作品集级”，不要声称真实用户。",
                aiSuggestion,
                new HumanReviewCenter.ReviewEvidence(jdSnippet, projects, evidenceNote),
                RISK_TERMS,
                trace(),
                COMPLIANCE_PRINCIPLES,
                "Confirmed".equals(status),
                "等待人工确认");
    }

    private HumanReviewCenter.ResumeProject project(String name, String excerpt, List<String> sourceTypes) {
        return new HumanReviewCenter.ResumeProject(name, excerpt, sourceTypes);
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
