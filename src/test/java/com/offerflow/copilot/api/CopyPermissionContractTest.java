package com.offerflow.copilot.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.offerflow.copilot.OfferFlowCopilotApplication;
import com.offerflow.copilot.copy.CopyPermissionPolicy;
import com.offerflow.copilot.copy.CopyPermissionRequest;
import com.offerflow.copilot.copy.CopyPermissionResult;
import com.offerflow.copilot.copy.CopyTargetSnapshot;
import com.offerflow.copilot.copy.CopyTargetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        classes = OfferFlowCopilotApplication.class,
        properties = "spring.datasource.url=jdbc:h2:mem:offerflow-copy;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=0;DB_CLOSE_ON_EXIT=false")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class CopyPermissionContractTest {

    @Autowired
    private MockMvc mockMvc;

    private final CopyPermissionPolicy policy = new CopyPermissionPolicy();

    @Test
    void policyAllowsOnlyConfirmedTargets() {
        CopyPermissionResult result = policy.evaluate(request(), snapshot("CONFIRMED", "Confirmed", true, true));

        assertThat(result.allowed()).isTrue();
        assertThat(result.confirmed()).isTrue();
        assertThat(result.reason()).isEqualTo("已通过人工复核，可复制使用。");
    }

    @ParameterizedTest
    @ValueSource(strings = {"DRAFT", "IN_REVIEW", "RETURNED", "RISK_FLAGGED", "ARCHIVED"})
    void policyBlocksEveryNonConfirmedTargetStatus(String status) {
        CopyPermissionResult result = policy.evaluate(request(), snapshot(status, "Confirmed", true, true));

        assertThat(result.allowed()).isFalse();
        assertThat(result.targetStatus()).isEqualTo(status);
    }

    @Test
    void policyBlocksWhenSchemaValidationDidNotPass() {
        CopyPermissionResult result = policy.evaluate(request(), snapshot("CONFIRMED", "Confirmed", false, true));

        assertThat(result.allowed()).isFalse();
        assertThat(result.reason()).isEqualTo("Schema Validate 未通过");
    }

    @Test
    void policyBlocksWhenRiskGuardDidNotPass() {
        CopyPermissionResult result = policy.evaluate(request(), snapshot("CONFIRMED", "Confirmed", true, false));

        assertThat(result.allowed()).isFalse();
        assertThat(result.reason()).isEqualTo("Risk Guard 未通过");
    }

    @Test
    void policyBlocksWhenHumanReviewIsNotConfirmed() {
        CopyPermissionResult result = policy.evaluate(request(), snapshot("CONFIRMED", "Draft", true, true));

        assertThat(result.allowed()).isFalse();
        assertThat(result.reason()).isEqualTo("Human Review 尚未 Confirmed");
    }

    @Test
    void copyPermissionCheckWritesAuditEvent() throws Exception {
        mockMvc.perform(post("/api/reviews/review-match-java-ai/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"humanNote\":\"confirm before unified copy\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/copy-permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType":"MATCH_REPORT",
                                  "targetId":"match-java-ai-demo-v1",
                                  "actor":"demo-reviewer",
                                  "actorRole":"Human reviewer",
                                  "requestedText":"Confirmed local-rule summary",
                                  "traceId":"JD-042-REP-21F3"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.targetType").value("MATCH_REPORT"))
                .andExpect(jsonPath("$.targetStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.humanReviewStatus").value("Confirmed"))
                .andExpect(jsonPath("$.schemaValidated").value(true))
                .andExpect(jsonPath("$.riskGuardPassed").value(true))
                .andExpect(jsonPath("$.confirmed").value(true))
                .andExpect(jsonPath("$.auditEventId").exists())
                .andExpect(jsonPath("$.apiKey").doesNotExist())
                .andExpect(jsonPath("$.rawResponse").doesNotExist());

        mockMvc.perform(get("/api/copy-permissions/audit-events")
                        .param("targetType", "MATCH_REPORT")
                        .param("targetId", "match-java-ai-demo-v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].action").value("COPY_ALLOWED"))
                .andExpect(jsonPath("$[0].allowed").value(true))
                .andExpect(jsonPath("$[0].actor").value("demo-reviewer"));
    }

    @Test
    void copyPermissionAuditEventsEndpointReturnsHistory() throws Exception {
        mockMvc.perform(post("/api/copy-permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"INTERVIEW_PREP\",\"targetId\":\"interview-java-ai-demo\",\"actor\":\"demo-user\",\"actorRole\":\"Human reviewer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.targetStatus").value("DRAFT"))
                .andExpect(jsonPath("$.reason").value("需要人工复核"));

        mockMvc.perform(get("/api/copy-permissions/audit-events")
                        .param("targetType", "INTERVIEW_PREP")
                        .param("targetId", "interview-java-ai-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].targetType").value("INTERVIEW_PREP"))
                .andExpect(jsonPath("$[0].action").value("COPY_BLOCKED"))
                .andExpect(jsonPath("$[0].schemaValidated").value(true))
                .andExpect(jsonPath("$[0].riskGuardPassed").value(true));
    }

    @Test
    void matchReportCopyCheckReusesUnifiedCopyPermissionService() throws Exception {
        mockMvc.perform(post("/api/match-reports/match-java-ai-demo-v1/copy-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-reviewer\",\"actorRole\":\"Human reviewer\",\"humanNote\":\"old endpoint unified copy check\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.targetType").value("MATCH_REPORT"))
                .andExpect(jsonPath("$.schemaValidated").value(true))
                .andExpect(jsonPath("$.riskGuardPassed").value(true))
                .andExpect(jsonPath("$.auditEventId").exists());

        mockMvc.perform(get("/api/copy-permissions/audit-events")
                        .param("targetType", "MATCH_REPORT")
                        .param("targetId", "match-java-ai-demo-v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].action").value("COPY_BLOCKED"))
                .andExpect(jsonPath("$[0].reason").value("需要人工复核"));
    }

    @Test
    void copyPermissionDoesNotEnableRealProviderNetworkCalls() throws Exception {
        mockMvc.perform(post("/api/copy-permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"PROVIDER_SANDBOX_OUTPUT\",\"targetId\":\"sandbox-draft\",\"providerRunId\":\"P4D-demo-run\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.copyText").value(""));

        mockMvc.perform(get("/api/provider/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.realCallEnabled").value(false))
                .andExpect(jsonPath("$.rawResponseSave").value(false));
    }

    private CopyPermissionRequest request() {
        return new CopyPermissionRequest(
                CopyTargetType.MATCH_REPORT,
                "match-demo",
                "demo-reviewer",
                "Human reviewer",
                "confirmed summary",
                "",
                "TRACE-demo",
                "schema-v1",
                "prompt-v1");
    }

    private CopyTargetSnapshot snapshot(String targetStatus, String humanReviewStatus, boolean schemaValidated, boolean riskGuardPassed) {
        return new CopyTargetSnapshot(
                CopyTargetType.MATCH_REPORT,
                "match-demo",
                targetStatus,
                humanReviewStatus,
                schemaValidated,
                riskGuardPassed,
                "TRACE-demo",
                "",
                "schema-v1",
                "prompt-v1",
                "confirmed summary");
    }
}
