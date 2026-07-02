# Provider Response Validation

`ProviderResponseValidator` is the local P4C guardrail for simulated or future Provider output. It does not call external networks and does not save raw model responses.

P4F real dry-run calls this same validator after provider response normalization. The validator sees only the normalized `ProviderResponse`; raw model response is discarded and not persisted.

## Responsibilities

- Check required response fields such as `providerMode`, `finalProvider`, and structured JSON fields.
- Parse `structuredJson` and require `provider`, `schemaVersion`, `summary`, `humanReviewRequired`, and `copyAllowed`.
- Reject schema version mismatch.
- Reject forbidden claims such as Offer probability, admission probability, guaranteed pass, real-time interview assistance, production-grade real model integration, real customer/user claims, and platform automation claims.
- Enforce `rawResponseSaved=false`.
- When `realCallEnabled=false`, reject any response that marks an external provider as real success.

## Validation Result

`ProviderValidatedResult` returns:

- `valid`
- `violations`
- `sanitizedOutput`
- `fallbackRequired`
- `humanReviewRequired`
- `riskFlags`
- `schemaVersion`
- `promptVersion`
- `riskPolicyVersion`

Each `ProviderContractViolation` includes `code`, `message`, `field`, `severity`, `fallbackRequired`, and `humanReviewRequired`.

## Fallback And Review

If validation fails, the system marks `fallbackRequired=true`, keeps `humanReviewRequired=true`, records risk flags, and prevents the output from being treated as confirmed success. Sandbox runs write validation-related Trace Evidence steps: Provider Response Validate, Schema Contract Validate, Risk Policy Guard, and Contract Violation Check.

If validation succeeds, the output is still not copyable by itself. P4D requires Human Review Confirmed and Copy Permission Contract approval before a page can expose a confirmed copy action.

For real dry-run, validation failure forces fallback to `local-rule` and writes Schema Validate / Risk Guard / Fallback Decision trace steps. Validation success still returns `humanReviewRequired=true` and `copyAllowed=false`.

P4F remains a manual dry-run path, not a production model gateway, and it does not prove real external model quality or availability.
