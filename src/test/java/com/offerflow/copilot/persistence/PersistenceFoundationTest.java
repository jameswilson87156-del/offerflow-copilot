package com.offerflow.copilot.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.offerflow.copilot.OfferFlowCopilotApplication;
import com.offerflow.copilot.domain.MatchReportDemo;
import com.offerflow.copilot.persistence.repository.ApplicationRecordRepository;
import com.offerflow.copilot.persistence.repository.HumanReviewAuditEventRepository;
import com.offerflow.copilot.persistence.repository.HumanReviewItemRepository;
import com.offerflow.copilot.persistence.repository.InterviewPrepRepository;
import com.offerflow.copilot.persistence.repository.JobPostRepository;
import com.offerflow.copilot.persistence.repository.MatchReportRepository;
import com.offerflow.copilot.persistence.repository.ProviderTraceRunRepository;
import com.offerflow.copilot.persistence.repository.ResumeEvidenceRepository;
import com.offerflow.copilot.persistence.repository.TraceStepRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = OfferFlowCopilotApplication.class)
@AutoConfigureMockMvc
class PersistenceFoundationTest {

    @Autowired
    private PersistenceSeedService seedService;

    @Autowired
    private JsonCodec jsonCodec;

    @Autowired
    private ResumeEvidenceRepository resumeEvidenceRepository;

    @Autowired
    private JobPostRepository jobPostRepository;

    @Autowired
    private MatchReportRepository matchReportRepository;

    @Autowired
    private InterviewPrepRepository interviewPrepRepository;

    @Autowired
    private ApplicationRecordRepository applicationRecordRepository;

    @Autowired
    private HumanReviewItemRepository humanReviewItemRepository;

    @Autowired
    private HumanReviewAuditEventRepository humanReviewAuditEventRepository;

    @Autowired
    private ProviderTraceRunRepository providerTraceRunRepository;

    @Autowired
    private TraceStepRepository traceStepRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void seedOnEmptyCreatesDemoPersistenceRows() {
        assertThat(jobPostRepository.count()).isEqualTo(1);
        assertThat(resumeEvidenceRepository.count()).isEqualTo(4);
        assertThat(matchReportRepository.count()).isEqualTo(1);
        assertThat(interviewPrepRepository.count()).isEqualTo(1);
        assertThat(applicationRecordRepository.count()).isEqualTo(3);
        assertThat(humanReviewItemRepository.count()).isEqualTo(6);
        assertThat(humanReviewAuditEventRepository.count()).isGreaterThanOrEqualTo(4);
        assertThat(providerTraceRunRepository.count()).isEqualTo(1);
        assertThat(traceStepRepository.count()).isEqualTo(10);
    }

    @Test
    void seedOnEmptyDoesNotDuplicateRows() {
        long evidenceCount = resumeEvidenceRepository.count();
        long reviewCount = humanReviewItemRepository.count();
        long auditCount = humanReviewAuditEventRepository.count();
        long stepCount = traceStepRepository.count();

        seedService.seedIfEmpty();

        assertThat(resumeEvidenceRepository.count()).isEqualTo(evidenceCount);
        assertThat(humanReviewItemRepository.count()).isEqualTo(reviewCount);
        assertThat(humanReviewAuditEventRepository.count()).isEqualTo(auditCount);
        assertThat(traceStepRepository.count()).isEqualTo(stepCount);
    }

    @Test
    void jsonCodecRoundTripsStructuredFields() {
        List<MatchReportDemo.SkillGap> gaps = List.of(
                new MatchReportDemo.SkillGap("Redis 深度使用", "中", "缺少复杂一致性案例", "补充缓存策略说明"));

        String json = jsonCodec.write(gaps);
        List<MatchReportDemo.SkillGap> restored = jsonCodec.readList(json, MatchReportDemo.SkillGap.class);

        assertThat(restored).hasSize(1);
        assertThat(restored.get(0).skill()).isEqualTo("Redis 深度使用");
        assertThat(restored.get(0).nextAction()).contains("缓存策略");
    }

    @Test
    void coreInterfacesReturnSeededDatabaseData() throws Exception {
        assertThat(matchReportRepository.findDemo()).isPresent();
        assertThat(providerTraceRunRepository.findByRunId(PersistenceSeedService.DEMO_RUN_ID)).isPresent();

        mockMvc.perform(get("/api/evidence/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(4)))
                .andExpect(jsonPath("$.items[0].projectName").value("MCP Tool Gateway"));

        mockMvc.perform(get("/api/match-report/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalScore").value(82));

        mockMvc.perform(get("/api/provider/traces/" + PersistenceSeedService.DEMO_RUN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipeline", hasSize(10)))
                .andExpect(jsonPath("$.pipeline[3].status").value("fallback"));
    }
}
