package com.offerflow.copilot.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.offerflow.copilot.OfferFlowCopilotApplication;
import com.offerflow.copilot.security.local.LocalActorContext;
import com.offerflow.copilot.security.local.LocalActorRole;
import com.offerflow.copilot.security.local.LocalPermissionPolicy;
import com.offerflow.copilot.security.local.PermissionAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        classes = OfferFlowCopilotApplication.class,
        properties = "spring.datasource.url=jdbc:h2:mem:offerflow-permission;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=0;DB_CLOSE_ON_EXIT=false")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class LocalPermissionWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocalPermissionPolicy permissionPolicy;

    @Test
    void ownerAllowsArchiveRestoreAndConfirm() throws Exception {
        mockMvc.perform(post("/api/match-reports/match-java-ai-demo-v1/archive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-owner\",\"actorRole\":\"OWNER\",\"humanNote\":\"owner archive\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        mockMvc.perform(post("/api/match-reports/match-java-ai-demo-v1/restore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-owner\",\"actorRole\":\"OWNER\",\"humanNote\":\"owner restore\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        mockMvc.perform(post("/api/reviews/review-opening-boss/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-owner\",\"actorRole\":\"OWNER\",\"humanNote\":\"owner confirm\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Confirmed"));

        mockMvc.perform(get("/api/permissions/audit-events")
                        .param("actor", "demo-owner")
                        .param("allowed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.action == 'MATCH_REPORT_ARCHIVE')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.action == 'MATCH_REPORT_RESTORE')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.action == 'REVIEW_CONFIRM')]", hasSize(1)));
    }

    @Test
    void reviewerAllowsHumanReviewConfirmReturnAndFlagRisk() throws Exception {
        mockMvc.perform(post("/api/reviews/review-opening-boss/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-reviewer\",\"actorRole\":\"REVIEWER\",\"humanNote\":\"review confirm\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Confirmed"));

        mockMvc.perform(post("/api/reviews/review-returned-devops/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-reviewer\",\"actorRole\":\"REVIEWER\",\"humanNote\":\"review return\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Returned"));

        mockMvc.perform(post("/api/reviews/review-risk-model/flag-risk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-reviewer\",\"actorRole\":\"REVIEWER\",\"humanNote\":\"review risk\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Risk Flagged"));
    }

    @Test
    void reviewerCannotArchiveEvidenceAndDeniedAuditIsWritten() throws Exception {
        mockMvc.perform(post("/api/evidence/evidence-mcp/archive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-reviewer\",\"actorRole\":\"REVIEWER\",\"humanNote\":\"should deny\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.action").value("EVIDENCE_ARCHIVE"))
                .andExpect(jsonPath("$.reason").value("REVIEWER is not allowed to perform EVIDENCE_ARCHIVE in demo local permission mode."));

        mockMvc.perform(get("/api/permissions/audit-events")
                        .param("actor", "demo-reviewer")
                        .param("action", "EVIDENCE_ARCHIVE")
                        .param("targetType", "EVIDENCE")
                        .param("targetId", "evidence-mcp")
                        .param("allowed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].actorRole").value("REVIEWER"));
    }

    @Test
    void editorAllowsJdCreateParseAndBindEvidence() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Manual Permission JD",
                                  "company":"Demo Company",
                                  "city":"Shanghai",
                                  "jdText":"Java Spring Boot local rule role",
                                  "sourceType":"MANUAL_PASTE",
                                  "sourceNote":"manual paste only",
                                  "actor":"demo-editor",
                                  "actorRole":"EDITOR",
                                  "humanNote":"editor create jd"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.job.title").value("Manual Permission JD"));

        mockMvc.perform(post("/api/jobs/job-java-ai-intern/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-editor\",\"actorRole\":\"EDITOR\",\"humanNote\":\"editor parse\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentParseVersion.parserMode").value("local-rule"));

        mockMvc.perform(post("/api/jobs/job-java-ai-intern/bind-evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-editor\",\"actorRole\":\"EDITOR\",\"humanNote\":\"editor bind\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.job.status").value("Bound"));

        mockMvc.perform(get("/api/permissions/audit-events")
                        .param("actor", "demo-editor")
                        .param("allowed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.action == 'JD_CREATE')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.action == 'JD_PARSE')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.action == 'JD_BIND_EVIDENCE')]", hasSize(1)));
    }

    @Test
    void editorCannotConfirmHumanReview() throws Exception {
        mockMvc.perform(post("/api/reviews/review-opening-boss/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-editor\",\"actorRole\":\"EDITOR\",\"humanNote\":\"editor cannot confirm\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.action").value("REVIEW_CONFIRM"));
    }

    @Test
    void viewerCannotPerformAnyWritePermissionAction() {
        LocalActorContext viewer = new LocalActorContext("demo-viewer", LocalActorRole.VIEWER, "test", "test");
        for (PermissionAction action : PermissionAction.values()) {
            assertThat(permissionPolicy.decide(viewer, action, "TEST", "target").allowed()).isFalse();
        }
    }

    @Test
    void systemCannotActAsHumanConfirmActor() throws Exception {
        mockMvc.perform(post("/api/reviews/review-opening-boss/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"local-rule-system\",\"actorRole\":\"SYSTEM\",\"humanNote\":\"system confirm\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.reason").value("SYSTEM cannot act as a human reviewer for final review actions."));
    }

    @Test
    void permissionCheckReturnsDecisionAndAuditHistory() throws Exception {
        mockMvc.perform(post("/api/permissions/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actor":"demo-editor",
                                  "actorRole":"EDITOR",
                                  "action":"REVIEW_CONFIRM",
                                  "targetType":"HUMAN_REVIEW",
                                  "targetId":"review-opening-boss"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.actorRole").value("EDITOR"))
                .andExpect(jsonPath("$.boundaryNotice").exists());

        mockMvc.perform(get("/api/permissions/audit-events")
                        .param("actor", "demo-editor")
                        .param("action", "REVIEW_CONFIRM")
                        .param("allowed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].targetType").value("HUMAN_REVIEW"));
    }

    @Test
    void currentActorDoesNotExposeSecretsOrRealIdentityData() throws Exception {
        mockMvc.perform(get("/api/permissions/current-actor")
                        .header("X-Demo-Actor", "demo.owner")
                        .header("X-Demo-Role", "OWNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actor").value("demo.owner"))
                .andExpect(jsonPath("$.actorRole").value("OWNER"))
                .andExpect(jsonPath("$.permissions", hasItem("REVIEW_CONFIRM")))
                .andExpect(jsonPath("$.boundaryNotice").exists())
                .andExpect(jsonPath("$.apiKey").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.phone").doesNotExist());
    }

    @Test
    void existingBusinessAuditStillWritesBesidePermissionAudit() throws Exception {
        mockMvc.perform(post("/api/reviews/review-opening-boss/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-reviewer\",\"actorRole\":\"REVIEWER\",\"humanNote\":\"confirm with both audits\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reviews/review-opening-boss/audit-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.action == 'CONFIRM')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.humanNote == 'confirm with both audits')]", hasSize(1)));

        mockMvc.perform(get("/api/permissions/audit-events")
                        .param("actor", "demo-reviewer")
                        .param("action", "REVIEW_CONFIRM")
                        .param("allowed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
