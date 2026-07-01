package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.DemoAnalysis;
import com.offerflow.copilot.domain.JobIntake;
import com.offerflow.copilot.service.DemoAnalysisService;
import com.offerflow.copilot.service.JobIntakeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobAnalysisController {

    private final DemoAnalysisService demoAnalysisService;
    private final JobIntakeService jobIntakeService;

    public JobAnalysisController(DemoAnalysisService demoAnalysisService, JobIntakeService jobIntakeService) {
        this.demoAnalysisService = demoAnalysisService;
        this.jobIntakeService = jobIntakeService;
    }

    @GetMapping
    public JobIntake jobs() {
        return jobIntakeService.listJobs();
    }

    @GetMapping("/{id}")
    public JobIntake.JobDetail jobDetail(@PathVariable String id) {
        return jobIntakeService.detail(id);
    }

    @PostMapping
    public JobIntake.JobDetail createJob(@RequestBody JobIntake.JobMutationRequest request) {
        return jobIntakeService.createJob(request);
    }

    @PutMapping("/{id}")
    public JobIntake.JobDetail updateJob(
            @PathVariable String id,
            @RequestBody JobIntake.JobMutationRequest request) {
        return jobIntakeService.updateJob(id, request);
    }

    @PostMapping("/{id}/parse")
    public JobIntake.JobDetail parseJob(
            @PathVariable String id,
            @RequestBody(required = false) JobIntake.JobActionRequest request) {
        return jobIntakeService.parseJob(id, request == null ? new JobIntake.JobActionRequest(null, null, null) : request);
    }

    @PostMapping("/{id}/bind-evidence")
    public JobIntake.JobDetail bindEvidence(
            @PathVariable String id,
            @RequestBody(required = false) JobIntake.JobActionRequest request) {
        return jobIntakeService.bindEvidence(id, request == null ? new JobIntake.JobActionRequest(null, null, null) : request);
    }

    @GetMapping("/{id}/parse-versions")
    public java.util.List<JobIntake.JdParseVersion> parseVersions(@PathVariable String id) {
        return jobIntakeService.parseVersions(id);
    }

    @GetMapping("/{id}/audit-events")
    public java.util.List<JobIntake.JdAuditEvent> auditEvents(@PathVariable String id) {
        return jobIntakeService.auditEvents(id);
    }

    @GetMapping("/{id}/evidence-bindings")
    public java.util.List<JobIntake.JdEvidenceBinding> evidenceBindings(@PathVariable String id) {
        return jobIntakeService.evidenceBindings(id);
    }

    @GetMapping("/demo-analysis")
    public DemoAnalysis demoAnalysis() {
        return demoAnalysisService.getDemoAnalysis();
    }
}
