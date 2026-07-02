# HANDOFF

## Current State

Latest commit before P5C: `19bf1d9 ci: add github actions verification workflow`.

Current phase: P5C Release Packaging + Repo Hygiene.

P5C packages the repository as a portfolio-ready local release candidate. It does not add runtime business features, does not call real providers, and does not change the core product workflow. The intended commit message for this pass is `docs: add release packaging checklist`.

Completed engineering scope:

- P1B-P2D: Vue product pages and screenshot-backed UI flow.
- P3A-P3G: persistence, audit trails, JD intake, evidence audit, match report versioning, Human Review, Copy Permission, and archived read-only states.
- P4A: Flyway, H2/MySQL compatibility, and local Docker Compose MySQL.
- P4B-P4E: Provider SPI, prompt/schema/risk contract hardening, Copy Permission Contract, and local role permission workflow.
- P4F-P4G: manual real Provider dry-run path and provider response schema alignment.
- P5A: GitHub README polish and screenshot evidence table.
- P5B: GitHub Actions CI for Maven tests, frontend build, and screenshot validation.
- P5C: release notes, demo walkthrough, screenshot manifest, GitHub templates, README links, and repo hygiene pass.
- Manual DeepSeek and OpenAI-compatible relay dry-run results recorded as sanitized metadata only.

The project is now a portfolio-ready local release candidate. Recommended next step: P5D Portfolio Hub case study integration.

## P5C Documentation Scope

- Release notes document `v0.1.0-portfolio` as a suggested tag only.
- Demo walkthrough gives a 5-8 minute HR/interviewer script.
- Screenshot manifest lists runtime Playwright screenshots and separates them from AI-generated design references.
- GitHub PR and issue templates preserve verification and boundary checks.
- README links release notes, demo walkthrough, screenshot manifest, and CI notes.
- Provider dry-run wording stays constrained to optional/manual/disabled-by-default language.
- Boundary docs continue to clarify no API keys, no raw provider responses, no recruiting platform API, no crawler, no auto-apply, no real user data, and no production recruiting claim.

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
