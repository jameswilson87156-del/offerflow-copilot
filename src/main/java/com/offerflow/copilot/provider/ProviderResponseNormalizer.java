package com.offerflow.copilot.provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offerflow.copilot.provider.contract.ProviderResponseSchema;
import com.offerflow.copilot.provider.contract.ProviderTaskType;
import org.springframework.stereotype.Component;

@Component
public class ProviderResponseNormalizer {

    public static final String BOUNDARY_NOTICE =
            "Manual provider dry-run output requires Human Review and Copy Permission.";

    private static final Pattern SECRET_PATTERN = Pattern.compile(
            "(?i)\\b(sk-[a-z0-9_-]{8,}|bearer\\s+[a-z0-9._-]{8,})\\b");

    private final ObjectMapper objectMapper;

    public ProviderResponseNormalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public NormalizedProviderResponse normalize(
            String providerMode,
            ProviderTaskType taskType,
            ProviderResponseSchema schema,
            String providerText) {
        String safeText = safeSnippet(providerText, schema.maxTextLength());
        try {
            JsonNode json = objectMapper.readTree(jsonCandidate(providerText));
            if (json != null && json.isObject()) {
                return fromJson(providerMode, taskType, schema, json, safeText);
            }
        } catch (Exception ignored) {
            // Plain text provider output is allowed to fall through to the wrapper path.
        }
        return fromText(providerMode, taskType, schema, safeText);
    }

    private NormalizedProviderResponse fromJson(
            String providerMode,
            ProviderTaskType taskType,
            ProviderResponseSchema schema,
            JsonNode json,
            String fallbackText) throws Exception {
        String answer = firstText(json, "answer", "content", "text", "message");
        if (answer.isBlank()) {
            answer = firstText(json, "summary");
        }
        if (answer.isBlank()) {
            answer = fallbackText;
        }
        answer = safeSnippet(answer, schema.maxTextLength());

        String summary = firstText(json, "summary");
        if (summary.isBlank()) {
            summary = summarize(answer);
        }

        Map<String, Object> fields = baseFields(providerMode, taskType, schema);
        fields.put("schemaVersion", valueOr(firstText(json, "schemaVersion", "schema_version"), schema.schemaVersion()));
        fields.put("taskType", canonicalTaskType(valueOr(firstText(json, "taskType", "task_type"), taskType.name())));
        fields.put("answer", answer);
        fields.put("summary", safeSnippet(summary, 240));
        fields.put("riskFlags", stringList(json, "riskFlags", "risk_flags"));
        fields.put("boundaryNotice", valueOr(firstText(json, "boundaryNotice", "boundary_notice"), BOUNDARY_NOTICE));
        fields.put("normalizedFromText", false);

        return new NormalizedProviderResponse(
                answer,
                objectMapper.writeValueAsString(fields),
                List.of("normalized-from-json"));
    }

    private NormalizedProviderResponse fromText(
            String providerMode,
            ProviderTaskType taskType,
            ProviderResponseSchema schema,
            String text) {
        String answer = valueOr(text, "External provider returned an empty dry-run response.");
        Map<String, Object> fields = baseFields(providerMode, taskType, schema);
        fields.put("schemaVersion", schema.schemaVersion());
        fields.put("taskType", taskType.name());
        fields.put("answer", answer);
        fields.put("summary", summarize(answer));
        fields.put("riskFlags", List.of());
        fields.put("boundaryNotice", BOUNDARY_NOTICE);
        fields.put("normalizedFromText", true);

        try {
            return new NormalizedProviderResponse(
                    answer,
                    objectMapper.writeValueAsString(fields),
                    List.of("normalized-from-text"));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to normalize provider response", exception);
        }
    }

    private Map<String, Object> baseFields(String providerMode, ProviderTaskType taskType, ProviderResponseSchema schema) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("provider", valueOr(providerMode, "unknown-provider"));
        fields.put("schemaVersion", schema.schemaVersion());
        fields.put("taskType", taskType.name());
        fields.put("humanReviewRequired", true);
        fields.put("copyAllowed", false);
        return fields;
    }

    private List<String> stringList(JsonNode json, String... names) {
        for (String name : names) {
            JsonNode node = json.get(name);
            if (node != null && node.isArray()) {
                List<String> values = new ArrayList<>();
                node.forEach(item -> {
                    if (item.isTextual() && !item.asText().isBlank()) {
                        values.add(safeSnippet(item.asText(), 120));
                    }
                });
                return List.copyOf(values);
            }
        }
        return List.of();
    }

    private String firstText(JsonNode json, String... names) {
        for (String name : names) {
            JsonNode node = json.get(name);
            if (node != null && node.isValueNode()) {
                String text = node.asText("");
                if (!text.isBlank()) {
                    return text.trim();
                }
            }
        }
        return "";
    }

    private String jsonCandidate(String value) {
        String text = valueOr(value, "").trim();
        if (text.startsWith("```")) {
            int firstLineBreak = text.indexOf('\n');
            int closingFence = text.lastIndexOf("```");
            if (firstLineBreak >= 0 && closingFence > firstLineBreak) {
                text = text.substring(firstLineBreak + 1, closingFence).trim();
            }
        }
        int firstBrace = text.indexOf('{');
        int lastBrace = text.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return text.substring(firstBrace, lastBrace + 1);
        }
        return text;
    }

    private String canonicalTaskType(String value) {
        try {
            return ProviderTaskType.from(value).name();
        } catch (Exception ignored) {
            return valueOr(value, ProviderTaskType.PROVIDER_SANDBOX.name()).trim();
        }
    }

    private String summarize(String value) {
        return safeSnippet(value, 160);
    }

    private String safeSnippet(String value, int maxLength) {
        String text = SECRET_PATTERN.matcher(valueOr(value, "")).replaceAll("[removed-secret]");
        text = text.replaceAll("\\s+", " ").trim();
        int limit = Math.max(40, maxLength);
        return text.length() > limit ? text.substring(0, limit - 3) + "..." : text;
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public record NormalizedProviderResponse(
            String outputText,
            String structuredJson,
            List<String> riskFlags) {

        public NormalizedProviderResponse {
            riskFlags = riskFlags == null ? List.of() : List.copyOf(riskFlags);
        }
    }
}
