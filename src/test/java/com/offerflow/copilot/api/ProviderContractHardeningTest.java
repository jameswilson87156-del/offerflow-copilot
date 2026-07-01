package com.offerflow.copilot.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.offerflow.copilot.OfferFlowCopilotApplication;
import com.offerflow.copilot.provider.AiProviderProperties;
import com.offerflow.copilot.provider.ProviderResponse;
import com.offerflow.copilot.provider.contract.PromptContract;
import com.offerflow.copilot.provider.contract.PromptContractRegistry;
import com.offerflow.copilot.provider.contract.ProviderResponseSchema;
import com.offerflow.copilot.provider.contract.ProviderResponseSchemaRegistry;
import com.offerflow.copilot.provider.contract.ProviderResponseValidator;
import com.offerflow.copilot.provider.contract.ProviderTaskType;
import com.offerflow.copilot.provider.contract.ProviderValidatedResult;
import com.offerflow.copilot.provider.contract.RiskPolicy;
import com.offerflow.copilot.provider.contract.RiskPolicyRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        classes = OfferFlowCopilotApplication.class,
        properties = "spring.datasource.url=jdbc:h2:mem:offerflow-contracts;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProviderContractHardeningTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromptContractRegistry promptContractRegistry;

    @Autowired
    private RiskPolicyRegistry riskPolicyRegistry;

    @Autowired
    private ProviderResponseSchemaRegistry responseSchemaRegistry;

    @Autowired
    private ProviderResponseValidator responseValidator;

    @Autowired
    private AiProviderProperties properties;

    @Test
    void everyProviderTaskTypeHasPromptContract() {
        for (ProviderTaskType taskType : ProviderTaskType.values()) {
            org.assertj.core.api.Assertions.assertThat(promptContractRegistry.get(taskType)).isNotNull();
        }
    }

    @Test
    void everyPromptContractHasVersionTriplet() {
        for (PromptContract contract : promptContractRegistry.all()) {
            org.assertj.core.api.Assertions.assertThat(contract.promptVersion()).isNotBlank();
            org.assertj.core.api.Assertions.assertThat(contract.schemaVersion()).isNotBlank();
            org.assertj.core.api.Assertions.assertThat(contract.riskPolicyVersion()).isNotBlank();
        }
    }

    @Test
    void riskPolicyBlocksOfferProbabilityAndGuaranteeClaims() {
        RiskPolicy policy = riskPolicyRegistry.get(ProviderTaskType.MATCH_REPORT);

        org.assertj.core.api.Assertions.assertThat(policy.forbiddenTerms())
                .contains("Offer 概率", "录取概率", "保证通过");
    }

    @Test
    void validatorAcceptsLegalLocalRuleResponse() {
        ProviderValidatedResult result = validate(localRuleResponse(validStructuredJson(), "Evidence coverage summary only."));

        org.assertj.core.api.Assertions.assertThat(result.valid()).isTrue();
        org.assertj.core.api.Assertions.assertThat(result.riskFlags()).contains("contract-validated");
    }

    @Test
    void validatorRejectsMissingRequiredField() {
        ProviderValidatedResult result = validate(localRuleResponse("""
                {"provider":"local-rule","schemaVersion":"match-report-schema-v1","humanReviewRequired":true,"copyAllowed":false}
                """, "Missing summary field."));

        org.assertj.core.api.Assertions.assertThat(result.valid()).isFalse();
        org.assertj.core.api.Assertions.assertThat(result.violations())
                .anyMatch(violation -> "missing_required_field".equals(violation.code()));
    }

    @Test
    void validatorRejectsOfferProbabilityClaims() {
        ProviderValidatedResult result = validate(localRuleResponse(validStructuredJson(), "Offer 概率 90%，保证通过。"));

        org.assertj.core.api.Assertions.assertThat(result.valid()).isFalse();
        org.assertj.core.api.Assertions.assertThat(result.violations())
                .anyMatch(violation -> "forbidden_term".equals(violation.code()));
    }

    @Test
    void validatorRejectsProductionRealModelClaims() {
        ProviderValidatedResult result = validate(localRuleResponse(validStructuredJson(), "生产级稳定接入真实模型。"));

        org.assertj.core.api.Assertions.assertThat(result.valid()).isFalse();
        org.assertj.core.api.Assertions.assertThat(result.violations())
                .anyMatch(violation -> "forbidden_term".equals(violation.code()));
    }

    @Test
    void validateResponseEndpointDoesNotCallNetworkOrSaveRawResponse() throws Exception {
        mockMvc.perform(post("/api/provider/validate-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskType": "match-report",
                                  "providerMode": "local-rule",
                                  "model": "validation-sandbox-model",
                                  "simulateUnsafeClaim": false,
                                  "simulateMissingField": false,
                                  "simulateSchemaMismatch": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.riskFlags[1]").value("no-external-model-call"))
                .andExpect(jsonPath("$.humanReviewRequired").value(true));
    }

    @Test
    void validateResponseEndpointRejectsMissingField() throws Exception {
        mockMvc.perform(post("/api/provider/validate-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskType": "match-report",
                                  "providerMode": "local-rule",
                                  "simulateMissingField": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.fallbackRequired").value(true))
                .andExpect(jsonPath("$.violations[0].code").value("missing_required_field"));
    }

    @Test
    void contractsEndpointDoesNotExposeApiKeyFields() throws Exception {
        mockMvc.perform(get("/api/provider/contracts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskType").value("jd-analysis"))
                .andExpect(content().string(not(containsString("apiKey"))))
                .andExpect(content().string(not(containsString("api-key"))));
    }

    @Test
    void unknownContractTaskTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/provider/contracts/not-a-task"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void providerDefaultsRemainDisabled() {
        org.assertj.core.api.Assertions.assertThat(properties.realCallEnabled()).isFalse();
        org.assertj.core.api.Assertions.assertThat(properties.rawResponseSave()).isFalse();
    }

    private ProviderValidatedResult validate(ProviderResponse response) {
        ProviderTaskType taskType = ProviderTaskType.MATCH_REPORT;
        PromptContract contract = promptContractRegistry.get(taskType);
        RiskPolicy policy = riskPolicyRegistry.get(taskType);
        ProviderResponseSchema schema = responseSchemaRegistry.get(taskType);
        return responseValidator.validate(taskType, response, contract, policy, schema);
    }

    private ProviderResponse localRuleResponse(String structuredJson, String outputText) {
        return new ProviderResponse(
                true,
                "local-rule",
                "local-rule",
                "local-rule-engine v2.1",
                outputText,
                structuredJson,
                false,
                "",
                "",
                "",
                10,
                "contract-test",
                List.of("human-review-required"),
                false,
                true);
    }

    private String validStructuredJson() {
        return """
                {"provider":"local-rule","schemaVersion":"match-report-schema-v1","summary":"Evidence coverage summary.","humanReviewRequired":true,"copyAllowed":false}
                """;
    }
}
