# Manual Provider Dry-run Verification

This document records P4F/P4G manual real Provider dry-run verification using sanitized input only. It intentionally records only status metadata and trace IDs.

It does not record API keys. It does not record raw model responses. It does not claim stable production provider integration. It does not bypass the copy gate.

## Verification Summary

| Provider | Success | External call attempted | External call blocked | Final provider | Model | Fallback used | Schema validated | Risk guard passed | Human review required | Copy allowed | Raw response saved | Trace ID |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| DeepSeek | `true` | `true` | `false` | `deepseek` | `deepseek-v4-pro` | `false` | `true` | `true` | `true` | `false` | `false` | `P4F-96549DC4` |
| OpenAI-compatible relay | `true` | `true` | `false` | `openai-compatible` | `gpt-5.5` | `false` | `true` | `true` | `true` | `false` | `false` | `P4F-2D3FF22E` |

## Boundary Notes

- This is an optional manual dry-run path, not production model gateway behavior.
- The request input was sanitized before manual execution.
- API keys are read from local environment configuration only and are not written here.
- Raw provider output is not persisted or copied into this repository.
- Passing Schema Validate and Risk Guard does not bypass Human Review.
- `copyAllowed=false` remains the expected state until the relevant output is Human Review Confirmed and Copy Permission passes.

## Follow-up Guidance

Future manual dry-run records should use the same metadata-only format: provider, final provider, model, validation flags, review/copy gates, raw response save state, and trace ID. Do not paste provider response text into docs, tests, logs, screenshots, or commits.
