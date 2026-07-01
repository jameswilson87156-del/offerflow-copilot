package com.offerflow.copilot.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.offerflow.copilot.OfferFlowCopilotApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        classes = OfferFlowCopilotApplication.class,
        properties = "spring.datasource.url=jdbc:h2:mem:offerflow-api;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=0;DB_CLOSE_ON_EXIT=false")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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
    void providerSettingsExposeFallbackBoundaryWithoutKeys() throws Exception {
        mockMvc.perform(get("/api/provider/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock/local-rule"))
                .andExpect(jsonPath("$.currentStatus").value("local-rule fallback active"))
                .andExpect(jsonPath("$.providers", hasSize(3)))
                .andExpect(jsonPath("$.providers[0].name").value("local-rule fallback"))
                .andExpect(jsonPath("$.providers[0].realCallEnabled").value(false))
                .andExpect(jsonPath("$.providers[1].status").value("Not configured"))
                .andExpect(jsonPath("$.providers[1].apiKeyStatus").value("masked / not configured"))
                .andExpect(jsonPath("$.providers[2].status").value("Not configured"))
                .andExpect(jsonPath("$.providers[2].realCallEnabled").value(false))
                .andExpect(jsonPath("$.safetyBoundaries", hasSize(5)));
    }

    @Test
    void providerTracesReturnMockRunIndex() throws Exception {
        mockMvc.perform(get("/api/provider/traces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock/local-rule"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].runId").value("JD-20260701-143522-9E4D"))
                .andExpect(jsonPath("$.items[0].finalProvider").value("local-rule fallback"))
                .andExpect(jsonPath("$.items[0].humanReviewStatus").value("待人工确认"));
    }

    @Test
    void providerTraceDetailExplainsFallbackAndEvidence() throws Exception {
        mockMvc.perform(get("/api/provider/traces/JD-20260701-143522-9E4D"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runId").value("JD-20260701-143522-9E4D"))
                .andExpect(jsonPath("$.finalProvider").value("local-rule fallback"))
                .andExpect(jsonPath("$.model").value("local-rule-engine v2.1"))
                .andExpect(jsonPath("$.riskFlags", hasSize(2)))
                .andExpect(jsonPath("$.pipeline", hasSize(10)))
                .andExpect(jsonPath("$.pipeline[3].status").value("fallback"))
                .andExpect(jsonPath("$.evidenceDetail.resumeEvidence", hasSize(3)))
                .andExpect(jsonPath("$.technicalTags", hasSize(10)));
    }

    @Test
    void matchReportExplainsScoreEvidenceAndReviewGate() throws Exception {
        mockMvc.perform(get("/api/match-report/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock/local-rule"))
                .andExpect(jsonPath("$.summary.jobTitle").value("Java 后端 / AI 应用开发实习生"))
                .andExpect(jsonPath("$.summary.totalScore").value(82))
                .andExpect(jsonPath("$.summary.status").value("Draft，需要人工复核"))
                .andExpect(jsonPath("$.score.items", hasSize(4)))
                .andExpect(jsonPath("$.evidenceSources", hasSize(4)))
                .andExpect(jsonPath("$.skillGaps", hasSize(5)))
                .andExpect(jsonPath("$.recommendedActions", hasSize(4)))
                .andExpect(jsonPath("$.traceEvidence", hasSize(6)));
    }

    @Test
    void interviewPrepIsPreInterviewOnly() throws Exception {
        mockMvc.perform(get("/api/interview-prep/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock/local-rule"))
                .andExpect(jsonPath("$.positioningNotice").value("面试前准备与复盘，不是实时面试辅助工具。"))
                .andExpect(jsonPath("$.focusAreas", hasSize(6)))
                .andExpect(jsonPath("$.questionGroups", hasSize(5)))
                .andExpect(jsonPath("$.starDraft.riskNote").value("不要声称真实稳定 LLM、企业客户、真实用户或生产级能力。"))
                .andExpect(jsonPath("$.riskReminders", hasSize(4)))
                .andExpect(jsonPath("$.reviewTimeline", hasSize(5)));
    }

    @Test
    void applicationTrackerUsesManualMockRecords() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock/local-rule"))
                .andExpect(jsonPath("$.boardColumns", hasSize(6)))
                .andExpect(jsonPath("$.applications", hasSize(3)))
                .andExpect(jsonPath("$.applications[0].company").value("科技创新公司"))
                .andExpect(jsonPath("$.applications[0].resumeVersion").value("Java 后端版"))
                .andExpect(jsonPath("$.communicationLogs", hasSize(5)))
                .andExpect(jsonPath("$.riskBoundaries", hasSize(4)));
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

    @Test
    void jobsListReturnsSeededManualJdIntake() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock/local-rule"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value("job-java-ai-intern"))
                .andExpect(jsonPath("$.items[0].sourceType").value("MANUAL_PASTE"))
                .andExpect(jsonPath("$.items[0].status").value("Bound"))
                .andExpect(jsonPath("$.items[0].currentVersion").value(1))
                .andExpect(jsonPath("$.items[0].bindingCount").value(4));
    }

    @Test
    void jobDetailReturnsParseBindingsAndAuditTrail() throws Exception {
        mockMvc.perform(get("/api/jobs/job-java-ai-intern"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock/local-rule"))
                .andExpect(jsonPath("$.job.id").value("job-java-ai-intern"))
                .andExpect(jsonPath("$.job.sourceType").value("MANUAL_PASTE"))
                .andExpect(jsonPath("$.currentParseVersion.versionNo").value(1))
                .andExpect(jsonPath("$.requirementGroups", hasSize(3)))
                .andExpect(jsonPath("$.evidenceBindings", hasSize(4)))
                .andExpect(jsonPath("$.auditTrail", hasSize(3)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createManualJobWritesJdAuditEvent() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Manual Java Intern\",\"company\":\"Demo Company\",\"city\":\"Shanghai\",\"jdText\":\"Java Spring Boot role, contact 13812345678 or hr@example.com\",\"sourceType\":\"MANUAL_PASTE\",\"sourceNote\":\"manual paste only\",\"actor\":\"demo-jd-editor\",\"actorRole\":\"JD reviewer\",\"humanNote\":\"create manual jd\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.job.title").value("Manual Java Intern"))
                .andExpect(jsonPath("$.job.sourceType").value("MANUAL_PASTE"))
                .andExpect(jsonPath("$.job.status").value("Draft"))
                .andExpect(jsonPath("$.job.jdText").value("Java Spring Boot role, contact [redacted-phone] or [redacted-email]"))
                .andExpect(jsonPath("$.auditTrail", hasSize(1)))
                .andExpect(jsonPath("$.auditTrail[0].action").value("CREATE_JD"))
                .andExpect(jsonPath("$.auditTrail[0].humanNote").value("create manual jd"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateManualJobWritesJdAuditEvent() throws Exception {
        mockMvc.perform(put("/api/jobs/job-java-ai-intern")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java AI 应用开发实习生 Updated\",\"company\":\"匿名演示公司\",\"city\":\"上海\",\"jdText\":\"Java Spring Boot RAG MCP local rule updated\",\"sourceNote\":\"manual paste updated\",\"actor\":\"demo-jd-editor\",\"actorRole\":\"JD reviewer\",\"humanNote\":\"update manual jd\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.job.title").value("Java AI 应用开发实习生 Updated"))
                .andExpect(jsonPath("$.auditTrail[?(@.action == 'UPDATE_JD')]", hasSize(1)))
                .andExpect(jsonPath("$.auditTrail[?(@.humanNote == 'update manual jd')]", hasSize(1)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void parseManualJobCreatesParseVersion() throws Exception {
        mockMvc.perform(post("/api/jobs/job-java-ai-intern/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-jd-editor\",\"actorRole\":\"JD reviewer\",\"humanNote\":\"parse local rule\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentParseVersion.versionNo").value(2))
                .andExpect(jsonPath("$.currentParseVersion.parserMode").value("local-rule"))
                .andExpect(jsonPath("$.parseVersions", hasSize(2)))
                .andExpect(jsonPath("$.auditTrail[?(@.action == 'PARSE_LOCAL_RULE')]", hasSize(2)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void bindEvidenceCreatesBindingsAndAuditEvent() throws Exception {
        mockMvc.perform(post("/api/jobs/job-java-ai-intern/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-jd-editor\",\"actorRole\":\"JD reviewer\",\"humanNote\":\"parse before bind\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/jobs/job-java-ai-intern/bind-evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-jd-editor\",\"actorRole\":\"JD reviewer\",\"humanNote\":\"bind local rule\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.job.status").value("Bound"))
                .andExpect(jsonPath("$.evidenceBindings", hasSize(4)))
                .andExpect(jsonPath("$.auditTrail[?(@.action == 'BIND_EVIDENCE')]", hasSize(2)));
    }

    @Test
    void jobParseVersionsEndpointReturnsHistory() throws Exception {
        mockMvc.perform(get("/api/jobs/job-java-ai-intern/parse-versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].versionNo").value(1))
                .andExpect(jsonPath("$[0].schemaVersion").value("jd-intake-v1"))
                .andExpect(jsonPath("$[0].parseStatus").value("success"));
    }

    @Test
    void jobAuditAndBindingEndpointsReturnHistory() throws Exception {
        mockMvc.perform(get("/api/jobs/job-java-ai-intern/audit-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].action").value("CREATE_JD"))
                .andExpect(jsonPath("$[2].action").value("BIND_EVIDENCE"));

        mockMvc.perform(get("/api/jobs/job-java-ai-intern/evidence-bindings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].evidenceProject").value("MCP Tool Gateway"));
    }

    @Test
    void evidenceLibraryReturnsReviewableProjectEvidence() throws Exception {
        mockMvc.perform(get("/api/evidence/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock/local-rule"))
                .andExpect(jsonPath("$.items", hasSize(4)))
                .andExpect(jsonPath("$.categories", hasSize(10)))
                .andExpect(jsonPath("$.items[0].projectName").value("MCP Tool Gateway"))
                .andExpect(jsonPath("$.items[0].credibility").value("强"))
                .andExpect(jsonPath("$.items[0].detail.sourceChain", hasSize(5)))
                .andExpect(jsonPath("$.items[2].humanReviewStatus").value("Needs review"));
    }

    @Test
    void evidenceCoverageExplainsSupportAndGaps() throws Exception {
        mockMvc.perform(get("/api/evidence/coverage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock/local-rule"))
                .andExpect(jsonPath("$.items", hasSize(8)))
                .andExpect(jsonPath("$.items[0].skill").value("Java"))
                .andExpect(jsonPath("$.items[0].level").value("强支撑"))
                .andExpect(jsonPath("$.items[7].level").value("弱支撑"))
                .andExpect(jsonPath("$.items[7].gap").value("仅有演示部署，不代表生产运维"));
    }

    @Test
    void evidenceDetailReturnsAuditTrail() throws Exception {
        mockMvc.perform(get("/api/evidence/evidence-mcp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock/local-rule"))
                .andExpect(jsonPath("$.item.projectName").value("MCP Tool Gateway"))
                .andExpect(jsonPath("$.item.status").value("Confirmed"))
                .andExpect(jsonPath("$.auditTrail", hasSize(1)))
                .andExpect(jsonPath("$.auditTrail[0].action").value("CONFIRM"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createEvidenceDraftWritesResumeEvidenceAndAuditEvent() throws Exception {
        mockMvc.perform(post("/api/evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-evidence-editor\",\"actorRole\":\"Evidence reviewer\",\"humanNote\":\"create draft note\",\"projectName\":\"Demo Evidence Draft\",\"summary\":\"Draft evidence summary\",\"abilityTags\":[\"Java backend\"],\"evidenceSources\":[\"README\"],\"credibility\":\"medium\",\"matchableRequirements\":[\"Spring Boot\"],\"boundaryNote\":\"No real privacy.\",\"relatedSkills\":[\"Java\"],\"riskBoundaries\":[\"No real user data\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.projectName").value("Demo Evidence Draft"))
                .andExpect(jsonPath("$.item.status").value("Draft"))
                .andExpect(jsonPath("$.auditTrail", hasSize(1)))
                .andExpect(jsonPath("$.auditTrail[0].action").value("CREATE_DRAFT"))
                .andExpect(jsonPath("$.auditTrail[0].humanNote").value("create draft note"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateEvidenceDraftWritesAuditEventWithChangedFields() throws Exception {
        mockMvc.perform(put("/api/evidence/evidence-rag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-evidence-editor\",\"actorRole\":\"Evidence reviewer\",\"humanNote\":\"update draft note\",\"evidenceSources\":[\"README\",\"Trace\",\"Evaluation\"],\"boundaryNote\":\"Keep as anonymized demo evidence.\",\"relatedSkills\":[\"RAG\",\"Trace\",\"Citation\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.status").value("Draft"))
                .andExpect(jsonPath("$.auditTrail[?(@.humanNote == 'update draft note')]", hasSize(1)))
                .andExpect(jsonPath("$.auditTrail[?(@.humanNote == 'update draft note')].action").value("UPDATE_DRAFT"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void confirmEvidenceUpdatesStatusAndWritesAuditEvent() throws Exception {
        mockMvc.perform(post("/api/evidence/evidence-rag/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-evidence-editor\",\"actorRole\":\"Evidence reviewer\",\"humanNote\":\"confirm evidence note\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.status").value("Confirmed"))
                .andExpect(jsonPath("$.auditTrail[?(@.humanNote == 'confirm evidence note')]", hasSize(1)))
                .andExpect(jsonPath("$.auditTrail[?(@.humanNote == 'confirm evidence note')].action").value("CONFIRM"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void archiveEvidenceUpdatesStatusAndWritesAuditEvent() throws Exception {
        mockMvc.perform(post("/api/evidence/evidence-mcp/archive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-evidence-editor\",\"actorRole\":\"Evidence reviewer\",\"humanNote\":\"archive evidence note\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.status").value("Archived"))
                .andExpect(jsonPath("$.auditTrail[?(@.humanNote == 'archive evidence note')]", hasSize(1)))
                .andExpect(jsonPath("$.auditTrail[?(@.humanNote == 'archive evidence note')].action").value("ARCHIVE"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void restoreEvidenceUpdatesStatusAndWritesAuditEvent() throws Exception {
        mockMvc.perform(post("/api/evidence/evidence-mcp/archive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-evidence-editor\",\"actorRole\":\"Evidence reviewer\",\"humanNote\":\"archive before restore\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/evidence/evidence-mcp/restore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-evidence-editor\",\"actorRole\":\"Evidence reviewer\",\"humanNote\":\"restore evidence note\",\"targetStatus\":\"Draft\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.status").value("Draft"))
                .andExpect(jsonPath("$.auditTrail[?(@.humanNote == 'restore evidence note')]", hasSize(1)))
                .andExpect(jsonPath("$.auditTrail[?(@.humanNote == 'restore evidence note')].action").value("RESTORE"));
    }

    @Test
    void evidenceAuditEventsEndpointReturnsHistory() throws Exception {
        mockMvc.perform(get("/api/evidence/evidence-mcp/audit-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].evidenceId").value("evidence-mcp"))
                .andExpect(jsonPath("$[0].action").value("CONFIRM"))
                .andExpect(jsonPath("$[0].actor").value("demo-evidence-editor"));
    }

    @Test
    void humanReviewCenterReturnsReviewQueue() throws Exception {
        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock/local-rule"))
                .andExpect(jsonPath("$.pendingReviewCount").value(12))
                .andExpect(jsonPath("$.groups", hasSize(4)))
                .andExpect(jsonPath("$.items", hasSize(6)))
                .andExpect(jsonPath("$.items[0].title").value("匹配报告：Java AI 应用开发实习生"))
                .andExpect(jsonPath("$.items[0].status").value("Draft"))
                .andExpect(jsonPath("$.compliancePrinciples", hasSize(5)));
    }

    @Test
    void humanReviewDetailKeepsDraftCopyLocked() throws Exception {
        mockMvc.perform(get("/api/reviews/review-star-mcp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Draft"))
                .andExpect(jsonPath("$.copyAllowed").value(false))
                .andExpect(jsonPath("$.riskTerms", hasSize(7)))
                .andExpect(jsonPath("$.traceEvidence", hasSize(6)))
                .andExpect(jsonPath("$.evidence.resumeProjects", hasSize(3)))
                .andExpect(jsonPath("$.auditTrail", hasSize(1)))
                .andExpect(jsonPath("$.auditTrail[0].action").value("AUTO_RISK_GUARD"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void humanReviewActionsReturnUpdatedState() throws Exception {
        mockMvc.perform(post("/api/reviews/review-confirmed-resume/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-reviewer\",\"actorRole\":\"Human reviewer\",\"humanNote\":\"人工确认后仅保留作品集级表述。\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Confirmed"))
                .andExpect(jsonPath("$.copyAllowed").value(true))
                .andExpect(jsonPath("$.humanNote").value("人工确认后仅保留作品集级表述。"))
                .andExpect(jsonPath("$.auditTrail[?(@.action == 'CONFIRM')]", hasSize(2)));

        mockMvc.perform(post("/api/reviews/review-returned-devops/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-reviewer\",\"actorRole\":\"Human reviewer\",\"humanNote\":\"需要补充部署证据边界。\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Returned"))
                .andExpect(jsonPath("$.copyAllowed").value(false))
                .andExpect(jsonPath("$.auditTrail[?(@.action == 'RETURN')]", hasSize(2)));

        mockMvc.perform(post("/api/reviews/review-risk-model/flag-risk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-reviewer\",\"actorRole\":\"Human reviewer\",\"humanNote\":\"Provider 能力超出本轮边界。\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("高风险"))
                .andExpect(jsonPath("$.status").value("Risk Flagged"))
                .andExpect(jsonPath("$.copyAllowed").value(false))
                .andExpect(jsonPath("$.auditTrail[?(@.action == 'FLAG_RISK')]", hasSize(1)));
    }

    @Test
    void humanReviewAuditEventsEndpointReturnsHistory() throws Exception {
        mockMvc.perform(get("/api/reviews/review-star-mcp/audit-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reviewId").value("review-star-mcp"))
                .andExpect(jsonPath("$[0].action").value("AUTO_RISK_GUARD"))
                .andExpect(jsonPath("$[0].traceHash").exists());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void confirmWritesAuditEventWithActorAndStateFlow() throws Exception {
        mockMvc.perform(post("/api/reviews/review-opening-boss/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-reviewer\",\"actorRole\":\"Human reviewer\",\"humanNote\":\"确认开场白只引用作品集证据，不承诺回复或 Offer。\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Confirmed"))
                .andExpect(jsonPath("$.auditTrail[?(@.action == 'CONFIRM')]", hasSize(1)))
                .andExpect(jsonPath("$.auditTrail[0].actor").value("demo-reviewer"))
                .andExpect(jsonPath("$.auditTrail[0].previousStatus").value("Draft"))
                .andExpect(jsonPath("$.auditTrail[0].nextStatus").value("Confirmed"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void returnWritesAuditEventWithHumanNote() throws Exception {
        mockMvc.perform(post("/api/reviews/review-returned-devops/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-reviewer\",\"actorRole\":\"Human reviewer\",\"humanNote\":\"继续退回，要求补充部署边界。\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Returned"))
                .andExpect(jsonPath("$.auditTrail[?(@.humanNote == '继续退回，要求补充部署边界。')]", hasSize(1)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void flagRiskWritesAuditEventWithRiskLevelChange() throws Exception {
        mockMvc.perform(post("/api/reviews/review-star-mcp/flag-risk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"demo-reviewer\",\"actorRole\":\"Human reviewer\",\"humanNote\":\"命中实时面试辅助与保证通过，标记风险。\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Risk Flagged"))
                .andExpect(jsonPath("$.riskLevel").value("高风险"))
                .andExpect(jsonPath("$.auditTrail[?(@.action == 'FLAG_RISK')]", hasSize(1)))
                .andExpect(jsonPath("$.auditTrail[1].previousRiskLevel").value("中"))
                .andExpect(jsonPath("$.auditTrail[1].nextRiskLevel").value("高风险"));
    }
}
