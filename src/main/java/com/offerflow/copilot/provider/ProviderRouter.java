package com.offerflow.copilot.provider;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ProviderRouter {

    private static final String LOCAL_RULE = "local-rule";
    private static final String OPENAI_COMPATIBLE = "openai-compatible";
    private static final String DEEPSEEK = "deepseek";

    private final AiProviderProperties properties;
    private final LocalRuleProviderClient localRuleProviderClient;
    private final NoOpOpenAiCompatibleProviderClient openAiCompatibleProviderClient;
    private final NoOpDeepSeekProviderClient deepSeekProviderClient;

    public ProviderRouter(
            AiProviderProperties properties,
            LocalRuleProviderClient localRuleProviderClient,
            NoOpOpenAiCompatibleProviderClient openAiCompatibleProviderClient,
            NoOpDeepSeekProviderClient deepSeekProviderClient) {
        this.properties = properties;
        this.localRuleProviderClient = localRuleProviderClient;
        this.openAiCompatibleProviderClient = openAiCompatibleProviderClient;
        this.deepSeekProviderClient = deepSeekProviderClient;
    }

    public ProviderResponse analyze(ProviderRequest request) {
        String selectedMode = normalize(request.providerMode());
        if (LOCAL_RULE.equals(selectedMode)) {
            return localRuleProviderClient.analyze(request);
        }

        String fallbackReason = sandboxFallbackReason(selectedMode, request);
        ProviderResponse localResponse = localRuleProviderClient.analyze(request);
        return new ProviderResponse(
                true,
                selectedMode,
                LOCAL_RULE,
                localResponse.model(),
                localResponse.outputText(),
                localResponse.structuredJson(),
                true,
                fallbackReason,
                "fallback_required",
                fallbackReason,
                localResponse.durationMs(),
                request.runId(),
                mergeRiskFlags(localResponse.riskFlags(), selectedMode),
                false,
                true);
    }

    public List<ProviderDescriptor> descriptors() {
        return List.of(
                localRuleProviderClient.descriptor(),
                openAiCompatibleProviderClient.descriptor(),
                deepSeekProviderClient.descriptor());
    }

    public List<ProviderHealth> health() {
        return List.of(
                localRuleProviderClient.health(),
                openAiCompatibleProviderClient.health(),
                deepSeekProviderClient.health());
    }

    public ProviderConfigCheck configCheck() {
        List<String> warnings = configWarnings();
        return new ProviderConfigCheck(
                properties.providerMode(),
                true,
                properties.openAiCompatibleConfigured(),
                properties.deepSeekConfigured(),
                properties.realCallEnabled(),
                properties.rawResponseSave(),
                Map.of(
                        OPENAI_COMPATIBLE, properties.apiKeyStatus(properties.getOpenaiCompatible()),
                        DEEPSEEK, properties.apiKeyStatus(properties.getDeepseek())),
                warnings,
                "Provider sandbox uses local-rule/no-op providers only; P4F real dry-run requires the manual endpoint and explicit opt-in.");
    }

    public String normalize(String providerMode) {
        String mode = providerMode == null || providerMode.isBlank() ? properties.providerMode() : providerMode;
        return mode.trim().toLowerCase(Locale.ROOT);
    }

    private String sandboxFallbackReason(String selectedMode, ProviderRequest request) {
        if (!List.of(OPENAI_COMPATIBLE, DEEPSEEK).contains(selectedMode)) {
            return "Unknown provider mode '" + selectedMode + "'; fallback to local-rule.";
        }
        if (request.simulateTimeout()) {
            return selectedMode + " simulated timeout after " + request.timeoutMs() + "ms; fallback to local-rule.";
        }
        if (request.simulateFailure()) {
            return selectedMode + " simulated failure; fallback to local-rule.";
        }
        ProviderResponse noOpResponse = client(selectedMode).analyze(request);
        return noOpResponse.fallbackReason();
    }

    private AiProviderClient client(String providerMode) {
        return DEEPSEEK.equals(providerMode) ? deepSeekProviderClient : openAiCompatibleProviderClient;
    }

    private List<String> configWarnings() {
        List<String> baseWarnings = new java.util.ArrayList<>();
        if (properties.realCallEnabled()) {
            baseWarnings.add("realCallEnabled is true; sandbox adapters remain no-op and real dry-run still requires manual opt-in.");
        }
        if (properties.rawResponseSave()) {
            baseWarnings.add("rawResponseSave is true, but ProviderResponse still forces rawResponseSaved=false.");
        }
        if (!properties.openAiCompatibleConfigured()) {
            baseWarnings.add("OpenAI-compatible is not configured; sandbox runs fallback to local-rule.");
        }
        if (!properties.deepSeekConfigured()) {
            baseWarnings.add("DeepSeek is not configured; sandbox runs fallback to local-rule.");
        }
        return List.copyOf(baseWarnings);
    }

    private List<String> mergeRiskFlags(List<String> flags, String selectedMode) {
        java.util.ArrayList<String> merged = new java.util.ArrayList<>(flags);
        merged.add(selectedMode + "-fallback");
        return List.copyOf(merged);
    }
}
