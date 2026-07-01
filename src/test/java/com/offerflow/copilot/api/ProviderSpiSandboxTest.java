package com.offerflow.copilot.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offerflow.copilot.OfferFlowCopilotApplication;
import com.offerflow.copilot.persistence.repository.ProviderTraceRunRepository;
import com.offerflow.copilot.persistence.repository.TraceStepRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
        classes = OfferFlowCopilotApplication.class,
        properties = "spring.datasource.url=jdbc:h2:mem:offerflow-provider-spi;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProviderSpiSandboxTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProviderTraceRunRepository providerTraceRunRepository;

    @Autowired
    private TraceStepRepository traceStepRepository;

    @Test
    void defaultsExposeLocalRuleProviderMode() throws Exception {
        mockMvc.perform(get("/api/provider/config-check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerMode").value("local-rule"))
                .andExpect(jsonPath("$.localRuleAvailable").value(true));
    }

    @Test
    void defaultsDisableRealCallsAndRawResponseSave() throws Exception {
        mockMvc.perform(get("/api/provider/config-check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.realCallEnabled").value(false))
                .andExpect(jsonPath("$.rawResponseSave").value(false));
    }

    @Test
    void statusUsesProviderSpiConfig() throws Exception {
        mockMvc.perform(get("/api/provider/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("local-rule"))
                .andExpect(jsonPath("$.openaiCompatibleReady").value(false))
                .andExpect(jsonPath("$.deepSeekReady").value(false))
                .andExpect(jsonPath("$.boundaryNotice").value(containsString("no real external model calls")));
    }

    @Test
    void settingsExposeProviderDescriptors() throws Exception {
        mockMvc.perform(get("/api/provider/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providers", hasSize(3)))
                .andExpect(jsonPath("$.providers[0].providerMode").value("local-rule"))
                .andExpect(jsonPath("$.providers[0].configured").value(true))
                .andExpect(jsonPath("$.providers[1].providerMode").value("openai-compatible"))
                .andExpect(jsonPath("$.providers[1].configured").value(false))
                .andExpect(jsonPath("$.providers[2].providerMode").value("deepseek"))
                .andExpect(jsonPath("$.providers[2].configured").value(false));
    }

    @Test
    void unconfiguredOpenAiCompatibleFallsBackToLocalRule() throws Exception {
        mockMvc.perform(post("/api/provider/sandbox-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sandboxPayload("openai-compatible", false, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerMode").value("openai-compatible"))
                .andExpect(jsonPath("$.finalProvider").value("local-rule"))
                .andExpect(jsonPath("$.fallbackUsed").value(true))
                .andExpect(jsonPath("$.fallbackReason").value(containsString("not configured")));
    }

    @Test
    void unconfiguredDeepSeekFallsBackToLocalRule() throws Exception {
        mockMvc.perform(post("/api/provider/sandbox-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sandboxPayload("deepseek", false, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerMode").value("deepseek"))
                .andExpect(jsonPath("$.finalProvider").value("local-rule"))
                .andExpect(jsonPath("$.fallbackUsed").value(true))
                .andExpect(jsonPath("$.fallbackReason").value(containsString("not configured")));
    }

    @Test
    void simulateFailureFallsBackAndWritesTrace() throws Exception {
        JsonNode response = sandbox("openai-compatible", true, false);

        mockMvc.perform(get("/api/provider/traces/" + response.get("traceId").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fallbackReason").value(containsString("simulated failure")))
                .andExpect(jsonPath("$.pipeline[4].status").value("FALLBACK"));
    }

    @Test
    void simulateTimeoutFallsBackAndWritesTrace() throws Exception {
        JsonNode response = sandbox("deepseek", false, true);

        mockMvc.perform(get("/api/provider/traces/" + response.get("traceId").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fallbackReason").value(containsString("simulated timeout")))
                .andExpect(jsonPath("$.pipeline[4].status").value("FALLBACK"));
    }

    @Test
    void sandboxRunWritesProviderTraceRun() throws Exception {
        long before = providerTraceRunRepository.count();

        sandbox("openai-compatible", false, false);

        org.assertj.core.api.Assertions.assertThat(providerTraceRunRepository.count()).isGreaterThan(before);
    }

    @Test
    void sandboxRunWritesEightTraceSteps() throws Exception {
        JsonNode response = sandbox("deepseek", false, false);

        org.assertj.core.api.Assertions.assertThat(traceStepRepository.findByRunId(response.get("traceId").asText()))
                .hasSize(8);
    }

    @Test
    void traceTimelineContainsRequiredProviderEvidenceSteps() throws Exception {
        JsonNode response = sandbox("openai-compatible", true, false);

        mockMvc.perform(get("/api/provider/traces/" + response.get("traceId").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipeline", hasSize(8)))
                .andExpect(jsonPath("$.pipeline[0].label").value("Provider Config Check"))
                .andExpect(jsonPath("$.pipeline[1].label").value("Prompt Build"))
                .andExpect(jsonPath("$.pipeline[2].label").value("Provider Select"))
                .andExpect(jsonPath("$.pipeline[3].label").value("Provider No-op"))
                .andExpect(jsonPath("$.pipeline[4].label").value("Fallback Decision"))
                .andExpect(jsonPath("$.pipeline[5].label").value("Schema Validate"))
                .andExpect(jsonPath("$.pipeline[6].label").value("Risk Guard"))
                .andExpect(jsonPath("$.pipeline[7].label").value("Human Review Required"));
    }

    @Test
    void traceContainsFallbackReason() throws Exception {
        JsonNode response = sandbox("deepseek", false, false);

        mockMvc.perform(get("/api/provider/traces/" + response.get("traceId").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fallbackReason").value(containsString("deepseek is not configured")))
                .andExpect(jsonPath("$.evidenceDetail.fallbackReason").value(containsString("deepseek is not configured")));
    }

    @Test
    void sandboxRunRecordsNoExternalNetworkRequest() throws Exception {
        JsonNode response = sandbox("openai-compatible", false, false);

        mockMvc.perform(get("/api/provider/traces/" + response.get("traceId").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipeline[3].outputSummary").value("No external request sent."))
                .andExpect(jsonPath("$.technicalTags[2]").value("No external model call"));
    }

    @Test
    void localRuleSandboxRunDoesNotRequireFallback() throws Exception {
        mockMvc.perform(post("/api/provider/sandbox-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sandboxPayload("local-rule", false, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerMode").value("local-rule"))
                .andExpect(jsonPath("$.finalProvider").value("local-rule"))
                .andExpect(jsonPath("$.fallbackUsed").value(false))
                .andExpect(jsonPath("$.rawResponseSaved").value(false))
                .andExpect(jsonPath("$.humanReviewRequired").value(true));
    }

    private JsonNode sandbox(String providerMode, boolean simulateFailure, boolean simulateTimeout) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/provider/sandbox-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sandboxPayload(providerMode, simulateFailure, simulateTimeout)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.traceId").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String sandboxPayload(String providerMode, boolean simulateFailure, boolean simulateTimeout) {
        return """
                {
                  "taskType": "provider-sandbox",
                  "inputText": "Java Spring Boot sandbox run. No offer probability, no auto apply.",
                  "providerMode": "%s",
                  "simulateFailure": %s,
                  "simulateTimeout": %s,
                  "actor": "provider-spi-test",
                  "actorRole": "System"
                }
                """.formatted(providerMode, simulateFailure, simulateTimeout);
    }
}
