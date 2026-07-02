# Local Permission Model

P4E adds a local demo permission layer for write actions. It is intentionally small and deterministic: no production login, no real user account, no tenant model, no external identity provider, and no compliance-grade authorization.

## Actor Resolution

Backend write APIs resolve the local actor in this order:

1. `actor` / `actorRole` in request body.
2. `X-Demo-Actor` / `X-Demo-Role` headers.
3. Default actor: `demo.reviewer` with role `REVIEWER`.

Legacy aliases are normalized for compatibility:

- `Human reviewer` -> `REVIEWER`
- `JD reviewer` -> `EDITOR`
- `Evidence reviewer` -> `EDITOR`
- `System` -> `SYSTEM`

Invalid or unknown roles are downgraded to `VIEWER`.

## Roles

- `OWNER`: allowed to perform every local demo write action.
- `REVIEWER`: allowed to confirm, return, and flag Human Review items, and run copy-check.
- `EDITOR`: allowed to create/update evidence drafts, create/update/parse JD content, bind JD evidence, generate Match Reports, and send Match Reports to review.
- `VIEWER`: read-only; no write action is allowed.
- `SYSTEM`: reserved for local-rule, seed, provider sandbox, risk guard, and trace actions. It cannot perform human confirmation, return, or risk-flag actions.

## Permission Actions

Human Review:

- `REVIEW_CONFIRM`
- `REVIEW_RETURN`
- `REVIEW_FLAG_RISK`

Evidence:

- `EVIDENCE_CREATE`
- `EVIDENCE_UPDATE`
- `EVIDENCE_CONFIRM`
- `EVIDENCE_ARCHIVE`
- `EVIDENCE_RESTORE`

JD Intake:

- `JD_CREATE`
- `JD_UPDATE`
- `JD_PARSE`
- `JD_BIND_EVIDENCE`

Match Report:

- `MATCH_REPORT_GENERATE`
- `MATCH_REPORT_SEND_TO_REVIEW`
- `MATCH_REPORT_ARCHIVE`
- `MATCH_REPORT_RESTORE`

Copy and Provider:

- `COPY_CHECK`
- `PROVIDER_SANDBOX_RUN`
- `PROVIDER_REAL_DRY_RUN`

`OWNER` and `EDITOR` may run `PROVIDER_REAL_DRY_RUN`. `VIEWER` is denied. `SYSTEM` is not allowed to act as a human real dry-run actor.

## API

- `GET /api/permissions/current-actor`
- `POST /api/permissions/check`
- `GET /api/permissions/audit-events`

Denied write requests return HTTP 403 with a structured `PermissionDecision` that includes `actor`, `actorRole`, `action`, `targetType`, `targetId`, `allowed=false`, `reason`, and `boundaryNotice`.

## Audit

P4E adds `permission_audit_event`.

The table records:

- actor and actor role
- permission action
- target type and target id
- allowed decision
- reason
- boundary notice
- request id
- created time

Allowed key writes and blocked attempts both create permission audit events. Existing business audit tables are not replaced:

- Human Review still writes `human_review_audit_event`.
- Evidence still writes `resume_evidence_audit_event`.
- JD still writes `jd_audit_event`.
- Match Report still writes `match_report_audit_event`.
- Copy check still writes `copy_permission_audit_event`.
- Provider sandbox still writes provider trace data.
- Provider real dry-run still writes provider trace data and records denied attempts as permission audit events.

## Frontend

The top bar shows the current local demo actor and role. The UI switch supports:

- `OWNER`
- `REVIEWER`
- `EDITOR`
- `VIEWER`

Actions that the current role cannot perform are disabled with an inline reason. Provider Settings also shows recent permission audit events and gates the Real Provider Dry-run run button with `PROVIDER_REAL_DRY_RUN`.

## Boundary

This model is local-rule/demo only and is not production authorization. P4F real dry-run can manually call DeepSeek or OpenAI-compatible only after local permission, explicit user toggles, PII Guard, and Provider config checks pass. It still does not connect to recruiting platforms, crawlers, auto-apply workflows, real interview assistance, real privacy collection, real API key storage, or offer/admission probability calculation.
