package com.offerflow.copilot.provider.contract;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.provider.AiProviderProperties;
import com.offerflow.copilot.provider.ProviderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProviderContractService {

    private final AiProviderProperties properties;
    private final PromptContractRegistry promptContractRegistry;
    private final RiskPolicyRegistry riskPolicyRegistry;
    private final ProviderResponseSchemaRegistry responseSchemaRegistry;
    private final ProviderResponseValidator responseValidator;
    private final JsonCodec jsonCodec;

    public ProviderContractService(
            AiProviderProperties properties,
            PromptContractRegistry promptContractRegistry,
            RiskPolicyRegistry riskPolicyRegistry,
            ProviderResponseSchemaRegistry responseSchemaRegistry,
            ProviderResponseValidator responseValidator,
            JsonCodec jsonCodec) {
        this.properties = properties;
        this.promptContractRegistry = promptContractRegistry;
        this.riskPolicyRegistry = riskPolicyRegistry;
        this.responseSchemaRegistry = responseSchemaRegistry;
        this.responseValidator = responseValidator;
        this.jsonCodec = jsonCodec;
    }

    public List<PromptContractSummary> summaries() {
        return promptContractRegistry.summaries(riskPolicyRegistry);
    }

    public PromptContract detail(String taskType) {
        try {
            return promptContractRegistry.get(taskType);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    public ProviderValidatedResult validate(ProviderValidationRequest request) {
        ProviderTaskType taskType = ProviderTaskType.fromOrDefault(request == null ? null : request.taskType());
        PromptContract contract = promptContractRegistry.get(taskType);
        RiskPolicy riskPolicy = riskPolicyRegistry.get(taskType);
        ProviderResponseSchema schema = responseSchemaRegistry.get(taskType);
        ProviderResponse response = validationResponse(request, schema);
        return responseValidator.validate(taskType, response, contract, riskPolicy, schema);
    }

    private ProviderResponse validationResponse(ProviderValidationRequest request, ProviderResponseSchema schema) {
        String providerMode = valueOr(request == null ? null : request.providerMode(), "local-rule");
        String finalProvider = providerMode;
        String outputText = valueOr(request == null ? null : request.outputText(),
                "Local validation sandbox output. No external model call was made; Human Review is required.");
        if (request != null && request.simulateUnsafeClaim()) {
            outputText = outputText + " Offer 概率 95%，保证通过，生产级稳定接入真实模型。";
        }
        String structuredJson = valueOr(request == null ? null : request.structuredJson(), structuredJson(request, schema));
        return new ProviderResponse(
                true,
                providerMode,
                finalProvider,
                valueOr(request == null ? null : request.model(), "validation-sandbox-model"),
                outputText,
                structuredJson,
                false,
                "",
                "",
                "",
                0,
                "validation-sandbox",
                List.of("validation-sandbox", "no-external-model-call"),
                false,
                true);
    }

    private String structuredJson(ProviderValidationRequest request, ProviderResponseSchema schema) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("provider", "local-rule");
        fields.put("schemaVersion", request != null && request.simulateSchemaMismatch()
                ? "schema-mismatch-v0"
                : schema.schemaVersion());
        if (request == null || !request.simulateMissingField()) {
            fields.put("summary", "Local validation response passes through contract checks.");
        }
        fields.put("humanReviewRequired", true);
        fields.put("copyAllowed", false);
        fields.put("boundaryNotice", "Validation sandbox only; raw model response is not saved.");
        return jsonCodec.write(fields);
    }

    public boolean defaultsKeepExternalCallsDisabled() {
        return !properties.realCallEnabled() && !properties.rawResponseSave();
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
