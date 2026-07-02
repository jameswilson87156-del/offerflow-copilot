package com.offerflow.copilot.api;

import java.util.List;

import com.offerflow.copilot.copy.CopyPermissionAuditEvent;
import com.offerflow.copilot.copy.CopyPermissionRequest;
import com.offerflow.copilot.copy.CopyPermissionResult;
import com.offerflow.copilot.copy.CopyPermissionService;
import com.offerflow.copilot.copy.CopyTargetType;
import com.offerflow.copilot.security.local.LocalActorContext;
import com.offerflow.copilot.security.local.LocalActorResolver;
import com.offerflow.copilot.security.local.LocalPermissionAuditService;
import com.offerflow.copilot.security.local.PermissionAction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/copy-permissions")
public class CopyPermissionController {

    private final CopyPermissionService copyPermissionService;
    private final LocalActorResolver actorResolver;
    private final LocalPermissionAuditService permissionAuditService;

    public CopyPermissionController(
            CopyPermissionService copyPermissionService,
            LocalActorResolver actorResolver,
            LocalPermissionAuditService permissionAuditService) {
        this.copyPermissionService = copyPermissionService;
        this.actorResolver = actorResolver;
        this.permissionAuditService = permissionAuditService;
    }

    @PostMapping("/check")
    public CopyPermissionResult check(
            @RequestBody(required = false) CopyPermissionRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        CopyPermissionRequest safeRequest = request == null
                ? new CopyPermissionRequest(null, null, null, null, null, null, null, null, null)
                : request;
        LocalActorContext actorContext = actorResolver.resolve(
                safeRequest.actor(), safeRequest.actorRole(), headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(
                actorContext,
                PermissionAction.COPY_CHECK,
                safeRequest.targetType() == null ? "COPY_PERMISSION" : safeRequest.targetType().name(),
                safeRequest.targetId());
        return copyPermissionService.check(withActor(safeRequest, actorContext));
    }

    @GetMapping("/audit-events")
    public List<CopyPermissionAuditEvent> auditEvents(
            @RequestParam(required = false) CopyTargetType targetType,
            @RequestParam(required = false) String targetId) {
        return copyPermissionService.auditEvents(targetType, targetId);
    }

    private CopyPermissionRequest withActor(CopyPermissionRequest request, LocalActorContext actorContext) {
        return new CopyPermissionRequest(
                request.targetType(),
                request.targetId(),
                actorContext.actor(),
                actorContext.actorRole().name(),
                request.requestedText(),
                request.providerRunId(),
                request.traceId(),
                request.schemaVersion(),
                request.promptVersion());
    }
}
