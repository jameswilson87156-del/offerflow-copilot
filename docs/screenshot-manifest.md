# Screenshot Manifest

Runtime screenshots are generated from the running app by Playwright and stored under `docs/images/` and `docs/images/large/`. These files can be used as README and portfolio evidence.

Design references under `docs/design/references/` are AI-generated visual references. They are not runtime screenshots and must not be used as README runtime evidence.

## Runtime Screenshots

| Screenshot | Page | Capability Evidence | Runtime screenshot | From Playwright | README / Portfolio |
| --- | --- | --- | --- | --- | --- |
| `docs/images/offerflow-dashboard.png` | JD Analyzer | Structured JD intake, parse version context, evidence binding, and workbench navigation | Yes | Yes | Yes |
| `docs/images/offerflow-evidence-library.png` | Resume Evidence Library | Evidence cards, status, detail panel, coverage map, and audit context | Yes | Yes | Yes |
| `docs/images/offerflow-match-report.png` | Match Report | Versioned report, score breakdown, evidence source binding, and copy gate | Yes | Yes | Yes |
| `docs/images/offerflow-interview-prep.png` | Interview Prep | Review-gated interview prep draft and copy permission status | Yes | Yes | Yes |
| `docs/images/offerflow-application-tracker.png` | Application Tracker | Manual application tracking without auto-apply behavior | Yes | Yes | Yes |
| `docs/images/offerflow-human-review.png` | Human Review | Review queue, audit trail, risk state, and confirmed/copy boundary | Yes | Yes | Yes |
| `docs/images/offerflow-provider-trace.png` | Provider Settings / Trace | Provider descriptors, config check, prompt/schema contract, trace timeline, and dry-run boundary | Yes | Yes | Yes |
| `docs/images/offerflow-evidence-library-1366.png` | Resume Evidence Library | 1366 viewport audit detail and overflow-safe evidence layout | Yes | Yes | Yes |
| `docs/images/offerflow-human-review-1366.png` | Human Review | 1366 viewport audit detail and overflow-safe review layout | Yes | Yes | Yes |
| `docs/images/offerflow-match-report-1366.png` | Match Report | 1366 viewport copy audit detail and overflow-safe report layout | Yes | Yes | Yes |
| `docs/images/large/offerflow-dashboard.png` | JD Analyzer | 1920 viewport JD workbench layout | Yes | Yes | Yes |
| `docs/images/large/offerflow-evidence-library.png` | Resume Evidence Library | 1920 viewport evidence library layout | Yes | Yes | Yes |
| `docs/images/large/offerflow-match-report.png` | Match Report | 1920 viewport match report layout | Yes | Yes | Yes |
| `docs/images/large/offerflow-interview-prep.png` | Interview Prep | 1920 viewport interview prep layout | Yes | Yes | Yes |
| `docs/images/large/offerflow-application-tracker.png` | Application Tracker | 1920 viewport manual application tracker layout | Yes | Yes | Yes |
| `docs/images/large/offerflow-human-review.png` | Human Review | 1920 viewport human review center layout | Yes | Yes | Yes |
| `docs/images/large/offerflow-provider-trace.png` | Provider Settings / Trace | 1920 viewport provider trace and contract layout | Yes | Yes | Yes |

## Overflow Validation

The Playwright suite also runs a route-level 1366 by 768 horizontal overflow check across:

- JD Analyzer
- Resume Evidence Library
- Match Report
- Interview Prep
- Application Tracker
- Human Review
- Provider Settings / Trace

That overflow check does not create a separate image file.
