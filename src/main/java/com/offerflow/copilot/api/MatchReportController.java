package com.offerflow.copilot.api;

import java.util.List;

import com.offerflow.copilot.domain.MatchReportVersioning;
import com.offerflow.copilot.security.local.LocalActorContext;
import com.offerflow.copilot.security.local.LocalActorResolver;
import com.offerflow.copilot.security.local.LocalPermissionAuditService;
import com.offerflow.copilot.security.local.PermissionAction;
import com.offerflow.copilot.service.MatchReportVersionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MatchReportController {

    private final MatchReportVersionService matchReportVersionService;
    private final LocalActorResolver actorResolver;
    private final LocalPermissionAuditService permissionAuditService;

    public MatchReportController(
            MatchReportVersionService matchReportVersionService,
            LocalActorResolver actorResolver,
            LocalPermissionAuditService permissionAuditService) {
        this.matchReportVersionService = matchReportVersionService;
        this.actorResolver = actorResolver;
        this.permissionAuditService = permissionAuditService;
    }

    @GetMapping("/match-report/demo")
    public MatchReportVersioning.ReportDetail demo() {
        return matchReportVersionService.getDemoReport();
    }

    @PostMapping("/jobs/{id}/match-reports/generate")
    public MatchReportVersioning.ReportDetail generate(
            @PathVariable String id,
            @RequestBody(required = false) MatchReportVersioning.ReportActionRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        MatchReportVersioning.ReportActionRequest safeRequest = orEmpty(request);
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.MATCH_REPORT_GENERATE, "JOB", id);
        return matchReportVersionService.generate(id, withActor(safeRequest, actorContext));
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

    @PostMapping("/match-reports/{versionId}/restore")
    public MatchReportVersioning.ReportDetail restore(
            @PathVariable String versionId,
            @RequestBody(required = false) MatchReportVersioning.ReportActionRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        MatchReportVersioning.ReportActionRequest safeRequest = orEmpty(request);
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(
                actorContext, PermissionAction.MATCH_REPORT_RESTORE, "MATCH_REPORT", versionId);
        return matchReportVersionService.restore(versionId, withActor(safeRequest, actorContext));
    }

    @PostMapping("/match-reports/{versionId}/copy-check")
    public MatchReportVersioning.CopyCheck copyCheck(
            @PathVariable String versionId,
            @RequestBody(required = false) MatchReportVersioning.ReportActionRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        MatchReportVersioning.ReportActionRequest safeRequest = orEmpty(request);
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.COPY_CHECK, "MATCH_REPORT", versionId);
        return matchReportVersionService.copyCheck(versionId, withActor(safeRequest, actorContext));
    }

    @PostMapping("/match-reports/{versionId}/send-to-review")
    public MatchReportVersioning.ReportDetail sendToReview(
            @PathVariable String versionId,
            @RequestBody(required = false) MatchReportVersioning.ReportActionRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        MatchReportVersioning.ReportActionRequest safeRequest = orEmpty(request);
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(
                actorContext, PermissionAction.MATCH_REPORT_SEND_TO_REVIEW, "MATCH_REPORT", versionId);
        return matchReportVersionService.sendToReview(versionId, withActor(safeRequest, actorContext));
    }

    @PostMapping("/match-reports/{versionId}/archive")
    public MatchReportVersioning.ReportDetail archive(
            @PathVariable String versionId,
            @RequestBody(required = false) MatchReportVersioning.ReportActionRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        MatchReportVersioning.ReportActionRequest safeRequest = orEmpty(request);
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(
                actorContext, PermissionAction.MATCH_REPORT_ARCHIVE, "MATCH_REPORT", versionId);
        return matchReportVersionService.archive(versionId, withActor(safeRequest, actorContext));
    }

    private MatchReportVersioning.ReportActionRequest orEmpty(MatchReportVersioning.ReportActionRequest request) {
        return request == null ? new MatchReportVersioning.ReportActionRequest(null, null, null, null) : request;
    }

    private LocalActorContext resolve(
            MatchReportVersioning.ReportActionRequest request,
            String headerActor,
            String headerRole,
            String requestId) {
        return actorResolver.resolve(request.actor(), request.actorRole(), headerActor, headerRole, requestId);
    }

    private MatchReportVersioning.ReportActionRequest withActor(
            MatchReportVersioning.ReportActionRequest request,
            LocalActorContext actorContext) {
        return new MatchReportVersioning.ReportActionRequest(
                actorContext.actor(),
                actorContext.actorRole().name(),
                request.humanNote(),
                request.note());
    }
}
