package com.offerflow.copilot.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offerflow.copilot.OfferFlowCopilotApplication;
import com.offerflow.copilot.persistence.repository.ProviderTraceRunRepository;
import com.offerflow.copilot.persistence.repository.TraceStepRepository;
import com.offerflow.copilot.provider.ProviderResponse;
import com.offerflow.copilot.provider.ProviderResponseNormalizer;
import com.offerflow.copilot.provider.RealProviderCallRequest;
import com.offerflow.copilot.provider.RealProviderCallResult;
import com.offerflow.copilot.provider.RealProviderGateway;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
        classes = OfferFlowCopilotApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:offerflow-real-dry-run;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                "offerflow.ai.provider.real-call-enabled=true",
                "offerflow.ai.provider.raw-response-save=false",
                "offerflow.ai.openai-compatible.base-url=https://unit.invalid/v1",
                "offerflow.ai.openai-compatible.api-key=dummy-openai-compatible-dry-run-key",
                "offerflow.ai.openai-compatible.model=unit-openai-model"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProviderRealDryRunTest {

    private static final String DUMMY_KEY = "dummy-openai-compatible-dry-run-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProviderTraceRunRepository providerTraceRunRepository;

    @Autowired
    private TraceStepRepository traceStepRepository;

    @Autowired
    private ProviderResponseNormalizer responseNormalizer;

    @Autowired
    private PromptContractRegistry promptContractRegistry;

    @Autowired
    private RiskPolicyRegistry riskPolicyRegistry;

    @Autowired
    private ProviderResponseSchemaRegistry responseSchemaRegistry;

    @Autowired
    private ProviderResponseValidator responseValidator;

    @MockBean
    private RealProviderGateway realProviderGateway;

    @Test
    void allowExternalCallFalseBlocksBeforeNetwork() throws Exception {
        mockMvc.perform(post("/api/provider/real-dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("openai-compatible", false, true, "OWNER",
                                "Sanitized dry-run input.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalCallAttempted").value(false))
                .andExpect(jsonPath("$.externalCallBlocked").value(true))
                .andExpect(jsonPath("$.fallbackUsed").value(true))
                .andExpect(jsonPath("$.fallbackReason").value(containsString("allowExternalCall=false")));

        verifyNoInteractions(realProviderGateway);
    }

    @Test
    void confirmNoPiiFalseBlocksBeforeNetwork() throws Exception {
        mockMvc.perform(post("/api/provider/real-dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("openai-compatible", true, false, "OWNER",
                                "Sanitized dry-run input.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalCallAttempted").value(false))
                .andExpect(jsonPath("$.externalCallBlocked").value(true))
                .andExpect(jsonPath("$.fallbackReason").value(containsString("confirmNoPii=false")));

        verifyNoInteractions(realProviderGateway);
    }

    @Test
    void unconfiguredProviderFallsBackWithoutNetwork() throws Exception {
        mockMvc.perform(post("/api/provider/real-dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("deepseek", true, true, "OWNER",
                                "Sanitized dry-run input.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalCallAttempted").value(false))
                .andExpect(jsonPath("$.externalCallBlocked").value(true))
                .andExpect(jsonPath("$.finalProvider").value("local-rule"))
                .andExpect(jsonPath("$.fallbackReason").value(containsString("deepseek is not configured")));

        verifyNoInteractions(realProviderGateway);
    }

    @Test
    void emailInputIsBlockedByPiiGuard() throws Exception {
        assertPiiBlocked("Contact me at candidate@example.com for the dry run.", "pii-email");
    }

    @Test
    void phoneInputIsBlockedByPiiGuard() throws Exception {
        assertPiiBlocked("Phone 13812345678 should be blocked.", "pii-phone");
    }

    @Test
    void secretKeyInputIsBlockedByPiiGuard() throws Exception {
        assertPiiBlocked("Never send sk-testsecretvalue123456 to a provider.", "pii-secret-key");
    }

    @Test
    void viewerCannotExecuteRealDryRun() throws Exception {
        mockMvc.perform(post("/api/provider/real-dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("openai-compatible", true, true, "VIEWER",
                                "Sanitized dry-run input.")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.reason").value(containsString("VIEWER is read-only")));

        verifyNoInteractions(realProviderGateway);
    }

    @Test
    void blockedDryRunWritesProviderTraceRun() throws Exception {
        long before = providerTraceRunRepository.count();

        dryRun("openai-compatible", false, true, "OWNER", "Sanitized dry-run input.");

        org.assertj.core.api.Assertions.assertThat(providerTraceRunRepository.count()).isGreaterThan(before);
    }

    @Test
    void blockedDryRunWritesFourteenTraceSteps() throws Exception {
        JsonNode result = dryRun("openai-compatible", false, true, "OWNER", "Sanitized dry-run input.");

        org.assertj.core.api.Assertions.assertThat(traceStepRepository.findByRunId(result.get("runId").asText()))
                .hasSize(14);
    }

    @Test
    void traceEndpointShowsRequiredDryRunSteps() throws Exception {
        JsonNode result = dryRun("openai-compatible", false, true, "OWNER", "Sanitized dry-run input.");

        mockMvc.perform(get("/api/provider/traces/" + result.get("runId").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipeline[0].label").value("Actor Permission Check"))
                .andExpect(jsonPath("$.pipeline[1].label").value("Real Call Flag Check"))
                .andExpect(jsonPath("$.pipeline[2].label").value("PII Guard"))
                .andExpect(jsonPath("$.pipeline[7].label").value("External Call Blocked"))
                .andExpect(jsonPath("$.pipeline[13].label").value("Copy Permission Blocked"));
    }

    @Test
    void externalCallFailureFallsBackToLocalRule() throws Exception {
        when(realProviderGateway.call(any(RealProviderCallRequest.class)))
                .thenReturn(new RealProviderCallResult(false, "", "external_call_failed", "External provider call failed or timed out.", 21));

        mockMvc.perform(post("/api/provider/real-dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("openai-compatible", true, true, "OWNER",
                                "Sanitized dry-run input.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalCallAttempted").value(true))
                .andExpect(jsonPath("$.externalCallBlocked").value(false))
                .andExpect(jsonPath("$.finalProvider").value("local-rule"))
                .andExpect(jsonPath("$.fallbackUsed").value(true))
                .andExpect(jsonPath("$.fallbackReason").value("External provider call failed or timed out."))
                .andExpect(jsonPath("$.schemaValidated").value(true))
                .andExpect(jsonPath("$.riskGuardPassed").value(true));

        verify(realProviderGateway).call(any(RealProviderCallRequest.class));
    }

    @Test
    void deepSeekLikeJsonResponsePassesProviderSandboxValidation() throws Exception {
        ProviderResponseNormalizer.NormalizedProviderResponse normalized = responseNormalizer.normalize(
                "deepseek",
                ProviderTaskType.PROVIDER_SANDBOX,
                responseSchemaRegistry.get(ProviderTaskType.PROVIDER_SANDBOX),
                """
                        {
                          "schema_version": "provider-sandbox-v1",
                          "task_type": "PROVIDER_SANDBOX",
                          "answer": "Trace Evidence records each provider validation step.",
                          "summary": "Trace Evidence is a review trail for provider output.",
                          "risk_flags": [],
                          "human_review_required": true,
                          "copy_allowed": false,
                          "boundary_notice": "Manual provider dry-run output requires Human Review and Copy Permission."
                        }
                        """);

        ProviderValidatedResult result = validateNormalized("deepseek", normalized);

        org.assertj.core.api.Assertions.assertThat(result.valid()).isTrue();
        org.assertj.core.api.Assertions.assertThat(result.humanReviewRequired()).isTrue();
        JsonNode structured = objectMapper.readTree(normalized.structuredJson());
        org.assertj.core.api.Assertions.assertThat(structured.get("schemaVersion").asText()).isEqualTo("provider-sandbox-v1");
        org.assertj.core.api.Assertions.assertThat(structured.get("taskType").asText()).isEqualTo("PROVIDER_SANDBOX");
        org.assertj.core.api.Assertions.assertThat(structured.get("copyAllowed").asBoolean()).isFalse();
    }

    @Test
    void openAiCompatibleLikeJsonResponsePassesProviderSandboxValidation() {
        ProviderResponseNormalizer.NormalizedProviderResponse normalized = responseNormalizer.normalize(
                "openai-compatible",
                ProviderTaskType.PROVIDER_SANDBOX,
                responseSchemaRegistry.get(ProviderTaskType.PROVIDER_SANDBOX),
                """
                        {"schemaVersion":"provider-sandbox-v1","taskType":"provider-sandbox","answer":"Trace Evidence links the provider call to schema validation.","summary":"Trace Evidence explains the validation trail.","riskFlags":["manual-dry-run"],"humanReviewRequired":true,"copyAllowed":false,"boundaryNotice":"Manual provider dry-run output requires Human Review and Copy Permission."}
                        """);

        ProviderValidatedResult result = validateNormalized("openai-compatible", normalized);

        org.assertj.core.api.Assertions.assertThat(result.valid()).isTrue();
        org.assertj.core.api.Assertions.assertThat(result.riskFlags()).contains("contract-validated");
    }

    @Test
    void plainTextProviderResponseIsWrappedIntoProviderSandboxSchema() throws Exception {
        ProviderResponseSchema schema = responseSchemaRegistry.get(ProviderTaskType.PROVIDER_SANDBOX);
        ProviderResponseNormalizer.NormalizedProviderResponse normalized = responseNormalizer.normalize(
                "openai-compatible",
                ProviderTaskType.PROVIDER_SANDBOX,
                schema,
                "Trace Evidence is the review trail that shows which provider checks ran.");

        ProviderValidatedResult result = validateNormalized("openai-compatible", normalized);
        JsonNode structured = objectMapper.readTree(normalized.structuredJson());

        org.assertj.core.api.Assertions.assertThat(result.valid()).isTrue();
        org.assertj.core.api.Assertions.assertThat(structured.get("schemaVersion").asText()).isEqualTo("provider-sandbox-v1");
        org.assertj.core.api.Assertions.assertThat(structured.get("taskType").asText()).isEqualTo("PROVIDER_SANDBOX");
        org.assertj.core.api.Assertions.assertThat(structured.get("answer").asText()).contains("Trace Evidence");
        org.assertj.core.api.Assertions.assertThat(structured.get("humanReviewRequired").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(structured.get("copyAllowed").asBoolean()).isFalse();
        org.assertj.core.api.Assertions.assertThat(structured.get("normalizedFromText").asBoolean()).isTrue();
    }

    @Test
    void plainTextExternalDryRunPassesValidationWithoutSavingRawResponse() throws Exception {
        when(realProviderGateway.call(any(RealProviderCallRequest.class)))
                .thenReturn(new RealProviderCallResult(
                        true,
                        "Trace Evidence is the review trail for provider contract checks.",
                        "",
                        "",
                        19));

        mockMvc.perform(post("/api/provider/real-dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("openai-compatible", true, true, "OWNER",
                                "Sanitized dry-run input.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.externalCallAttempted").value(true))
                .andExpect(jsonPath("$.externalCallBlocked").value(false))
                .andExpect(jsonPath("$.finalProvider").value("openai-compatible"))
                .andExpect(jsonPath("$.fallbackUsed").value(false))
                .andExpect(jsonPath("$.schemaValidated").value(true))
                .andExpect(jsonPath("$.riskGuardPassed").value(true))
                .andExpect(jsonPath("$.humanReviewRequired").value(true))
                .andExpect(jsonPath("$.copyAllowed").value(false))
                .andExpect(jsonPath("$.rawResponseSaved").value(false));

        verify(realProviderGateway).call(any(RealProviderCallRequest.class));
    }

    @Test
    void schemaVersionMismatchStillFallsBack() throws Exception {
        when(realProviderGateway.call(any(RealProviderCallRequest.class)))
                .thenReturn(new RealProviderCallResult(
                        true,
                        """
                                {"schemaVersion":"provider-sandbox-v0","taskType":"PROVIDER_SANDBOX","answer":"Trace Evidence records provider checks.","summary":"Trace Evidence summary.","riskFlags":[],"humanReviewRequired":true,"copyAllowed":false,"boundaryNotice":"Manual provider dry-run output requires Human Review and Copy Permission."}
                                """,
                        "",
                        "",
                        23));

        mockMvc.perform(post("/api/provider/real-dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("openai-compatible", true, true, "OWNER",
                                "Sanitized dry-run input.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.externalCallAttempted").value(true))
                .andExpect(jsonPath("$.externalCallBlocked").value(false))
                .andExpect(jsonPath("$.finalProvider").value("local-rule"))
                .andExpect(jsonPath("$.fallbackUsed").value(true))
                .andExpect(jsonPath("$.fallbackReason").value(containsString("schema_version_mismatch")))
                .andExpect(jsonPath("$.schemaValidated").value(false))
                .andExpect(jsonPath("$.humanReviewRequired").value(true))
                .andExpect(jsonPath("$.copyAllowed").value(false))
                .andExpect(jsonPath("$.rawResponseSaved").value(false));

        verify(realProviderGateway).call(any(RealProviderCallRequest.class));
    }

    @Test
    void responseDoesNotContainApiKeyOrRawResponse() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/provider/real-dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("openai-compatible", false, true, "OWNER",
                                "Sanitized dry-run input.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawResponse").doesNotExist())
                .andExpect(jsonPath("$.rawResponseSaved").value(false))
                .andExpect(content().string(not(containsString(DUMMY_KEY))))
                .andReturn();

        org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsString()).doesNotContain(DUMMY_KEY);
    }

    @Test
    void ordinaryBlockedTestsDoNotUseNetwork() throws Exception {
        dryRun("openai-compatible", false, true, "EDITOR", "Sanitized dry-run input.");

        verifyNoInteractions(realProviderGateway);
    }

    private void assertPiiBlocked(String inputText, String riskFlag) throws Exception {
        mockMvc.perform(post("/api/provider/real-dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("openai-compatible", true, true, "OWNER", inputText)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalCallAttempted").value(false))
                .andExpect(jsonPath("$.externalCallBlocked").value(true))
                .andExpect(jsonPath("$.riskFlags").value(org.hamcrest.Matchers.hasItem(riskFlag)))
                .andExpect(jsonPath("$.fallbackReason").value(containsString("PII Guard blocked")));

        verifyNoInteractions(realProviderGateway);
    }

    private JsonNode dryRun(
            String providerMode,
            boolean allowExternalCall,
            boolean confirmNoPii,
            String role,
            String inputText) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/provider/real-dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(providerMode, allowExternalCall, confirmNoPii, role, inputText)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runId").exists())
                .andExpect(jsonPath("$.traceId").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private ProviderValidatedResult validateNormalized(
            String providerMode,
            ProviderResponseNormalizer.NormalizedProviderResponse normalized) {
        ProviderTaskType taskType = ProviderTaskType.PROVIDER_SANDBOX;
        PromptContract contract = promptContractRegistry.get(taskType);
        RiskPolicy riskPolicy = riskPolicyRegistry.get(taskType);
        ProviderResponseSchema schema = responseSchemaRegistry.get(taskType);
        ProviderResponse response = new ProviderResponse(
                true,
                providerMode,
                providerMode,
                "unit-provider-model",
                normalized.outputText(),
                normalized.structuredJson(),
                false,
                "",
                "",
                "",
                10,
                "normalizer-test",
                List.of("real-dry-run", "raw-response-not-saved"),
                false,
                true);
        return responseValidator.validate(taskType, response, contract, riskPolicy, schema);
    }

    private String payload(
            String providerMode,
            boolean allowExternalCall,
            boolean confirmNoPii,
            String role,
            String inputText) {
        return """
                {
                  "providerMode": "%s",
                  "taskType": "provider-sandbox",
                  "inputText": "%s",
                  "actor": "dry-run-test",
                  "actorRole": "%s",
                  "allowExternalCall": %s,
                  "confirmNoPii": %s
                }
                """.formatted(providerMode, inputText, role, allowExternalCall, confirmNoPii);
    }
}
