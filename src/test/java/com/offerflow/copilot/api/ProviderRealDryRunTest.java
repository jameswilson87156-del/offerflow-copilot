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
import com.offerflow.copilot.provider.RealProviderCallRequest;
import com.offerflow.copilot.provider.RealProviderCallResult;
import com.offerflow.copilot.provider.RealProviderGateway;
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
