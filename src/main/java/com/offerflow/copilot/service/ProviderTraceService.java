package com.offerflow.copilot.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.offerflow.copilot.domain.ProviderTraceCenter;
import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.persistence.entity.ProviderTraceRunEntity;
import com.offerflow.copilot.persistence.entity.TraceStepEntity;
import com.offerflow.copilot.persistence.repository.ProviderTraceRunRepository;
import com.offerflow.copilot.persistence.repository.TraceStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProviderTraceService {

    private static final String MODE = "mock/local-rule";
    private static final String CURRENT_STATUS = "local-rule fallback active";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProviderTraceRunRepository providerTraceRunRepository;
    private final TraceStepRepository traceStepRepository;
    private final JsonCodec jsonCodec;

    public ProviderTraceService(
            ProviderTraceRunRepository providerTraceRunRepository,
            TraceStepRepository traceStepRepository,
            JsonCodec jsonCodec) {
        this.providerTraceRunRepository = providerTraceRunRepository;
        this.traceStepRepository = traceStepRepository;
        this.jsonCodec = jsonCodec;
    }

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
                providerTraceRunRepository.findAll().stream()
                        .map(this::summary)
                        .toList());
    }

    public ProviderTraceCenter.TraceRun trace(String runId) {
        ProviderTraceRunEntity run = providerTraceRunRepository.findByRunId(runId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Provider trace not found"));
        return new ProviderTraceCenter.TraceRun(
                run.getRunId(),
                run.getJobTitle(),
                run.getProviderMode(),
                run.getFinalProvider(),
                run.getModel(),
                run.getFallbackReason(),
                run.getPromptVersion(),
                run.getSchemaVersion(),
                jsonCodec.readList(run.getRiskFlagsJson(), String.class),
                run.getEvidenceCount(),
                run.getHumanReviewStatus(),
                duration(run.getDurationMs()),
                run.getTraceHash(),
                traceStepRepository.findByRunId(runId).stream().map(this::pipelineStep).toList(),
                jsonCodec.read(run.getEvidenceDetailJson(), ProviderTraceCenter.TraceEvidenceDetail.class),
                jsonCodec.readList(run.getTechnicalTagsJson(), String.class));
    }

    private ProviderTraceCenter.TraceSummary summary(ProviderTraceRunEntity run) {
        return new ProviderTraceCenter.TraceSummary(
                run.getRunId(),
                run.getJobTitle(),
                run.getProviderMode(),
                run.getFinalProvider(),
                "warning",
                run.getCreatedAt().format(FORMATTER),
                duration(run.getDurationMs()),
                run.getEvidenceCount(),
                run.getHumanReviewStatus());
    }

    private ProviderTraceCenter.PipelineStep pipelineStep(TraceStepEntity step) {
        return new ProviderTraceCenter.PipelineStep(
                step.getStepKey(),
                step.getStepName(),
                step.getStatus(),
                duration(step.getDurationMs()),
                step.getInputSummary(),
                step.getOutputSummary(),
                String.join(" / ", jsonCodec.readList(step.getEvidenceRefsJson(), String.class)));
    }

    private String duration(int durationMs) {
        return durationMs + "ms";
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
}
