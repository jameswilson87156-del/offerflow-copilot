# HANDOFF

## Current State

Latest commit before P5A: `dcca920 docs: record manual provider dry-run verification`.

P5A polishes the GitHub-facing README and supporting docs for portfolio review. It does not add large runtime features, does not call real providers, and does not change the core product workflow.

Completed engineering scope:

- P1B-P2D: Vue product pages and screenshot-backed UI flow.
- P3A-P3G: persistence, audit trails, JD intake, evidence audit, match report versioning, Human Review, Copy Permission, and archived read-only states.
- P4A: Flyway, H2/MySQL compatibility, and local Docker Compose MySQL.
- P4B-P4E: Provider SPI, prompt/schema/risk contract hardening, Copy Permission Contract, and local role permission workflow.
- P4F-P4G: manual real Provider dry-run path and provider response schema alignment.
- Manual DeepSeek and OpenAI-compatible relay dry-run results recorded as sanitized metadata only.

## P5A Documentation Scope

- README now follows a GitHub portfolio showcase structure.
- Runtime screenshot evidence table uses only actual files under `docs/images/` and `docs/images/large/`.
- Mermaid workflow and architecture diagrams were added to README.
- Provider dry-run wording is constrained to optional/manual/disabled-by-default language.
- Boundary docs clarify no API keys, no raw provider responses, no recruiting platform API, no crawler, no auto-apply, no real user data, and no production recruiting claim.

## Manual Provider Dry-run Summary

| Provider | Result | Final provider | Model | Trace ID | Review/copy boundary |
| --- | --- | --- | --- | --- | --- |
| DeepSeek | `success=true`, `externalCallAttempted=true`, `externalCallBlocked=false`, `fallbackUsed=false`, `schemaValidated=true`, `riskGuardPassed=true`, `rawResponseSaved=false` | `deepseek` | `deepseek-v4-pro` | `P4F-96549DC4` | `humanReviewRequired=true`, `copyAllowed=false` |
| OpenAI-compatible relay | `success=true`, `externalCallAttempted=true`, `externalCallBlocked=false`, `fallbackUsed=false`, `schemaValidated=true`, `riskGuardPassed=true`, `rawResponseSaved=false` | `openai-compatible` | `gpt-5.5` | `P4F-2D3FF22E` | `humanReviewRequired=true`, `copyAllowed=false` |

These are manual dry-run verification summaries, not raw model responses. They do not include API keys and do not establish production provider integration.

## Local Run

Backend:

```bash
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Default mode uses H2 seeded demo data and `local-rule`. MySQL can be started locally with Docker Compose for profile validation, but this is local development/demo infrastructure only.

## Acceptance Commands

```bash
mvn test
cd frontend
npm run build
npm run screenshots
cd ..
git diff --check
git status --short
```

## Boundaries To Preserve

- Do not commit `.env` or credentials.
- Do not print or document API key values.
- Do not record raw provider responses.
- Do not call real providers in automated tests.
- Do not describe fallback/local-rule output as real model success.
- Do not add recruiting platform APIs, crawlers, auto-apply, real user data, Offer probability, admission probability, guaranteed pass claims, or real-time interview assistance.
- Keep Copy Permission blocked until Human Review is confirmed and Copy Permission Contract passes.
