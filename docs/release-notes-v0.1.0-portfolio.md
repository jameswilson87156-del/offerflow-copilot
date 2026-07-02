# Release Notes: v0.1.0-portfolio

## Summary

OfferFlow Copilot portfolio-ready release.

This release packages the repository for GitHub portfolio review. It focuses on an evidence-first Java + AI workflow demo with local deterministic behavior, audit trails, review gates, provider traceability, and screenshot-backed verification.

## Included Capabilities

- Structured JD Intake
- Resume Evidence Library + Audit Trail
- Match Report Versioning
- Human Review Audit Trail
- Provider Trace Evidence
- Provider SPI
- Prompt / Schema Contract
- Risk Guard
- Copy Permission Contract
- Local Permission Workflow
- Flyway + H2/MySQL
- Docker Compose MySQL
- GitHub Actions CI
- Playwright screenshot validation
- Optional manual real Provider dry-run path verified with DeepSeek and OpenAI-compatible relay

## Verification

Current local acceptance for this portfolio release candidate:

- `mvn test`: 126 tests passed
- `npm run build`: passed
- `npm run screenshots`: 18 passed
- GitHub Actions workflow added for backend tests, frontend build, and screenshot validation

If the test count changes later, trust the latest command output over this release note.

## Boundaries

- Not a production recruiting platform.
- No recruiting platform API.
- No crawler.
- No auto-apply.
- No real user data.
- No Offer prediction.
- No real-time interview assistance.
- Local permission is demo-level.
- Real Provider dry-run is manual and disabled by default.
- Raw response is not saved.

## Suggested Git Tag

Suggested tag after maintainer confirmation:

```bash
v0.1.0-portfolio
```

This document does not create the tag. Tagging should wait for explicit confirmation.
