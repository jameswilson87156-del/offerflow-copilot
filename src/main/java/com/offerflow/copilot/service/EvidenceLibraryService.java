package com.offerflow.copilot.service;

import java.util.List;

import com.offerflow.copilot.domain.EvidenceCoverage;
import com.offerflow.copilot.domain.EvidenceLibrary;
import org.springframework.stereotype.Service;

@Service
public class EvidenceLibraryService {

    public EvidenceLibrary getLibrary() {
        return new EvidenceLibrary(
                "mock/local-rule",
                4,
                categories(),
                List.of(mcpGateway(), devFlow(), ticketRag(), portfolioHub()),
                "证据库仅使用匿名化演示项目数据，不代表真实客户、用户、线上流量或生产级效果。");
    }

    public EvidenceCoverage getCoverage() {
        return new EvidenceCoverage(
                "mock/local-rule",
                List.of(
                        coverage("Java", "强支撑", 92, List.of("MCP Tool Gateway", "Enterprise Ticket RAG Copilot"), "补充复杂并发案例"),
                        coverage("Spring Boot", "强支撑", 94, List.of("MCP Tool Gateway", "Enterprise Ticket RAG Copilot"), "补充事务压测记录"),
                        coverage("Vue 3", "中支撑", 68, List.of("Portfolio Hub", "DevFlow Copilot"), "缺少组件测试证据"),
                        coverage("AI Workflow", "强支撑", 88, List.of("DevFlow Copilot", "MCP Tool Gateway"), "需人工核验编排边界"),
                        coverage("RAG", "中支撑", 72, List.of("Enterprise Ticket RAG Copilot"), "缺少大规模离线评测"),
                        coverage("Trace", "强支撑", 86, List.of("MCP Tool Gateway", "DevFlow Copilot", "Enterprise Ticket RAG Copilot"), "补充失败链路样例"),
                        coverage("CI", "中支撑", 74, List.of("Portfolio Hub", "DevFlow Copilot"), "缺少发布审批记录"),
                        coverage("Deployment", "弱支撑", 46, List.of("Portfolio Hub"), "仅有演示部署，不代表生产运维")),
                "覆盖度由确定性 local-rule 根据证据类型与人工确认状态计算，不是岗位录取概率。保持空缺比虚构证据更重要。") ;
    }

    private List<EvidenceLibrary.EvidenceCategory> categories() {
        return List.of(
                new EvidenceLibrary.EvidenceCategory("all", "全部证据", 4),
                new EvidenceLibrary.EvidenceCategory("java", "Java 后端", 2),
                new EvidenceLibrary.EvidenceCategory("ai-app", "AI 应用开发", 3),
                new EvidenceLibrary.EvidenceCategory("agent", "AI Agent 工具", 2),
                new EvidenceLibrary.EvidenceCategory("rag", "RAG / Knowledge", 1),
                new EvidenceLibrary.EvidenceCategory("ai-coding", "AI Coding", 1),
                new EvidenceLibrary.EvidenceCategory("frontend", "前端工程", 2),
                new EvidenceLibrary.EvidenceCategory("ci", "CI / 部署", 2),
                new EvidenceLibrary.EvidenceCategory("trace", "Trace / Human Review", 3));
    }

    private EvidenceLibrary.EvidenceItem mcpGateway() {
        return item(
                "evidence-mcp", "MCP Tool Gateway", "mcp-tool-gateway",
                "Spring Boot 工具网关，展示统一注册、调用审计与 Trace 证据。",
                List.of("Java 后端", "AI Agent 工具", "Trace / Human Review"),
                List.of("README", "接口设计", "后端测试", "Trace"),
                "强", List.of("Spring Boot", "MCP Tool", "接口设计", "审计与异常处理"), "Confirmed", "2026-07-01",
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
                                step("人工确认", "verified", "事实与来源已复核"))));
    }

    private EvidenceLibrary.EvidenceItem devFlow() {
        return item(
                "evidence-devflow", "DevFlow Copilot", "devflow-copilot",
                "从任务拆解到工具执行的 AI Coding Workflow 演示。",
                List.of("AI Coding", "AI 应用开发", "Trace / Human Review"),
                List.of("README", "截图", "接口设计", "GitHub Actions"),
                "强", List.of("AI Workflow", "Prompt Workflow", "工程交付", "协作开发"), "Confirmed", "2026-06-30",
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
                                step("人工确认", "verified", "表述已复核"))));
    }

    private EvidenceLibrary.EvidenceItem ticketRag() {
        return item(
                "evidence-rag", "Enterprise Ticket RAG Copilot", "enterprise-ticket-rag",
                "带引用、Trace 与 Human Review 的企业工单 RAG 演示。",
                List.of("RAG / Knowledge", "AI 应用开发", "Trace / Human Review"),
                List.of("README", "Trace", "效果评估", "截图"),
                "中", List.of("RAG", "知识检索", "Provider fallback", "引用追踪"), "Needs review", "2026-06-29",
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
                                step("人工确认", "partial", "效果描述待复核"))));
    }

    private EvidenceLibrary.EvidenceItem portfolioHub() {
        return item(
                "evidence-portfolio", "Portfolio Hub", "portfolio-hub",
                "作品集聚合、真实页面截图与持续构建证据。",
                List.of("前端工程", "CI / 部署"),
                List.of("真实截图", "GitHub Actions", "部署记录", "README"),
                "中", List.of("Vue 3", "前端工程", "CI", "Deployment"), "Confirmed", "2026-06-28",
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
                                step("人工确认", "verified", "来源已复核"))));
    }

    private EvidenceLibrary.EvidenceItem item(
            String id,
            String name,
            String slug,
            String summary,
            List<String> tags,
            List<String> sources,
            String credibility,
            List<String> requirements,
            String reviewStatus,
            String updatedAt,
            EvidenceLibrary.EvidenceDetail detail) {
        return new EvidenceLibrary.EvidenceItem(
                id, name, slug, summary, tags, sources, credibility, requirements, reviewStatus, updatedAt, detail);
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

    private EvidenceCoverage.CoverageItem coverage(
            String skill,
            String level,
            int score,
            List<String> projects,
            String gap) {
        return new EvidenceCoverage.CoverageItem(skill, level, score, projects, gap);
    }
}
