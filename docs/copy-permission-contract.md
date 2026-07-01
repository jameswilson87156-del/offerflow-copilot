# Copy Permission Contract

P4D adds Copy Permission Contract as the final gate before any AI or local-rule output can be copied for formal use. It sits after Provider schema validation, risk guard, and Human Review.

## Gate Order

1. Schema Validate checks the output structure and expected schema version.
2. Risk Guard blocks unsafe claims such as Offer probability, admission probability, guaranteed pass, real-time interview assistance, real GPT/DeepSeek claims, production-grade model claims, real customer/user claims, or platform automation claims.
3. Human Review confirms facts, evidence sources, and wording boundaries.
4. Copy Permission Contract makes the final copy decision and writes an audit event.

Schema Validate passed does not mean copy is allowed. Risk Guard passed does not mean copy is allowed. Human Review Confirmed is required, and Copy Permission Contract is the final decision point.

## Allowed State

`CONFIRMED` is the only target status that can be copied, and Human Review must also be `Confirmed`.

Blocked states:

- `DRAFT`
- `IN_REVIEW`
- `RETURNED`
- `RISK_FLAGGED`
- `ARCHIVED`

`RETURNED`, `RISK_FLAGGED`, and `ARCHIVED` can never be copied as formal advice. They must be revised, restored, or excluded from current use.

## Backend Shape

Package: `com.offerflow.copilot.copy`

Core types:

- `CopyTargetType`
- `CopyPermissionRequest`
- `CopyPermissionResult`
- `CopyPermissionPolicy`
- `CopyPermissionService`

API:

- `POST /api/copy-permissions/check`
- `GET /api/copy-permissions/audit-events?targetType=...&targetId=...`

`POST /api/match-reports/{versionId}/copy-check` remains compatible and internally reuses `CopyPermissionService`. It still writes `match_report_audit_event` with `COPY_ENABLED` / `COPY_BLOCKED`, and it now also writes `copy_permission_audit_event`.

## Audit Table

`copy_permission_audit_event` records:

- target type and target id
- action: `COPY_ALLOWED` or `COPY_BLOCKED`
- allowed decision and reason
- target status and Human Review status
- schemaValidated and riskGuardPassed
- actor and actorRole
- traceId and providerRunId
- boundaryNotice
- createdAt

The table intentionally does not store requested text, raw model response, real privacy data, API keys, or platform chat records.

## Frontend

- `/match-report` shows Copy Permission Contract results and unified copy audit history. The confirmed summary copy action is only visible when the gate allows it.
- `/interview-prep` shows a Copy Gate block for the current preparation material. The current demo status is Draft, so copy is blocked until review is confirmed.
- `/provider-settings` states that Provider validation passing does not grant copy permission. Copy Permission Contract is the final gate after Human Review.

## Current Boundaries

Current actor is a demo user, not a production identity. This is not a production-grade permission system, tenant model, or immutable audit system.

Current data is anonymized demo/local-rule/no-op data. The system does not save real phone numbers, emails, ID cards, HR chat records, API keys, raw model responses, or requested copy text. It does not connect to recruiting platforms, crawl websites, auto-apply, call OpenAI, call DeepSeek, call relay gateways, output Offer/admission probability, guarantee passing, or provide real-time interview assistance.
