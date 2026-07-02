package com.offerflow.copilot.security.local;

public class LocalPermissionDeniedException extends RuntimeException {

    private final PermissionDecision decision;

    public LocalPermissionDeniedException(PermissionDecision decision) {
        super(decision.reason());
        this.decision = decision;
    }

    public PermissionDecision decision() {
        return decision;
    }
}
