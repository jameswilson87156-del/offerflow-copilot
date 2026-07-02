-- Local demo permission decisions for P4E.
-- This is not a production security audit table and stores no real PII.

CREATE TABLE permission_audit_event (
    id VARCHAR(80) PRIMARY KEY,
    actor VARCHAR(120) NOT NULL,
    actor_role VARCHAR(80) NOT NULL,
    action VARCHAR(80) NOT NULL,
    target_type VARCHAR(80) NOT NULL,
    target_id VARCHAR(120) NOT NULL,
    allowed BOOLEAN NOT NULL,
    reason TEXT NOT NULL,
    boundary_notice TEXT NOT NULL,
    request_id VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
