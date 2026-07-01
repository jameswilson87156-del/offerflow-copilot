package com.offerflow.copilot.service;

import java.util.List;

import com.offerflow.copilot.domain.InterviewPrepDemo;
import org.springframework.stereotype.Service;

@Service
public class InterviewPrepService {

    public InterviewPrepDemo getDemoPrep() {
        return new InterviewPrepDemo(
                "mock/local-rule",
                "Java 后端 / AI 应用开发实习生",
                "面试前准备与复盘，不是实时面试辅助工具。",
                focusAreas(),
                questionGroups(),
                new InterviewPrepDemo.StarDraft(
                        "项目组合需要把 Java 后端能力和 AI Workflow 证据讲清楚，同时避免夸大外部模型能力。",
                        "准备一个能解释 MCP Tool Gateway、Provider fallback 与 Human Review 的项目深挖回答。",
                        "按 Trace Evidence 展示输入、规则处理、Schema Validate、Risk Guard 和人工复核链路。",
                        "形成可复述的作品集级项目表达，面试后继续补充 Redis、分布式事务和性能证据。",
                        "不要声称真实稳定 LLM、企业客户、真实用户或生产级能力。"),
                List.of(
                        "不夸大生产级能力",
                        "不声称真实稳定 LLM",
                        "不虚构企业客户和真实用户",
                        "不把 fallback 包装为模型能力"),
                List.of(
                        timeline("prepare", "待准备", "current", "整理问题、证据与 STAR 草稿"),
                        timeline("answers-confirmed", "已确认答案", "upcoming", "人工确认后可用于面试前复习"),
                        timeline("mock-practice", "已模拟练习", "upcoming", "仅做面试前练习，不做实时辅助"),
                        timeline("interviewed", "已面试", "upcoming", "记录面试问题与反馈"),
                        timeline("reviewed", "已复盘", "upcoming", "补充证据缺口和后续行动")),
                "所有内容仅用于面试前准备与复盘，不能用于实时面试辅助或作弊。");
    }

    private List<InterviewPrepDemo.FocusArea> focusAreas() {
        return List.of(
                focus("Java / Spring Boot", "接口分层、异常处理、事务与测试", "MCP Tool Gateway"),
                focus("AI Workflow", "任务拆解、工具调用、结构化输出", "DevFlow Copilot"),
                focus("Trace Evidence", "每一步输入输出、证据和风险可回溯", "Provider Trace"),
                focus("Human Review", "复制或投递前必须人工确认", "人工复核中心"),
                focus("Provider fallback", "Provider 未配置或失败时明确降级", "local-rule fallback"),
                focus("RAG 边界", "引用、评估和匿名化数据边界", "Enterprise Ticket RAG Copilot"));
    }

    private List<InterviewPrepDemo.QuestionGroup> questionGroups() {
        return List.of(
                group("foundation", "技术基础", List.of(
                        "Spring Boot 项目中如何处理幂等、事务与异常？",
                        "如果岗位要求 Redis，你会如何补强？")),
                group("project", "项目深挖", List.of(
                        "你在 MCP Tool Gateway 中如何设计 Trace Evidence？",
                        "DevFlow Copilot 为什么需要 Human Review？")),
                group("ai-app", "AI 应用理解", List.of(
                        "local-rule fallback 和真实 LLM Provider 有什么区别？",
                        "Ticket RAG 项目中 RAG 的边界是什么？")),
                group("engineering", "工程边界", List.of(
                        "Provider 未配置时如何避免伪装成成功？",
                        "如何证明项目证据不是虚构的？")),
                group("collaboration", "协作与复盘", List.of(
                        "面试后你会如何复盘问题和补齐证据？",
                        "当 AI 草稿存在夸大表述时你如何处理？")));
    }

    private InterviewPrepDemo.FocusArea focus(String label, String detail, String evidence) {
        return new InterviewPrepDemo.FocusArea(label, detail, evidence);
    }

    private InterviewPrepDemo.QuestionGroup group(String key, String label, List<String> questions) {
        return new InterviewPrepDemo.QuestionGroup(key, label, questions);
    }

    private InterviewPrepDemo.TimelineStep timeline(String key, String label, String status, String detail) {
        return new InterviewPrepDemo.TimelineStep(key, label, status, detail);
    }
}
