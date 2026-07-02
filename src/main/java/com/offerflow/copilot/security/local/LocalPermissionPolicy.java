package com.offerflow.copilot.security.local;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class LocalPermissionPolicy {

    public static final String BOUNDARY_NOTICE =
            "Demo local permission mode only; this is not production authentication or authorization.";

    private static final Set<PermissionAction> REVIEWER_ALLOWED = EnumSet.of(
            PermissionAction.REVIEW_CONFIRM,
            PermissionAction.REVIEW_RETURN,
            PermissionAction.REVIEW_FLAG_RISK,
            PermissionAction.COPY_CHECK);

    private static final Set<PermissionAction> EDITOR_ALLOWED = EnumSet.of(
            PermissionAction.EVIDENCE_CREATE,
            PermissionAction.EVIDENCE_UPDATE,
            PermissionAction.JD_CREATE,
            PermissionAction.JD_UPDATE,
            PermissionAction.JD_PARSE,
            PermissionAction.JD_BIND_EVIDENCE,
            PermissionAction.MATCH_REPORT_GENERATE,
            PermissionAction.MATCH_REPORT_SEND_TO_REVIEW,
            PermissionAction.PROVIDER_REAL_DRY_RUN);

    private static final Set<PermissionAction> SYSTEM_ALLOWED = EnumSet.of(
            PermissionAction.JD_PARSE,
            PermissionAction.JD_BIND_EVIDENCE,
            PermissionAction.MATCH_REPORT_GENERATE,
            PermissionAction.PROVIDER_SANDBOX_RUN);

    public PermissionDecision decide(
            LocalActorContext actorContext,
            PermissionAction action,
            String targetType,
            String targetId) {
        boolean allowed = isAllowed(actorContext.actorRole(), action);
        return new PermissionDecision(
                allowed,
                reason(actorContext.actorRole(), action, allowed),
                actorContext.actor(),
                actorContext.actorRole(),
                action,
                valueOr(targetType, "UNKNOWN"),
                valueOr(targetId, ""),
                BOUNDARY_NOTICE);
    }

    public List<PermissionAction> allowedActions(LocalActorRole role) {
        if (role == LocalActorRole.OWNER) {
            return List.of(PermissionAction.values());
        }
        if (role == LocalActorRole.REVIEWER) {
            return REVIEWER_ALLOWED.stream().toList();
        }
        if (role == LocalActorRole.EDITOR) {
            return EDITOR_ALLOWED.stream().toList();
        }
        if (role == LocalActorRole.SYSTEM) {
            return SYSTEM_ALLOWED.stream().toList();
        }
        return List.of();
    }

    private boolean isAllowed(LocalActorRole role, PermissionAction action) {
        if (role == LocalActorRole.OWNER) {
            return true;
        }
        if (role == LocalActorRole.REVIEWER) {
            return REVIEWER_ALLOWED.contains(action);
        }
        if (role == LocalActorRole.EDITOR) {
            return EDITOR_ALLOWED.contains(action);
        }
        if (role == LocalActorRole.SYSTEM) {
            return SYSTEM_ALLOWED.contains(action);
        }
        return false;
    }

    private String reason(LocalActorRole role, PermissionAction action, boolean allowed) {
        if (allowed) {
            return role.name() + " may perform " + action.name() + " in demo local permission mode.";
        }
        if (role == LocalActorRole.SYSTEM
                && (action == PermissionAction.REVIEW_CONFIRM
                || action == PermissionAction.REVIEW_RETURN
                || action == PermissionAction.REVIEW_FLAG_RISK)) {
            return "SYSTEM cannot act as a human reviewer for final review actions.";
        }
        if (role == LocalActorRole.VIEWER) {
            return "VIEWER is read-only and cannot perform write operations.";
        }
        return role.name() + " is not allowed to perform " + action.name() + " in demo local permission mode.";
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
