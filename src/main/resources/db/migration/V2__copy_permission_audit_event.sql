-- Unified copy gate audit events for P4D Copy Permission Contract.
-- This table intentionally does not store requested text or PII.

CREATE TABLE copy_permission_audit_event (
    id VARCHAR(80) PRIMARY KEY,
    target_type VARCHAR(80) NOT NULL,
    target_id VARCHAR(120) NOT NULL,
    action VARCHAR(40) NOT NULL,
    allowed BOOLEAN NOT NULL,
    reason TEXT NOT NULL,
    target_status VARCHAR(40) NOT NULL,
    human_review_status VARCHAR(40) NOT NULL,
    schema_validated BOOLEAN NOT NULL,
    risk_guard_passed BOOLEAN NOT NULL,
    actor VARCHAR(120) NOT NULL,
    actor_role VARCHAR(80) NOT NULL,
    trace_id VARCHAR(100) NOT NULL,
    provider_run_id VARCHAR(100) NOT NULL,
    boundary_notice TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
