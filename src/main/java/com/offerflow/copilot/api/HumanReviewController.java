package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.HumanReviewCenter;
import com.offerflow.copilot.security.local.LocalActorContext;
import com.offerflow.copilot.security.local.LocalActorResolver;
import com.offerflow.copilot.security.local.LocalPermissionAuditService;
import com.offerflow.copilot.security.local.PermissionAction;
import com.offerflow.copilot.service.HumanReviewService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class HumanReviewController {

    private final HumanReviewService humanReviewService;
    private final LocalActorResolver actorResolver;
    private final LocalPermissionAuditService permissionAuditService;

    public HumanReviewController(
            HumanReviewService humanReviewService,
            LocalActorResolver actorResolver,
            LocalPermissionAuditService permissionAuditService) {
        this.humanReviewService = humanReviewService;
        this.actorResolver = actorResolver;
        this.permissionAuditService = permissionAuditService;
    }

    @GetMapping
    public HumanReviewCenter reviews() {
        return humanReviewService.listReviews();
    }

    @GetMapping("/{id}")
    public HumanReviewCenter.ReviewDetail review(@PathVariable String id) {
        return humanReviewService.getReview(id);
    }

    @GetMapping("/{id}/audit-events")
    public List<HumanReviewCenter.AuditEvent> auditEvents(@PathVariable String id) {
        return humanReviewService.auditEvents(id);
    }

    @PostMapping("/{id}/confirm")
    public HumanReviewCenter.ReviewDetail confirm(
            @PathVariable String id,
            @RequestBody(required = false) ReviewActionRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        LocalActorContext actorContext = resolve(request, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.REVIEW_CONFIRM, "HUMAN_REVIEW", id);
        return humanReviewService.confirm(id, actorContext.actor(), actorContext.actorRole().name(), humanNote(request));
    }

    @PostMapping("/{id}/return")
    public HumanReviewCenter.ReviewDetail returnForRevision(
            @PathVariable String id,
            @RequestBody(required = false) ReviewActionRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        LocalActorContext actorContext = resolve(request, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.REVIEW_RETURN, "HUMAN_REVIEW", id);
        return humanReviewService.returnForRevision(
                id, actorContext.actor(), actorContext.actorRole().name(), humanNote(request));
    }

    @PostMapping("/{id}/flag-risk")
    public HumanReviewCenter.ReviewDetail flagRisk(
            @PathVariable String id,
            @RequestBody(required = false) ReviewActionRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        LocalActorContext actorContext = resolve(request, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.REVIEW_FLAG_RISK, "HUMAN_REVIEW", id);
        return humanReviewService.flagRisk(id, actorContext.actor(), actorContext.actorRole().name(), humanNote(request));
    }

    private LocalActorContext resolve(
            ReviewActionRequest request,
            String headerActor,
            String headerRole,
            String requestId) {
        return actorResolver.resolve(
                request == null ? null : request.actor(),
                request == null ? null : request.actorRole(),
                headerActor,
                headerRole,
                requestId);
    }

    private String humanNote(ReviewActionRequest request) {
        if (request == null) {
            return "";
        }
        return request.humanNote() == null || request.humanNote().isBlank() ? request.note() : request.humanNote();
    }

    public record ReviewActionRequest(String actor, String actorRole, String humanNote, String note) {
    }
}
