package com.offerflow.copilot.provider;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class NoOpOpenAiCompatibleProviderClient implements AiProviderClient {

    private static final String MODE = "openai-compatible";

    private final AiProviderProperties properties;

    public NoOpOpenAiCompatibleProviderClient(AiProviderProperties properties) {
        this.properties = properties;
    }

    @Override
    public ProviderResponse analyze(ProviderRequest request) {
        String reason = properties.configurationReason(MODE);
        return new ProviderResponse(
                false,
                MODE,
                MODE,
                properties.getOpenaiCompatible().getModel(),
                "",
                "{}",
                false,
                reason,
                "fallback_required",
                reason,
                0,
                request.runId(),
                List.of("external-provider-disabled", "human-review-required"),
                false,
                true);
    }

    @Override
    public ProviderHealth health() {
        boolean configured = properties.openAiCompatibleConfigured();
        return new ProviderHealth(
                MODE,
                configured ? "CONFIGURED_NOOP" : "NOT_CONFIGURED",
                configured,
                properties.realCallEnabled(),
                LocalDateTime.now(),
                properties.configurationReason(MODE));
    }

    @Override
    public ProviderDescriptor descriptor() {
        boolean configured = properties.openAiCompatibleConfigured();
        return new ProviderDescriptor(
                MODE,
                "OpenAI-compatible",
                hasText(properties.getOpenaiCompatible().getBaseUrl()),
                configured,
                properties.apiKeyStatus(properties.getOpenaiCompatible()),
                properties.getOpenaiCompatible().getModel(),
                properties.timeoutMs(),
                MODE.equals(properties.providerMode()),
                properties.realCallEnabled(),
                properties.rawResponseSave(),
                "fallback to local-rule when disabled, unconfigured, failed, or timed out",
                "Adapter structure exists, but P4B does not perform real OpenAI-compatible calls.");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
