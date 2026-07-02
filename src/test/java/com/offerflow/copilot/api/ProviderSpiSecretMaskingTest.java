package com.offerflow.copilot.api;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.offerflow.copilot.OfferFlowCopilotApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        classes = OfferFlowCopilotApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:offerflow-provider-mask;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                "offerflow.ai.openai-compatible.base-url=https://unit.invalid/openai",
                "offerflow.ai.openai-compatible.api-key=test-openai-secret-value",
                "offerflow.ai.openai-compatible.model=unit-openai-model",
                "offerflow.ai.deepseek.base-url=https://unit.invalid/deepseek",
                "offerflow.ai.deepseek.api-key=test-deepseek-secret-value",
                "offerflow.ai.deepseek.model=unit-deepseek-model"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProviderSpiSecretMaskingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void configCheckDoesNotLeakConfiguredApiKeys() throws Exception {
        mockMvc.perform(get("/api/provider/config-check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openAiCompatibleConfigured").value(true))
                .andExpect(jsonPath("$.deepSeekConfigured").value(true))
                .andExpect(jsonPath("$.apiKeyStatus['openai-compatible']").value("masked"))
                .andExpect(jsonPath("$.apiKeyStatus.deepseek").value("masked"))
                .andExpect(content().string(not(containsString("test-openai-secret-value"))))
                .andExpect(content().string(not(containsString("test-deepseek-secret-value"))));
    }

    @Test
    void settingsDoesNotLeakConfiguredApiKeys() throws Exception {
        mockMvc.perform(get("/api/provider/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providers[1].configured").value(true))
                .andExpect(jsonPath("$.providers[1].apiKeyStatus").value("masked"))
                .andExpect(jsonPath("$.providers[2].configured").value(true))
                .andExpect(jsonPath("$.providers[2].apiKeyStatus").value("masked"))
                .andExpect(content().string(not(containsString("test-openai-secret-value"))))
                .andExpect(content().string(not(containsString("test-deepseek-secret-value"))));
    }
}
