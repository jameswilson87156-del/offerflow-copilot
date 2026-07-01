package com.offerflow.copilot.provider.contract;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offerflow.copilot.provider.AiProviderProperties;
import com.offerflow.copilot.provider.ProviderResponse;
import org.springframework.stereotype.Component;

@Component
public class ProviderResponseValidator {

    private static final String LOCAL_RULE = "local-rule";

    private final AiProviderProperties properties;
    private final ObjectMapper objectMapper;

    public ProviderResponseValidator(AiProviderProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public ProviderValidatedResult validate(
            ProviderTaskType taskType,
            ProviderResponse response,
            PromptContract contract,
            RiskPolicy riskPolicy,
            ProviderResponseSchema schema) {
        List<ProviderContractViolation> violations = new ArrayList<>();
        LinkedHashSet<String> riskFlags = new LinkedHashSet<>(response.riskFlags());

        requireText(response.providerMode(), "providerMode", "missing_provider_mode", violations);
        requireText(response.finalProvider(), "finalProvider", "missing_final_provider", violations);

        if (response.rawResponseSaved()) {
            violations.add(violation(
                    "raw_response_saved",
                    "rawResponseSaved must remain false in P4C.",
                    "rawResponseSaved",
                    "BLOCKER",
                    true));
        }

        if (!properties.realCallEnabled()
                && response.success()
                && response.finalProvider() != null
                && !LOCAL_RULE.equals(response.finalProvider())) {
            violations.add(violation(
                    "external_success_disabled",
                    "realCallEnabled=false, so external provider success cannot be treated as real success.",
                    "finalProvider",
                    "BLOCKER",
                    true));
        }

        JsonNode json = validateStructuredJson(response.structuredJson(), schema, violations);
        if (json != null && json.has("schemaVersion")
                && !schema.schemaVersion().equals(json.get("schemaVersion").asText())) {
            violations.add(violation(
                    "schema_version_mismatch",
                    "Structured JSON schemaVersion does not match " + schema.schemaVersion() + ".",
                    "structuredJson.schemaVersion",
                    "ERROR",
                    true));
        }

        if (response.outputText() != null && response.outputText().length() > schema.maxTextLength()) {
            violations.add(violation(
                    "output_text_too_long",
                    "Output text exceeds maxTextLength=" + schema.maxTextLength() + ".",
                    "outputText",
                    "WARNING",
                    false));
        }

        String combinedOutput = textOr(response.outputText()) + "\n" + textOr(response.structuredJson());
        for (String term : riskPolicy.forbiddenTerms()) {
            if (containsIgnoreCase(combinedOutput, term)) {
                violations.add(violation(
                        "forbidden_term",
                        "Output contains forbidden term: " + term,
                        "outputText",
                        "BLOCKER",
                        true));
            }
        }
        for (String claim : riskPolicy.forbiddenClaims()) {
            if (containsIgnoreCase(combinedOutput, claim)) {
                violations.add(violation(
                        "forbidden_claim",
                        "Output contains forbidden claim: " + claim,
                        "outputText",
                        "BLOCKER",
                        true));
            }
        }

        for (ProviderContractViolation violation : violations) {
            riskFlags.add("contract-" + violation.code());
        }
        riskFlags.add("schema:" + schema.schemaVersion());
        riskFlags.add("prompt:" + contract.promptVersion());
        riskFlags.add("risk-policy:" + riskPolicy.riskPolicyVersion());
        if (riskPolicy.requireHumanReview()) {
            riskFlags.add("human-review-required");
        }
        if (violations.isEmpty()) {
            riskFlags.add("contract-validated");
        }

        boolean fallbackRequired = violations.stream().anyMatch(ProviderContractViolation::fallbackRequired);
        boolean humanReviewRequired = riskPolicy.requireHumanReview()
                || violations.stream().anyMatch(ProviderContractViolation::humanReviewRequired);
        return new ProviderValidatedResult(
                violations.isEmpty(),
                violations,
                sanitizeOutput(response.outputText(), riskPolicy),
                fallbackRequired,
                humanReviewRequired,
                List.copyOf(riskFlags),
                schema.schemaVersion(),
                contract.promptVersion(),
                riskPolicy.riskPolicyVersion());
    }

    private JsonNode validateStructuredJson(
            String structuredJson,
            ProviderResponseSchema schema,
            List<ProviderContractViolation> violations) {
        if (!schema.requireStructuredJson()) {
            return null;
        }
        if (structuredJson == null || structuredJson.isBlank()) {
            violations.add(violation(
                    "missing_structured_json",
                    "structuredJson is required by " + schema.schemaName() + ".",
                    "structuredJson",
                    "ERROR",
                    true));
            return null;
        }
        try {
            JsonNode json = objectMapper.readTree(structuredJson);
            for (String requiredField : schema.requiredFields()) {
                if (!json.hasNonNull(requiredField)) {
                    violations.add(violation(
                            "missing_required_field",
                            "Structured JSON is missing required field: " + requiredField,
                            "structuredJson." + requiredField,
                            "ERROR",
                            true));
                }
            }
            return json;
        } catch (JsonProcessingException exception) {
            violations.add(violation(
                    "invalid_structured_json",
                    "structuredJson is not valid JSON.",
                    "structuredJson",
                    "ERROR",
                    true));
            return null;
        }
    }

    private void requireText(
            String value,
            String field,
            String code,
            List<ProviderContractViolation> violations) {
        if (value == null || value.isBlank()) {
            violations.add(violation(
                    code,
                    field + " is required.",
                    field,
                    "ERROR",
                    true));
        }
    }

    private ProviderContractViolation violation(
            String code,
            String message,
            String field,
            String severity,
            boolean fallbackRequired) {
        return new ProviderContractViolation(
                code,
                message,
                field,
                severity,
                fallbackRequired,
                true);
    }

    private String sanitizeOutput(String outputText, RiskPolicy riskPolicy) {
        String sanitized = outputText == null || outputText.isBlank()
                ? "Validation sandbox local response; no external model call was made."
                : outputText;
        for (String term : riskPolicy.forbiddenTerms()) {
            sanitized = sanitized.replace(term, "[removed-risk-term]");
        }
        for (String claim : riskPolicy.forbiddenClaims()) {
            sanitized = sanitized.replace(claim, "[removed-risk-claim]");
        }
        return sanitized;
    }

    private boolean containsIgnoreCase(String haystack, String needle) {
        if (haystack == null || needle == null || needle.isBlank()) {
            return false;
        }
        return haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private String textOr(String value) {
        return value == null ? "" : value;
    }
}
