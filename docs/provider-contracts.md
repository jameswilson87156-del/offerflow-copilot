# Provider Contracts

P4C adds a local Provider contract layer before any real model adapter exists. It defines which prompt, schema, and risk policy each taskType must use. Current adapters remain `local-rule` / no-op and do not call OpenAI, DeepSeek, or relay gateways.

## Task Mapping

| taskType | promptVersion | schemaVersion | riskPolicyVersion |
| --- | --- | --- | --- |
| `jd-analysis` | `jd-analysis-prompt-v1` | `jd-analysis-schema-v1` | `provider-risk-policy-v1` |
| `evidence-binding` | `evidence-binding-prompt-v1` | `evidence-binding-schema-v1` | `provider-risk-policy-v1` |
| `match-report` | `match-report-prompt-v1` | `match-report-schema-v1` | `provider-risk-policy-v1` |
| `interview-prep` | `interview-prep-prompt-v1` | `interview-prep-schema-v1` | `provider-risk-policy-v1` |
| `opening-message` | `opening-message-prompt-v1` | `opening-message-schema-v1` | `provider-risk-policy-v1` |
| `human-review-rewrite` | `human-review-rewrite-prompt-v1` | `human-review-rewrite-schema-v1` | `provider-risk-policy-v1` |
| `provider-sandbox` | `provider-sandbox-prompt-v1` | `provider-sandbox-schema-v1` | `provider-risk-policy-v1` |

## Registries

- `PromptContractRegistry` owns `PromptContract` definitions, including required inputs, forbidden claims, output schema name, and boundary notice.
- `RiskPolicyRegistry` owns forbidden terms, forbidden claims, required boundary notices, `requireHumanReview=true`, and `allowCopyOnlyAfterConfirmed=true`.
- `ProviderResponseSchemaRegistry` owns required fields for each structured response schema.

All real Provider output must pass contract validation before it can be shown as usable output or enter a copy flow. The current UI exposes this via `GET /api/provider/contracts`, `GET /api/provider/contracts/{taskType}`, and the `/provider-settings` contract panel.

## Required Boundary

Contracts do not mean real GPT, DeepSeek, or relay integration is available. API Key values are not stored, displayed, logged, or returned. Unvalidated output must not bypass Human Review.
