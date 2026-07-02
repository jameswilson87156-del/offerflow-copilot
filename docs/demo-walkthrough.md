# Demo Walkthrough

This is a 5-8 minute walkthrough script for HR reviewers or interviewers. It shows OfferFlow Copilot as a portfolio-grade engineering demo, not as a production recruiting system.

## 1. Project Positioning

Start at the README.

Say:

> OfferFlow Copilot is an evidence-first Java + AI workflow demo for job-search scenarios. The point is not to auto-generate application answers. The point is to show how generated output can be grounded in reviewed resume evidence, versioned, traced, and blocked from copying until human review and copy permission pass.

Show:

- Core Workflow diagram
- Architecture Overview
- Project Boundaries
- Verification section

## 2. JD Analyzer

Open the JD Analyzer page.

Show:

- Manual JD input and structured intake flow
- Parse version context
- Requirement cards
- Resume evidence binding
- Evidence coverage and gaps

Say:

> The workflow starts from manually provided JD text. It does not crawl job platforms. Parsed requirements are connected to explicit resume evidence so later reports can show where each claim came from.

## 3. Resume Evidence Library

Open the Resume Evidence Library page.

Show:

- Evidence status
- Evidence cards
- Evidence detail
- Edit and review context
- Audit history
- Coverage map

Say:

> Resume evidence is treated as a reusable asset library. Each item has status and audit history, so generated reports cannot silently invent unsupported claims.

## 4. Match Report

Open the Match Report page.

Show:

- Versioned report
- Score breakdown
- Evidence source bindings
- Skill gaps
- Human Review handoff
- Copy Permission panel

Say:

> The match report is versioned and tied back to the JD parse version and evidence bindings. The score is for demo ranking and explanation only. The app does not predict offers, admission probability, or guaranteed outcomes.

## 5. Human Review

Open the Human Review page.

Show:

- Review queue
- `confirm`
- `return`
- `flag-risk`
- Audit timeline
- Risk state

Say:

> Draft output must pass human review before it can become confirmed content. Returning or flagging risk creates audit evidence instead of silently allowing the draft to move forward.

## 6. Provider Settings

Open Provider Settings.

Show:

- Provider descriptors
- Provider Trace
- Schema Validate
- Risk Guard
- Fallback states
- Manual dry-run boundary

Say:

> The default provider path is deterministic `local-rule`. The real Provider path is optional, manual, disabled by default, and still requires schema validation, risk guard, human review, and copy permission. This is not a production DeepSeek, GPT, or relay integration claim.

## 7. Copy Permission

Return to Match Report or Interview Prep.

Show:

- Copy Permission Contract
- Copy blocked before `Confirmed`
- Copy audit event

Say:

> Copy permission is intentionally separate from generation. Until the review state is confirmed and the copy contract passes, the UI keeps copy blocked.

## 8. CI, Screenshots, And Tests

Return to README or GitHub Actions documentation.

Show:

- `mvn test`
- `npm run build`
- `npm run screenshots`
- GitHub Actions workflow
- Screenshot evidence table
- Screenshot manifest

Say:

> The showcase is backed by local and CI verification. Playwright captures runtime screenshots and checks horizontal overflow. CI does not require real Provider API keys and does not call real providers.

## 9. Project Boundaries

Close on the Project Boundaries section.

Say:

> The project is a portfolio engineering demo. It does not connect to recruiting platform APIs, does not crawl pages, does not auto-apply, does not save real private user data, does not predict offers, and does not provide real-time interview assistance.
