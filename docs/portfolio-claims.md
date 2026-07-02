# Portfolio Claims

This document keeps public README, resume, and interview wording accurate.

## Safe Claims

- Built a Java + Vue portfolio demo for an evidence-first job-application workflow.
- Implemented JD intake, resume evidence management, match report versioning, Human Review, Provider Trace, and Copy Permission governance.
- Added Flyway-managed H2/MySQL-compatible schema migration for local development and demo validation.
- Added Provider SPI, prompt/schema contracts, response validation, risk guard, fallback, and trace evidence.
- Added an optional real Provider dry-run path manually verified with DeepSeek and an OpenAI-compatible relay using sanitized input only.
- Added Playwright screenshot validation and Maven test coverage.

## Claims To Avoid

- Production recruiting platform.
- Recruiting platform integration.
- Crawler or automated job scraping.
- Auto-apply system.
- Real users, customers, traffic, conversion, or business outcomes.
- Offer prediction, admission probability, ranking guarantee, or guaranteed pass claim.
- Real-time interview assistance or cheating feature.
- Provider dry-run described as a general provider gateway.
- Vendor-backed model integration claim.
- Review/copy gates can be bypassed.

## Provider Dry-run Wording

Accurate:

- Optional real Provider dry-run path.
- Disabled by default.
- API keys are read from local environment variables only.
- Raw provider responses are not saved.
- DeepSeek manual dry-run verified.
- OpenAI-compatible relay manual dry-run verified.
- Output still requires Schema Validate, Risk Guard, Human Review, and Copy Permission.
- `copyAllowed=false` before confirmation.

Inaccurate:

- Always-on model service.
- Vendor-backed model integration.
- Generated output is approved before review.
- Provider success bypasses review or copy gates.

## Human Review And Copy Permission Wording

Accurate:

- Generated/local-rule output starts as Draft.
- Human Review is required before confirmed use.
- Copy Permission is a separate final gate.
- Schema and risk validation are necessary but not sufficient for copying.

Inaccurate:

- Generated output bypasses review.
- Passing validation means output is approved.
- Copy gate is only a frontend UI state.

## Demo Boundary

- Default mode is H2 + sanitized seed data + deterministic `local-rule`.
- OpenAI-compatible and DeepSeek sandbox adapters are no-op/fallback unless the manual dry-run path is explicitly enabled.
- Local role permission is a demo model, not production authentication or authorization.
- MySQL profile and Docker Compose are for local development/demo validation only.
