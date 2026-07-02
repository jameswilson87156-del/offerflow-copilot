package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.DemoAnalysis;
import com.offerflow.copilot.domain.JobIntake;
import com.offerflow.copilot.security.local.LocalActorContext;
import com.offerflow.copilot.security.local.LocalActorResolver;
import com.offerflow.copilot.security.local.LocalPermissionAuditService;
import com.offerflow.copilot.security.local.PermissionAction;
import com.offerflow.copilot.service.DemoAnalysisService;
import com.offerflow.copilot.service.JobIntakeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobAnalysisController {

    private final DemoAnalysisService demoAnalysisService;
    private final JobIntakeService jobIntakeService;
    private final LocalActorResolver actorResolver;
    private final LocalPermissionAuditService permissionAuditService;

    public JobAnalysisController(
            DemoAnalysisService demoAnalysisService,
            JobIntakeService jobIntakeService,
            LocalActorResolver actorResolver,
            LocalPermissionAuditService permissionAuditService) {
        this.demoAnalysisService = demoAnalysisService;
        this.jobIntakeService = jobIntakeService;
        this.actorResolver = actorResolver;
        this.permissionAuditService = permissionAuditService;
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
    public JobIntake.JobDetail createJob(
            @RequestBody JobIntake.JobMutationRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        LocalActorContext actorContext = resolve(request, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.JD_CREATE, "JOB", "new");
        return jobIntakeService.createJob(withActor(request, actorContext));
    }

    @PutMapping("/{id}")
    public JobIntake.JobDetail updateJob(
            @PathVariable String id,
            @RequestBody JobIntake.JobMutationRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        LocalActorContext actorContext = resolve(request, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.JD_UPDATE, "JOB", id);
        return jobIntakeService.updateJob(id, withActor(request, actorContext));
    }

    @PostMapping("/{id}/parse")
    public JobIntake.JobDetail parseJob(
            @PathVariable String id,
            @RequestBody(required = false) JobIntake.JobActionRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        JobIntake.JobActionRequest safeRequest = request == null
                ? new JobIntake.JobActionRequest(null, null, null)
                : request;
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.JD_PARSE, "JOB", id);
        return jobIntakeService.parseJob(id, withActor(safeRequest, actorContext));
    }

    @PostMapping("/{id}/bind-evidence")
    public JobIntake.JobDetail bindEvidence(
            @PathVariable String id,
            @RequestBody(required = false) JobIntake.JobActionRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        JobIntake.JobActionRequest safeRequest = request == null
                ? new JobIntake.JobActionRequest(null, null, null)
                : request;
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.JD_BIND_EVIDENCE, "JOB", id);
        return jobIntakeService.bindEvidence(id, withActor(safeRequest, actorContext));
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

    private LocalActorContext resolve(
            JobIntake.JobMutationRequest request,
            String headerActor,
            String headerRole,
            String requestId) {
        return actorResolver.resolve(request.actor(), request.actorRole(), headerActor, headerRole, requestId);
    }

    private LocalActorContext resolve(
            JobIntake.JobActionRequest request,
            String headerActor,
            String headerRole,
            String requestId) {
        return actorResolver.resolve(request.actor(), request.actorRole(), headerActor, headerRole, requestId);
    }

    private JobIntake.JobMutationRequest withActor(
            JobIntake.JobMutationRequest request,
            LocalActorContext actorContext) {
        return new JobIntake.JobMutationRequest(
                request.title(),
                request.company(),
                request.city(),
                request.jdText(),
                request.sourceType(),
                request.sourceNote(),
                actorContext.actor(),
                actorContext.actorRole().name(),
                request.humanNote());
    }

    private JobIntake.JobActionRequest withActor(
            JobIntake.JobActionRequest request,
            LocalActorContext actorContext) {
        return new JobIntake.JobActionRequest(
                actorContext.actor(),
                actorContext.actorRole().name(),
                request.humanNote());
    }
}
