package com.offerflow.copilot.service;

import com.offerflow.copilot.domain.MatchReportDemo;
import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.persistence.entity.MatchReportEntity;
import com.offerflow.copilot.persistence.repository.MatchReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class MatchReportService {

    private final MatchReportRepository matchReportRepository;
    private final JsonCodec jsonCodec;

    public MatchReportService(MatchReportRepository matchReportRepository, JsonCodec jsonCodec) {
        this.matchReportRepository = matchReportRepository;
        this.jsonCodec = jsonCodec;
    }

    public MatchReportDemo getDemoReport() {
        MatchReportEntity report = matchReportRepository.findDemo()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Match report demo data not found"));
        return new MatchReportDemo(
                report.getMode(),
                jsonCodec.read(report.getSummaryJson(), MatchReportDemo.ReportSummary.class),
                jsonCodec.read(report.getScoreJson(), MatchReportDemo.ScoreBreakdown.class),
                jsonCodec.readList(report.getEvidenceSourcesJson(), MatchReportDemo.EvidenceSource.class),
                jsonCodec.readList(report.getSkillGapsJson(), MatchReportDemo.SkillGap.class),
                jsonCodec.readList(report.getRecommendedActionsJson(), MatchReportDemo.RecommendedAction.class),
                jsonCodec.readList(report.getTraceEvidenceJson(), MatchReportDemo.TraceStep.class),
                report.getDisclaimer());
    }
}
