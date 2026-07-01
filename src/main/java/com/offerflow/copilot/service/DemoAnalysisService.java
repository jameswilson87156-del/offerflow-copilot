package com.offerflow.copilot.service;

import java.util.List;

import com.offerflow.copilot.domain.DemoAnalysis;
import org.springframework.stereotype.Service;

@Service
public class DemoAnalysisService {

    public DemoAnalysis getDemoAnalysis() {
        return new DemoAnalysis(
                new DemoAnalysis.Job("Java 后端 / AI 应用开发实习生", "示例科技", "手动录入的演示 JD", "2026-07-01 14:20"),
                requirementGroups(),
                evidenceMatches(),
                score(),
                interviewPreparation(),
                new DemoAnalysis.HumanReview(
                        "Draft", "Pending", false,
                        "AI / 规则生成内容必须人工确认事实和措辞后才能复制使用。"),
                timeline(),
                List.of(
                        new DemoAnalysis.ResumeVersion("Java 后端版", "突出 Spring Boot、接口设计与工程交付", true),
                        new DemoAnalysis.ResumeVersion("AI Coding 版", "突出 AI Workflow、RAG 与工具编排", true)),
                List.of("Spring Boot 3", "Vue 3", "TypeScript", "MySQL", "Redis", "RAG", "MCP", "GitHub Actions", "Human Review"),
                "这是 mock/local-rule 演示结果，不是录取概率或真实招聘结论。所有内容需要人工复核。") ;
    }

    private List<DemoAnalysis.RequirementGroup> requirementGroups() {
        return List.of(
                new DemoAnalysis.RequirementGroup("core", "核心要求", "primary", List.of(
                        new DemoAnalysis.Requirement("req-1", "Java / Spring Boot", "熟悉 Spring Boot 3，具备 REST API 与微服务基础", "核心", List.of("Java", "Spring Boot")),
                        new DemoAnalysis.Requirement("req-2", "MySQL / Redis", "理解索引、事务、缓存与常见一致性问题", "核心", List.of("MySQL", "Redis")))),
                new DemoAnalysis.RequirementGroup("bonus", "加分要求", "positive", List.of(
                        new DemoAnalysis.Requirement("req-3", "AI 应用开发", "有 RAG、LLM 集成或知识检索实践", "加分", List.of("RAG", "LLM")),
                        new DemoAnalysis.Requirement("req-4", "Prompt Workflow", "能设计可追踪的 Prompt 流程与工具调用链", "加分", List.of("Prompt", "Workflow")))),
                new DemoAnalysis.RequirementGroup("risk", "风险要求", "warning", List.of(
                        new DemoAnalysis.Requirement("req-5", "项目落地经验", "希望有从 0 到 1 交付、部署与复盘经历", "风险", List.of("部署", "交付")))));
    }

    private List<DemoAnalysis.EvidenceMatch> evidenceMatches() {
        return List.of(
                new DemoAnalysis.EvidenceMatch(
                        "Spring Boot", "微服务框架与接口分层", "MCP Tool Gateway", "mcp-tool-gateway", "强",
                        "项目包含 Spring Boot 工具网关、统一异常处理与接口契约。",
                        List.of("README", "接口设计", "Trace")),
                new DemoAnalysis.EvidenceMatch(
                        "AI Workflow", "LLM 编排与工具链", "DevFlow Copilot", "devflow-copilot", "强",
                        "展示从任务拆解到工具执行和 Human Review 的完整工作流。",
                        List.of("README", "截图", "接口设计", "Trace")),
                new DemoAnalysis.EvidenceMatch(
                        "RAG / Knowledge", "知识检索与问答", "Enterprise Ticket RAG Copilot", "enterprise-ticket-rag", "中",
                        "具备检索、引用与效果评估证据，仍需补充更完整离线评测。",
                        List.of("README", "截图", "Trace", "效果评估")),
                new DemoAnalysis.EvidenceMatch(
                        "部署 / CI / 截图", "部署与持续集成", "Portfolio Hub", "portfolio-hub", "中",
                        "提供构建流程、GitHub Actions 与真实页面截图。",
                        List.of("部署记录", "GitHub Actions", "截图")));
    }

    private DemoAnalysis.ScoreBreakdown score() {
        return new DemoAnalysis.ScoreBreakdown(
                82, 100,
                List.of(
                        new DemoAnalysis.ScoreItem("skills", "技能命中", 36, 40, "关键词与岗位要求覆盖", "primary"),
                        new DemoAnalysis.ScoreItem("evidence", "项目证据", 28, 35, "证据深度、广度与可验证性", "positive"),
                        new DemoAnalysis.ScoreItem("risk", "经验风险", -4, 10, "交付经历仍需在面试中核验", "warning"),
                        new DemoAnalysis.ScoreItem("interview", "面试准备", 22, 25, "追问覆盖与 STAR 草稿完整度", "info")),
                "综合得分用于解释证据覆盖，不代表 Offer 或录取概率。") ;
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
                List.of(
                        "不要夸大模型能力与线上性能。",
                        "不要把演示数据描述为真实客户数据。",
                        "所有陈述需有 README、代码、截图或 Trace 支撑。"));
    }

    private List<DemoAnalysis.TimelineStep> timeline() {
        return List.of(
                new DemoAnalysis.TimelineStep("not-applied", "未投递", "current", "材料整理中", "—"),
                new DemoAnalysis.TimelineStep("contacted", "已沟通", "upcoming", "等待主动沟通", "—"),
                new DemoAnalysis.TimelineStep("resume-sent", "已发送简历", "upcoming", "尚未发送", "—"),
                new DemoAnalysis.TimelineStep("interview", "已约面试", "upcoming", "尚未约面", "—"),
                new DemoAnalysis.TimelineStep("following-up", "跟进中", "upcoming", "尚未进入", "—"));
    }
}
