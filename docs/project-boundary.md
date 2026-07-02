# Project Boundary

OfferFlow Copilot is a portfolio-grade engineering demo for an evidence-first job-application workflow. It demonstrates Java backend architecture, Vue frontend workflow design, persistence, audit trails, provider traceability, and review/copy governance using sanitized demo data.

## Allowed Scope

- Manual JD intake using sanitized user-provided text or seeded demo data.
- Resume evidence management with Draft, Confirmed, Archived, and restored states.
- Evidence binding before match report generation.
- Versioned match reports with audit events and Human Review handoff.
- Human Review status transitions with audit trails.
- Copy Permission Contract after Human Review confirmation.
- Provider SPI, prompt/schema contracts, response validation, risk guard, fallback, and trace evidence.
- Optional manual real Provider dry-run with sanitized input, explicit local opt-in, and environment-based credentials.
- H2 demo mode and local MySQL profile for development validation.
- Playwright screenshot validation of the running app.

## Explicitly Out Of Scope

- Production recruiting platform behavior.
- Recruiting platform API integration.
- Crawlers or page scraping.
- Auto-apply or automated messaging to recruiters.
- Real user data, real resumes, phone numbers, email addresses, ID cards, chat logs, or credentials.
- Offer probability, admission probability, ranking guarantees, or guaranteed pass claims.
- Real-time interview assistance or cheating workflows.
- Production authentication, authorization, compliance, or security claims.
- Stable production provider integration claims.
- Raw provider response storage.

## Provider Boundary

Default provider behavior is deterministic `local-rule`. OpenAI-compatible and DeepSeek adapters are represented through Provider SPI descriptors, no-op sandbox behavior, and a manually enabled dry-run path.

The optional real Provider dry-run path:

- is disabled by default;
- requires explicit local opt-in;
- requires sanitized input and `confirmNoPii=true`;
- reads API keys only from local environment variables;
- does not save raw provider responses;
- writes trace metadata and review/copy gate status;
- falls back when provider config, external calls, schema validation, or risk guard fail;
- keeps `humanReviewRequired=true` and `copyAllowed=false` until review/copy gates pass.

The manual verification records for DeepSeek and the OpenAI-compatible relay contain only sanitized status metadata and trace IDs. They do not include API keys or raw model responses, and they do not expand the project into production provider integration.

## Human Review And Copy Permission

Generated or local-rule output starts as Draft. Schema Validate and Risk Guard are necessary but not sufficient for copying. Copy Permission requires:

- `schemaValidated=true`
- `riskGuardPassed=true`
- target status `CONFIRMED`
- Human Review status `Confirmed`

Archived, Draft, Returned, Risk Flagged, or In Review content cannot be copied as formal output.

## Data Boundary

The repository uses sanitized seed data. Do not commit:

- `.env` files;
- real API keys;
- raw provider responses;
- real resumes;
- real personal data;
- database dumps containing private data;
- screenshots containing private data.

## Screenshot Boundary

Runtime screenshots used for README evidence must come from `docs/images/` and `docs/images/large/`. Files under `docs/design/references/` are design references only and must not be presented as real running-app screenshots.
