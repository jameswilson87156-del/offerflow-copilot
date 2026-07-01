package com.offerflow.copilot.provider;

import java.util.List;
import java.util.Map;

public record ProviderConfigCheck(
        String providerMode,
        boolean localRuleAvailable,
        boolean openAiCompatibleConfigured,
        boolean deepSeekConfigured,
        boolean realCallEnabled,
        boolean rawResponseSave,
        Map<String, String> apiKeyStatus,
        List<String> warnings,
        String boundaryNotice) {
}
