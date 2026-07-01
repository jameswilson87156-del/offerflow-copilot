package com.offerflow.copilot.service;

import java.util.List;

import com.offerflow.copilot.domain.ProviderTraceCenter;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProviderTraceService {

    private static final String RUN_ID = "JD-20260701-143522-9E4D";
    private static final String MODE = "mock/local-rule";
    private static final String CURRENT_STATUS = "local-rule fallback active";

    public ProviderTraceCenter.ProviderSettings settings() {
        return new ProviderTraceCenter.ProviderSettings(
                MODE,
                CURRENT_STATUS,
                List.of(
                        provider(
                                "local-rule",
                                "local-rule fallback",
                                "Active",
                                "本地规则引擎",
                                "local-rule-engine v2.1",
                                "无网络超时",
                                "2026-07-01 14:35:22",
                                "primary fallback",
                                "仅本地规则，不访问外部服务",
                                false,
                                "disabled",
                                "disabled"),
                        provider(
                                "openai-compatible",
                                "OpenAI-compatible",
                                "Not configured",
                                "not configured",
                                "not selected",
                                "30s",
                                "未真实调用",
                                "fallback to local-rule",
                                "配置占位，本轮不发起真实请求",
                                false,
                                "disabled",
                                "masked / not configured"),
                        provider(
                                "deepseek",
                                "DeepSeek",
                                "Not configured",
                                "not configured",
                                "disabled",
                                "30s",
                                "未真实调用",
                                "fallback to local-rule",
                                "DeepSeek 禁用，本轮不发起真实请求",
                                false,
                                "disabled",
                                "masked / not configured")),
                List.of(
                        boundary("不保存 API Key 明文", "API Key 仅显示 masked / not configured / disabled，本轮不落库。", "safe"),
                        boundary("不保存真实隐私", "输入输出使用匿名化演示数据，不保存 PII 原文。", "safe"),
                        boundary("Provider 未配置时必须显示 fallback", "OpenAI-compatible 与 DeepSeek 未配置时明确回退 local-rule。", "warning"),
                        boundary("模型失败时不伪装成成功", "Provider Call 步骤标记 fallback，Run Details 保留降级原因。", "warning"),
                        boundary("所有输出进入 Human Review", "AI / 规则输出默认 Draft，人工确认前不可复制。", "safe")));
    }

    public ProviderTraceCenter.TraceIndex traces() {
        return new ProviderTraceCenter.TraceIndex(
                MODE,
                CURRENT_STATUS,
                List.of(
                        new ProviderTraceCenter.TraceSummary(
                                RUN_ID,
                                "Java AI 应用开发实习生",
                                "local-rule fallback active",
                                "local-rule fallback",
                                "warning",
                                "2026-07-01 14:35:22",
                                "842ms",
                                12,
                                "待人工确认")));
    }

    public ProviderTraceCenter.TraceRun trace(String runId) {
        if (!RUN_ID.equals(runId)) {
            throw new ResponseStatusException(NOT_FOUND, "Provider trace not found");
        }
        return new ProviderTraceCenter.TraceRun(
                RUN_ID,
                "Java AI 应用开发实习生",
                "local-rule fallback active",
                "local-rule fallback",
                "local-rule-engine v2.1",
                "OpenAI-compatible 与 DeepSeek 均未配置；本轮边界禁止真实 Provider 调用。",
                "v2.4.8",
                "v1.4.3",
                List.of("命中风险词：真实用户", "命中风险词：保证通过"),
                12,
                "待人工确认",
                "842ms",
                "mock-a3f9d2c7b8e19f4d",
                pipeline(),
                new ProviderTraceCenter.TraceEvidenceDetail(
                        "熟悉 Spring Boot 开发，了解 AI 工具集成与集成方式；有 RAG、MCP 等相关实践优先。",
                        List.of(
                                evidence("RES-001", "MCP Tool Gateway", "Spring Boot 工具网关，包含注册、调用审计与 Trace 证据。", List.of("README", "后端测试", "Trace")),
                                evidence("RES-012", "DevFlow Copilot", "任务拆解、Provider fallback、Schema Validate、Human Review。", List.of("README", "截图", "Trace")),
                                evidence("RES-022", "Enterprise Ticket RAG Copilot", "带引用和复核链路的 RAG 演示，仍缺少大规模离线评测。", List.of("README", "截图", "Trace"))),
                        "{\"provider\":\"local-rule fallback\",\"schema\":\"v1.4.3\",\"evidence_count\":12,\"copy_allowed\":false}",
                        "OpenAI-compatible 未配置；DeepSeek 禁用；未发起任何外部网络请求。",
                        "需要把结果承诺改成证据覆盖说明，所有输出进入 Human Review。"),
                List.of(
                        "OpenAI-compatible",
                        "DeepSeek",
                        "local-rule fallback",
                        "Trace Evidence",
                        "Schema Validate",
                        "Risk Guard",
                        "Human Review",
                        "Spring Boot 3",
                        "Vue 3",
                        "TypeScript"));
    }

    private ProviderTraceCenter.ProviderCard provider(
            String id,
            String name,
            String status,
            String baseUrlStatus,
            String model,
            String timeout,
            String lastRun,
            String fallbackPolicy,
            String boundaryNotice,
            boolean realCallEnabled,
            String rawResponseSave,
            String apiKeyStatus) {
        return new ProviderTraceCenter.ProviderCard(
                id,
                name,
                status,
                baseUrlStatus,
                model,
                timeout,
                lastRun,
                fallbackPolicy,
                boundaryNotice,
                realCallEnabled,
                rawResponseSave,
                apiKeyStatus);
    }

    private ProviderTraceCenter.SafetyBoundary boundary(String title, String description, String tone) {
        return new ProviderTraceCenter.SafetyBoundary(title, description, tone);
    }

    private List<ProviderTraceCenter.PipelineStep> pipeline() {
        return List.of(
                step("jd-input", "JD Input", "success", "62ms", "手动录入演示 JD", "抽取岗位、技能与风险要求", "JD-042"),
                step("pii-redaction", "PII Redaction", "success", "34ms", "匿名化候选材料摘要", "未保存 PII 原文", "PII Guard"),
                step("prompt-template", "Prompt Template", "success", "88ms", "Prompt v2.4.8", "生成结构化提示摘要", "Template Registry"),
                step("provider-call", "Provider Call", "fallback", "0ms", "Provider 未配置", "未发起网络请求，回退 local-rule", "Provider Boundary"),
                step("json-parse", "JSON Parse", "success", "41ms", "local-rule JSON snapshot", "解析为结构化对象", "Parser"),
                step("schema-validate", "Schema Validate", "success", "55ms", "Schema v1.4.3", "36 条字段通过", "Schema Guard"),
                step("risk-guard", "Risk Guard", "warning", "64ms", "禁用风险词扫描", "命中 2 项，复制仍禁用", "Risk Guard"),
                step("evidence-binding", "Evidence Binding", "success", "97ms", "JD + Resume Evidence", "绑定 12 条证据", "RES-001 / RES-012"),
                step("human-review", "Human Review", "warning", "401ms", "Draft 输出", "等待人工确认", "review-star-mcp"),
                step("confirmed-result", "Confirmed Result", "warning", "0ms", "人工确认前", "尚无可复制结果", "copyAllowed=false"));
    }

    private ProviderTraceCenter.PipelineStep step(
            String key,
            String label,
            String status,
            String duration,
            String inputSummary,
            String outputSummary,
            String linkedEvidence) {
        return new ProviderTraceCenter.PipelineStep(key, label, status, duration, inputSummary, outputSummary, linkedEvidence);
    }

    private ProviderTraceCenter.ResumeEvidenceRef evidence(
            String id,
            String title,
            String excerpt,
            List<String> sources) {
        return new ProviderTraceCenter.ResumeEvidenceRef(id, title, excerpt, sources);
    }
}
