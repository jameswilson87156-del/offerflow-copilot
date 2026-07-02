# Real Provider Manual Dry-run

P4F adds a manual real Provider dry-run path for DeepSeek and OpenAI-compatible providers:

`POST /api/provider/real-dry-run`

This is a local, manually enabled verification path, not production provider integration and not an always-on model-service claim. It keeps Provider Contract, PII Guard, Schema Validate, Risk Guard, Human Review, Copy Permission, Local Permission, and fallback behavior visible in one trace.

## Manual Verification Summary

P4F/P4G manual dry-run verification was completed with sanitized input only. The records below are status summaries, not raw model responses, and they do not include API keys.

| Provider | Success | External attempted | External blocked | Final provider | Model | Fallback used | Schema validated | Risk guard passed | Human review required | Copy allowed | Raw response saved | Trace ID |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| DeepSeek | `true` | `true` | `false` | `deepseek` | `deepseek-v4-pro` | `false` | `true` | `true` | `true` | `false` | `false` | `P4F-96549DC4` |
| OpenAI-compatible relay | `true` | `true` | `false` | `openai-compatible` | `gpt-5.5` | `false` | `true` | `true` | `true` | `false` | `false` | `P4F-2D3FF22E` |

This verification confirms the optional manual dry-run path can normalize and validate provider output under the P4G schema alignment. It does not establish production provider integration or bypass the copy gate.

## Default Safety

- Real external calls are off by default: `OFFERFLOW_AI_REAL_CALL_ENABLED=false`.
- Raw model response saving is off by default: `OFFERFLOW_AI_RAW_RESPONSE_SAVE=false`.
- Default provider mode remains `local-rule`.
- API keys are read only from environment variables.
- API keys are never written to code, docs, tests, logs, screenshots, database rows, or API responses.
- Dry-run input must be sanitized; do not input real phone numbers, email addresses, ID cards, chat logs, resumes, or API keys.
- Dry-run output is not copyable. `copyAllowed=false` until Human Review and Copy Permission Contract pass.
- The path does not connect to recruiting platform APIs, crawlers, auto-apply workflows, or real-time interview assistance.

## Environment Variables

The dry-run path reads provider configuration from local environment variables. This document lists names only and does not provide values.

| Purpose | Variable name |
| --- | --- |
| Provider mode | `OFFERFLOW_AI_PROVIDER_MODE` |
| Real-call flag | `OFFERFLOW_AI_REAL_CALL_ENABLED` |
| Raw response save flag | `OFFERFLOW_AI_RAW_RESPONSE_SAVE` |
| Timeout | `OFFERFLOW_AI_TIMEOUT_MS` |
| DeepSeek base URL | `OFFERFLOW_DEEPSEEK_BASE_URL` |
| DeepSeek secret | `OFFERFLOW_DEEPSEEK_API_KEY` |
| DeepSeek model | `OFFERFLOW_DEEPSEEK_MODEL` |
| OpenAI-compatible base URL | `OFFERFLOW_OPENAI_COMPATIBLE_BASE_URL` |
| OpenAI-compatible secret | `OFFERFLOW_OPENAI_COMPATIBLE_API_KEY` |
| OpenAI-compatible model | `OFFERFLOW_OPENAI_COMPATIBLE_MODEL` |

The config-check APIs only report configured/masked state. They must not print key values.

## Request

```json
{
  "providerMode": "deepseek",
  "taskType": "match-report",
  "inputText": "Sanitized JD summary and sanitized evidence notes.",
  "actor": "demo.owner",
  "actorRole": "OWNER",
  "allowExternalCall": true,
  "confirmNoPii": true
}
```

Supported `providerMode` values are `deepseek` and `openai-compatible`.

## Response Summary

The response includes:

- `success`
- `externalCallAttempted`
- `externalCallBlocked`
- `providerMode`
- `finalProvider`
- `model`
- `fallbackUsed`
- `fallbackReason`
- `schemaValidated`
- `riskGuardPassed`
- `humanReviewRequired`
- `copyAllowed=false`
- `rawResponseSaved=false`
- `traceId`
- `runId`
- `riskFlags`
- `boundaryNotice`

It does not include the API key or raw model response.

## Trigger Conditions

An external call can be attempted only when all conditions are true:

- actor has `PROVIDER_REAL_DRY_RUN`
- actor role is `OWNER` or `EDITOR`
- actor role is not `SYSTEM`
- `providerMode` is `deepseek` or `openai-compatible`
- `allowExternalCall=true`
- `confirmNoPii=true`
- `realCallEnabled=true`
- provider base URL is configured
- provider API key is configured
- PII Guard passes

If any condition fails, the external call is blocked or the path falls back to `local-rule`.

## PII Guard

Input is blocked before any external call when it contains:

- email address
- China mainland mobile number
- China resident ID card pattern
- API key style text
- `sk-` style secret pattern

Blocked input writes provider trace evidence and returns `externalCallAttempted=false`.

## Fallback Strategy

Fallback to `local-rule` is required when:

- real calls are disabled
- provider config is missing
- external provider call fails
- external provider response fails Schema Validate or Risk Guard
- provider mode is unsupported

For hard safety blocks such as `confirmNoPii=false` or PII detected, the call is blocked before network. The trace still records why the dry-run did not proceed.

For `provider-sandbox`, JSON provider content is normalized into the `provider-sandbox-v1` contract shape before validation. Plain text provider content is wrapped as a review-gated structured response with `normalizedFromText=true`. An explicit schemaVersion mismatch is still a validation failure and still falls back.

## Trace Evidence

Each real dry-run writes a `provider_trace_run` and 14 `trace_step` records:

1. Actor Permission Check
2. Real Call Flag Check
3. PII Guard
4. Prompt Contract Load
5. Risk Policy Load
6. Provider Config Check
7. Provider Select
8. External Provider Call or External Call Blocked
9. Provider Response Normalize
10. Schema Validate
11. Risk Guard
12. Fallback Decision
13. Human Review Required
14. Copy Permission Blocked

Trace detail records only normalized status, fallback reason, risk flags, and review/copy gates. Raw model response is not saved.

## Frontend

`/provider-settings` includes a Real Provider Dry-run panel with:

- DeepSeek / OpenAI-compatible provider selector
- taskType selector
- sanitized input textarea
- `confirmNoPii` checkbox
- `allowExternalCall` checkbox
- run button gated by local permission
- result summary for blocked/fallback/schema/risk/human-review/copy status
- Trace ID and Run ID

The page does not expose an API key input.

## Testing Boundary

Automated tests mock the real provider gateway. `mvn test` must not depend on real network access, real API keys, or Docker MySQL.
