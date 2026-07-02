package com.offerflow.copilot.api;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.offerflow.copilot.OfferFlowCopilotApplication;
import com.offerflow.copilot.provider.RealProviderGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        classes = OfferFlowCopilotApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:offerflow-real-dry-run-disabled;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                "offerflow.ai.provider.real-call-enabled=false",
                "offerflow.ai.provider.raw-response-save=false",
                "offerflow.ai.openai-compatible.base-url=https://unit.invalid/v1",
                "offerflow.ai.openai-compatible.api-key=dummy-openai-compatible-dry-run-key",
                "offerflow.ai.openai-compatible.model=unit-openai-model"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProviderRealDryRunDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RealProviderGateway realProviderGateway;

    @Test
    void realCallDisabledBlocksExternalRequestAndFallsBack() throws Exception {
        mockMvc.perform(post("/api/provider/real-dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("openai-compatible", true, true, "OWNER",
                                "Sanitized Java Spring Boot dry-run input.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalCallAttempted").value(false))
                .andExpect(jsonPath("$.externalCallBlocked").value(true))
                .andExpect(jsonPath("$.finalProvider").value("local-rule"))
                .andExpect(jsonPath("$.fallbackUsed").value(true))
                .andExpect(jsonPath("$.fallbackReason").value("realCallEnabled=false; fallback to local-rule."))
                .andExpect(jsonPath("$.schemaValidated").value(true))
                .andExpect(jsonPath("$.riskGuardPassed").value(true))
                .andExpect(jsonPath("$.rawResponseSaved").value(false));

        verifyNoInteractions(realProviderGateway);
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
