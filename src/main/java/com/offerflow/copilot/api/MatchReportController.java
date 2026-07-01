package com.offerflow.copilot.api;

import java.util.List;

import com.offerflow.copilot.domain.MatchReportVersioning;
import com.offerflow.copilot.service.MatchReportVersionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MatchReportController {

    private final MatchReportVersionService matchReportVersionService;

    public MatchReportController(MatchReportVersionService matchReportVersionService) {
        this.matchReportVersionService = matchReportVersionService;
    }

    @GetMapping("/match-report/demo")
    public MatchReportVersioning.ReportDetail demo() {
        return matchReportVersionService.getDemoReport();
    }

    @PostMapping("/jobs/{id}/match-reports/generate")
    public MatchReportVersioning.ReportDetail generate(
            @PathVariable String id,
            @RequestBody(required = false) MatchReportVersioning.ReportActionRequest request) {
        return matchReportVersionService.generate(id, request);
    }

    @GetMapping("/jobs/{id}/match-reports")
    public List<MatchReportVersioning.VersionSummary> versions(@PathVariable String id) {
        return matchReportVersionService.listVersions(id);
    }

    @GetMapping("/match-reports/{versionId}")
    public MatchReportVersioning.ReportDetail detail(@PathVariable String versionId) {
        return matchReportVersionService.getVersion(versionId);
    }

    @GetMapping("/match-reports/{versionId}/audit-events")
    public List<MatchReportVersioning.AuditEvent> auditEvents(@PathVariable String versionId) {
        return matchReportVersionService.auditEvents(versionId);
    }

    @PostMapping("/match-reports/{versionId}/send-to-review")
    public MatchReportVersioning.ReportDetail sendToReview(
            @PathVariable String versionId,
            @RequestBody(required = false) MatchReportVersioning.ReportActionRequest request) {
        return matchReportVersionService.sendToReview(versionId, request);
    }

    @PostMapping("/match-reports/{versionId}/archive")
    public MatchReportVersioning.ReportDetail archive(
            @PathVariable String versionId,
            @RequestBody(required = false) MatchReportVersioning.ReportActionRequest request) {
        return matchReportVersionService.archive(versionId, request);
    }
}
