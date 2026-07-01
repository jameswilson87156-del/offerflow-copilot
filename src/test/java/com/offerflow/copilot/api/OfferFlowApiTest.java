package com.offerflow.copilot.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.offerflow.copilot.OfferFlowCopilotApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
    void evidenceLibraryReturnsReviewableProjectEvidence() throws Exception {
        mockMvc.perform(get("/api/evidence/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock/local-rule"))
                .andExpect(jsonPath("$.items", hasSize(4)))
                .andExpect(jsonPath("$.categories", hasSize(9)))
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
                .andExpect(jsonPath("$.evidence.resumeProjects", hasSize(3)));
    }

    @Test
    void humanReviewActionsReturnUpdatedState() throws Exception {
        mockMvc.perform(post("/api/reviews/review-confirmed-resume/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"人工确认后仅保留作品集级表述。\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Confirmed"))
                .andExpect(jsonPath("$.copyAllowed").value(true))
                .andExpect(jsonPath("$.humanNote").value("人工确认后仅保留作品集级表述。"));

        mockMvc.perform(post("/api/reviews/review-returned-devops/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"需要补充部署证据边界。\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Returned"))
                .andExpect(jsonPath("$.copyAllowed").value(false));

        mockMvc.perform(post("/api/reviews/review-risk-model/flag-risk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Provider 能力超出本轮边界。\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("高风险"))
                .andExpect(jsonPath("$.status").value("Draft"))
                .andExpect(jsonPath("$.copyAllowed").value(false));
    }
}
