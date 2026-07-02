package com.offerflow.copilot.api;

import java.util.List;

import com.offerflow.copilot.security.local.LocalActorContext;
import com.offerflow.copilot.security.local.LocalActorResolver;
import com.offerflow.copilot.security.local.LocalActorRole;
import com.offerflow.copilot.security.local.LocalPermissionAuditService;
import com.offerflow.copilot.security.local.LocalPermissionPolicy;
import com.offerflow.copilot.security.local.PermissionAction;
import com.offerflow.copilot.security.local.PermissionAuditEvent;
import com.offerflow.copilot.security.local.PermissionDecision;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final LocalActorResolver actorResolver;
    private final LocalPermissionPolicy permissionPolicy;
    private final LocalPermissionAuditService permissionAuditService;

    public PermissionController(
            LocalActorResolver actorResolver,
            LocalPermissionPolicy permissionPolicy,
            LocalPermissionAuditService permissionAuditService) {
        this.actorResolver = actorResolver;
        this.permissionPolicy = permissionPolicy;
        this.permissionAuditService = permissionAuditService;
    }

    @GetMapping("/current-actor")
    public CurrentActorResponse currentActor(
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        LocalActorContext actorContext = actorResolver.resolve(null, null, headerActor, headerRole, requestId);
        return new CurrentActorResponse(
                actorContext.actor(),
                actorContext.actorRole(),
                permissionPolicy.allowedActions(actorContext.actorRole()),
                LocalPermissionPolicy.BOUNDARY_NOTICE);
    }

    @PostMapping("/check")
    public PermissionDecision check(
            @RequestBody(required = false) PermissionCheckRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        if (request == null || request.action() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Permission action is required");
        }
        LocalActorContext actorContext = actorResolver.resolve(
                request.actor(), request.actorRole(), headerActor, headerRole, requestId);
        return permissionAuditService.auditDecision(actorContext, request.action(), request.targetType(), request.targetId());
    }

    @GetMapping("/audit-events")
    public List<PermissionAuditEvent> auditEvents(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) PermissionAction action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) Boolean allowed) {
        return permissionAuditService.auditEvents(actor, action, targetType, targetId, allowed);
    }

    public record CurrentActorResponse(
            String actor,
            LocalActorRole actorRole,
            List<PermissionAction> permissions,
            String boundaryNotice) {
    }

    public record PermissionCheckRequest(
            String actor,
            String actorRole,
            PermissionAction action,
            String targetType,
            String targetId) {
    }
}
