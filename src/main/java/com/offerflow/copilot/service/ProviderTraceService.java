package com.offerflow.copilot.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.offerflow.copilot.domain.ProviderTraceCenter;
import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.persistence.entity.ProviderTraceRunEntity;
import com.offerflow.copilot.persistence.entity.TraceStepEntity;
import com.offerflow.copilot.persistence.repository.ProviderTraceRunRepository;
import com.offerflow.copilot.persistence.repository.TraceStepRepository;
import com.offerflow.copilot.provider.AiProviderProperties;
import com.offerflow.copilot.provider.ProviderRouter;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProviderTraceService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AiProviderProperties properties;
    private final ProviderRouter providerRouter;
    private final ProviderTraceRunRepository providerTraceRunRepository;
    private final TraceStepRepository traceStepRepository;
    private final JsonCodec jsonCodec;

    public ProviderTraceService(
            AiProviderProperties properties,
            ProviderRouter providerRouter,
            ProviderTraceRunRepository providerTraceRunRepository,
            TraceStepRepository traceStepRepository,
            JsonCodec jsonCodec) {
        this.properties = properties;
        this.providerRouter = providerRouter;
        this.providerTraceRunRepository = providerTraceRunRepository;
        this.traceStepRepository = traceStepRepository;
        this.jsonCodec = jsonCodec;
    }

    public ProviderTraceCenter.ProviderSettings settings() {
        return new ProviderTraceCenter.ProviderSettings(
                properties.providerMode(),
                currentStatus(),
                providerRouter.descriptors(),
                List.of(
                        boundary("不保存 API Key 明文", "API Key 仅显示 masked / not configured / disabled，本轮不落库。", "safe"),
                        boundary("不保存真实隐私", "输入输出使用匿名化演示数据，不保存 PII 原文。", "safe"),
                        boundary("Provider 未配置时必须显示 fallback", "OpenAI-compatible 与 DeepSeek 未配置时明确回退 local-rule。", "warning"),
                        boundary("模型失败时不伪装成成功", "Provider Call 步骤标记 fallback，Run Details 保留降级原因。", "warning"),
                        boundary("未校验输出不可进入复制流程", "所有 ProviderResponse 必须通过 schema validate 与 risk guard。", "warning"),
                        boundary("所有输出进入 Human Review", "AI / 规则输出默认 Draft，人工确认前不可复制。", "safe")));
    }

    public ProviderTraceCenter.TraceIndex traces() {
        return new ProviderTraceCenter.TraceIndex(
                properties.providerMode(),
                currentStatus(),
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
                run.getFallbackReason() == null || run.getFallbackReason().isBlank() ? "success" : "warning",
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

    private String currentStatus() {
        return "local-rule active; provider contracts validate responses before Human Review";
    }

    private ProviderTraceCenter.SafetyBoundary boundary(String title, String description, String tone) {
        return new ProviderTraceCenter.SafetyBoundary(title, description, tone);
    }
}
