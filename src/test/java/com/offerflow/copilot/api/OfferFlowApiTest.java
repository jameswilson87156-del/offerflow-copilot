package com.offerflow.copilot.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.offerflow.copilot.OfferFlowCopilotApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = OfferFlowCopilotApplication.class)
@AutoConfigureMockMvc
class OfferFlowApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthReportsMockLocalRuleBoundary() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.dataMode").value("mock/local-rule"));
    }

    @Test
    void providerDoesNotClaimRealCalls() throws Exception {
        mockMvc.perform(get("/api/provider/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("local-rule"))
                .andExpect(jsonPath("$.openaiCompatibleReady").value(false))
                .andExpect(jsonPath("$.deepSeekReady").value(false))
                .andExpect(jsonPath("$.realCallEnabled").value(false))
                .andExpect(jsonPath("$.fallback").value("local-rule"));
    }

    @Test
    void dashboardReturnsDemoCounts() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingJobDescriptions").value(3))
                .andExpect(jsonPath("$.pendingHumanReviews").value(2))
                .andExpect(jsonPath("$.recentApplications").value(5));
    }

    @Test
    void demoAnalysisContainsEvidenceChainAndScoreBreakdown() throws Exception {
        mockMvc.perform(get("/api/jobs/demo-analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requirementGroups", hasSize(3)))
                .andExpect(jsonPath("$.evidenceMatches", hasSize(4)))
                .andExpect(jsonPath("$.evidenceMatches[0].project").value("MCP Tool Gateway"))
                .andExpect(jsonPath("$.score.total").value(82))
                .andExpect(jsonPath("$.score.items[0].value").value(36))
                .andExpect(jsonPath("$.score.items[2].value").value(-4))
                .andExpect(jsonPath("$.humanReview.aiOutputStatus").value("Draft"))
                .andExpect(jsonPath("$.humanReview.copyAllowed").value(false))
                .andExpect(jsonPath("$.timeline", hasSize(5)));
    }
}
