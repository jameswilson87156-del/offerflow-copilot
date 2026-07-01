package com.offerflow.copilot.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "offerflow.ai")
public class AiProviderProperties {

    private final Provider provider = new Provider();
    private final ExternalProvider openaiCompatible = new ExternalProvider("gpt-compatible-demo");
    private final ExternalProvider deepseek = new ExternalProvider("deepseek-chat");

    public Provider getProvider() {
        return provider;
    }

    public ExternalProvider getOpenaiCompatible() {
        return openaiCompatible;
    }

    public ExternalProvider getDeepseek() {
        return deepseek;
    }

    public String providerMode() {
        return textOr(provider.mode, "local-rule");
    }

    public boolean realCallEnabled() {
        return provider.realCallEnabled;
    }

    public boolean rawResponseSave() {
        return provider.rawResponseSave;
    }

    public int timeoutMs() {
        return provider.timeoutMs > 0 ? provider.timeoutMs : 8000;
    }

    public boolean openAiCompatibleConfigured() {
        return configured(openaiCompatible);
    }

    public boolean deepSeekConfigured() {
        return configured(deepseek);
    }

    public boolean configured(ExternalProvider externalProvider) {
        return hasText(externalProvider.baseUrl) && hasText(externalProvider.apiKey);
    }

    public String apiKeyStatus(ExternalProvider externalProvider) {
        return hasText(externalProvider.apiKey) ? "masked" : "not configured";
    }

    private boolean configured(String providerMode) {
        return switch (providerMode) {
            case "local-rule" -> true;
            case "openai-compatible" -> openAiCompatibleConfigured();
            case "deepseek" -> deepSeekConfigured();
            default -> false;
        };
    }

    public String configurationReason(String providerMode) {
        if ("local-rule".equals(providerMode)) {
            return "local-rule is always available and does not use external network calls.";
        }
        if (!configured(providerMode)) {
            return providerMode + " is not configured with both base URL and API key.";
        }
        if (!realCallEnabled()) {
            return "realCallEnabled=false; external provider requests are disabled.";
        }
        return "P4B adapter is no-op; real network calls are intentionally not wired.";
    }

    private static String textOr(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static class Provider {
        private String mode = "local-rule";
        private boolean realCallEnabled = false;
        private boolean rawResponseSave = false;
        private int timeoutMs = 8000;

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public boolean isRealCallEnabled() {
            return realCallEnabled;
        }

        public void setRealCallEnabled(boolean realCallEnabled) {
            this.realCallEnabled = realCallEnabled;
        }

        public boolean isRawResponseSave() {
            return rawResponseSave;
        }

        public void setRawResponseSave(boolean rawResponseSave) {
            this.rawResponseSave = rawResponseSave;
        }

        public int getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }

    public static class ExternalProvider {
        private String baseUrl = "";
        private String apiKey = "";
        private String model;

        public ExternalProvider(String model) {
            this.model = model;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}
