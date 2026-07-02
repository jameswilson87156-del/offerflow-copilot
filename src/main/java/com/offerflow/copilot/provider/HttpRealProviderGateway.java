package com.offerflow.copilot.provider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class HttpRealProviderGateway implements RealProviderGateway {

    private final ObjectMapper objectMapper;

    public HttpRealProviderGateway(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public RealProviderCallResult call(RealProviderCallRequest request) {
        long started = System.nanoTime();
        try {
            String payload = objectMapper.writeValueAsString(payload(request));
            HttpRequest httpRequest = HttpRequest.newBuilder(endpoint(request.baseUrl()))
                    .timeout(Duration.ofMillis(request.timeoutMs()))
                    .header("Authorization", "Bearer " + request.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(request.timeoutMs()))
                    .build()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int duration = durationMs(started);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new RealProviderCallResult(
                        false,
                        "",
                        "external_http_error",
                        "External provider returned HTTP " + response.statusCode() + ".",
                        duration);
            }
            return new RealProviderCallResult(true, normalizedText(response.body()), "", "", duration);
        } catch (Exception exception) {
            return new RealProviderCallResult(
                    false,
                    "",
                    "external_call_failed",
                    "External provider call failed or timed out.",
                    durationMs(started));
        }
    }

    private Map<String, Object> payload(RealProviderCallRequest request) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", request.inputText());

        Map<String, Object> system = new LinkedHashMap<>();
        system.put("role", "system");
        system.put("content", systemPrompt(request));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", request.model());
        payload.put("messages", List.of(system, message));
        payload.put("temperature", 0);
        payload.put("stream", false);
        return payload;
    }

    private String systemPrompt(RealProviderCallRequest request) {
        String taskInstruction = request.contract().userInstructionTemplate()
                .replace("{{requiredInputs}}", String.join(", ", request.contract().requiredInputs()));
        return request.contract().systemInstruction()
                + " " + taskInstruction
                + " Return JSON only, with no Markdown fences and no prose outside JSON."
                + " The schemaVersion must be exactly \"" + request.contract().schemaVersion() + "\"."
                + " For provider-sandbox, use taskType \"PROVIDER_SANDBOX\"."
                + " Set humanReviewRequired to true and copyAllowed to false."
                + " Include boundaryNotice \"Manual provider dry-run output requires Human Review and Copy Permission.\""
                + " Do not include private data or API keys.";
    }

    private URI endpoint(String baseUrl) {
        String trimmed = baseUrl == null ? "" : baseUrl.trim();
        if (trimmed.endsWith("/chat/completions")) {
            return URI.create(trimmed);
        }
        return URI.create(trimmed.replaceAll("/+$", "") + "/chat/completions");
    }

    private String normalizedText(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isTextual()) {
            return content.asText();
        }
        return "";
    }

    private int durationMs(long started) {
        return Math.max(1, (int) ((System.nanoTime() - started) / 1_000_000));
    }
}
