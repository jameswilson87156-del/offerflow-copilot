package com.offerflow.copilot.api;

import java.util.List;

import com.offerflow.copilot.domain.EvidenceCoverage;
import com.offerflow.copilot.domain.EvidenceLibrary;
import com.offerflow.copilot.security.local.LocalActorContext;
import com.offerflow.copilot.security.local.LocalActorResolver;
import com.offerflow.copilot.security.local.LocalPermissionAuditService;
import com.offerflow.copilot.security.local.PermissionAction;
import com.offerflow.copilot.service.EvidenceLibraryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceLibraryService evidenceLibraryService;
    private final LocalActorResolver actorResolver;
    private final LocalPermissionAuditService permissionAuditService;

    public EvidenceController(
            EvidenceLibraryService evidenceLibraryService,
            LocalActorResolver actorResolver,
            LocalPermissionAuditService permissionAuditService) {
        this.evidenceLibraryService = evidenceLibraryService;
        this.actorResolver = actorResolver;
        this.permissionAuditService = permissionAuditService;
    }

    @GetMapping("/library")
    public EvidenceLibrary library() {
        return evidenceLibraryService.getLibrary();
    }

    @GetMapping("/{id}")
    public EvidenceLibrary.EvidenceItemDetail detail(@PathVariable String id) {
        return evidenceLibraryService.getDetail(id);
    }

    @GetMapping("/{id}/audit-events")
    public List<EvidenceLibrary.EvidenceAuditEvent> auditEvents(@PathVariable String id) {
        return evidenceLibraryService.auditEvents(id);
    }

    @PostMapping
    public EvidenceLibrary.EvidenceItemDetail createDraft(
            @RequestBody(required = false) EvidenceLibrary.EvidenceMutationRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        EvidenceLibrary.EvidenceMutationRequest safeRequest = orEmpty(request);
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.EVIDENCE_CREATE, "EVIDENCE", "new");
        return evidenceLibraryService.createDraft(withActor(safeRequest, actorContext));
    }

    @PutMapping("/{id}")
    public EvidenceLibrary.EvidenceItemDetail updateDraft(
            @PathVariable String id,
            @RequestBody(required = false) EvidenceLibrary.EvidenceMutationRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        EvidenceLibrary.EvidenceMutationRequest safeRequest = orEmpty(request);
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.EVIDENCE_UPDATE, "EVIDENCE", id);
        return evidenceLibraryService.updateDraft(id, withActor(safeRequest, actorContext));
    }

    @PostMapping("/{id}/confirm")
    public EvidenceLibrary.EvidenceItemDetail confirm(
            @PathVariable String id,
            @RequestBody(required = false) EvidenceLibrary.EvidenceMutationRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        EvidenceLibrary.EvidenceMutationRequest safeRequest = orEmpty(request);
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.EVIDENCE_CONFIRM, "EVIDENCE", id);
        return evidenceLibraryService.confirm(id, withActor(safeRequest, actorContext));
    }

    @PostMapping("/{id}/return-to-draft")
    public EvidenceLibrary.EvidenceItemDetail returnToDraft(
            @PathVariable String id,
            @RequestBody(required = false) EvidenceLibrary.EvidenceMutationRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        EvidenceLibrary.EvidenceMutationRequest safeRequest = orEmpty(request);
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.EVIDENCE_UPDATE, "EVIDENCE", id);
        return evidenceLibraryService.returnToDraft(id, withActor(safeRequest, actorContext));
    }

    @PostMapping("/{id}/archive")
    public EvidenceLibrary.EvidenceItemDetail archive(
            @PathVariable String id,
            @RequestBody(required = false) EvidenceLibrary.EvidenceMutationRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        EvidenceLibrary.EvidenceMutationRequest safeRequest = orEmpty(request);
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.EVIDENCE_ARCHIVE, "EVIDENCE", id);
        return evidenceLibraryService.archive(id, withActor(safeRequest, actorContext));
    }

    @PostMapping("/{id}/restore")
    public EvidenceLibrary.EvidenceItemDetail restore(
            @PathVariable String id,
            @RequestBody(required = false) EvidenceLibrary.EvidenceMutationRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        EvidenceLibrary.EvidenceMutationRequest safeRequest = orEmpty(request);
        LocalActorContext actorContext = resolve(safeRequest, headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(actorContext, PermissionAction.EVIDENCE_RESTORE, "EVIDENCE", id);
        return evidenceLibraryService.restore(id, withActor(safeRequest, actorContext));
    }

    @GetMapping("/coverage")
    public EvidenceCoverage coverage() {
        return evidenceLibraryService.getCoverage();
    }

    private EvidenceLibrary.EvidenceMutationRequest orEmpty(EvidenceLibrary.EvidenceMutationRequest request) {
        if (request != null) {
            return request;
        }
        return new EvidenceLibrary.EvidenceMutationRequest(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private LocalActorContext resolve(
            EvidenceLibrary.EvidenceMutationRequest request,
            String headerActor,
            String headerRole,
            String requestId) {
        return actorResolver.resolve(request.actor(), request.actorRole(), headerActor, headerRole, requestId);
    }

    private EvidenceLibrary.EvidenceMutationRequest withActor(
            EvidenceLibrary.EvidenceMutationRequest request,
            LocalActorContext actorContext) {
        return new EvidenceLibrary.EvidenceMutationRequest(
                actorContext.actor(),
                actorContext.actorRole().name(),
                request.humanNote(),
                request.projectName(),
                request.summary(),
                request.abilityTags(),
                request.evidenceSources(),
                request.credibility(),
                request.matchableRequirements(),
                request.boundaryNote(),
                request.relatedSkills(),
                request.suitableRoles(),
                request.interviewAnswers(),
                request.riskBoundaries(),
                request.targetStatus());
    }
}
