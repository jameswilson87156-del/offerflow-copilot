package com.offerflow.copilot.security.local;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import com.offerflow.copilot.persistence.entity.PermissionAuditEventEntity;
import com.offerflow.copilot.persistence.repository.PermissionAuditEventRepository;
import org.springframework.stereotype.Service;

@Service
public class LocalPermissionAuditService {

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LocalPermissionPolicy permissionPolicy;
    private final PermissionAuditEventRepository auditEventRepository;

    public LocalPermissionAuditService(
            LocalPermissionPolicy permissionPolicy,
            PermissionAuditEventRepository auditEventRepository) {
        this.permissionPolicy = permissionPolicy;
        this.auditEventRepository = auditEventRepository;
    }

    public PermissionDecision requireAllowed(
            LocalActorContext actorContext,
            PermissionAction action,
            String targetType,
            String targetId) {
        PermissionDecision decision = auditDecision(actorContext, action, targetType, targetId);
        if (!decision.allowed()) {
            throw new LocalPermissionDeniedException(decision);
        }
        return decision;
    }

    public PermissionDecision auditDecision(
            LocalActorContext actorContext,
            PermissionAction action,
            String targetType,
            String targetId) {
        PermissionDecision decision = permissionPolicy.decide(actorContext, action, targetType, targetId);
        auditEventRepository.save(toEntity(actorContext, decision));
        return decision;
    }

    public List<PermissionAuditEvent> auditEvents(
            String actor,
            PermissionAction action,
            String targetType,
            String targetId,
            Boolean allowed) {
        return auditEventRepository.find(actor, action, targetType, targetId, allowed).stream()
                .map(this::toDto)
                .toList();
    }

    private PermissionAuditEventEntity toEntity(LocalActorContext actorContext, PermissionDecision decision) {
        PermissionAuditEventEntity entity = new PermissionAuditEventEntity();
        entity.setId("permission-audit-" + UUID.randomUUID());
        entity.setActor(decision.actor());
        entity.setActorRole(decision.actorRole().name());
        entity.setAction(decision.action().name());
        entity.setTargetType(decision.targetType());
        entity.setTargetId(decision.targetId());
        entity.setAllowed(decision.allowed());
        entity.setReason(decision.reason());
        entity.setBoundaryNotice(decision.boundaryNotice());
        entity.setRequestId(actorContext.requestId());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    private PermissionAuditEvent toDto(PermissionAuditEventEntity entity) {
        return new PermissionAuditEvent(
                entity.getId(),
                entity.getActor(),
                LocalActorRole.from(entity.getActorRole()).orElse(LocalActorRole.VIEWER),
                PermissionAction.valueOf(entity.getAction()),
                entity.getTargetType(),
                entity.getTargetId(),
                Boolean.TRUE.equals(entity.getAllowed()),
                entity.getReason(),
                entity.getBoundaryNotice(),
                entity.getRequestId(),
                format(entity.getCreatedAt()));
    }

    private String format(LocalDateTime timestamp) {
        return timestamp == null ? "" : DISPLAY_TIME.format(timestamp);
    }
}
