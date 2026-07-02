# Continuous Integration

OfferFlow Copilot uses GitHub Actions to run the same verification path expected for the portfolio showcase.

## Workflow

The workflow lives at `.github/workflows/ci.yml` and runs on:

- `push`
- `pull_request`

Global CI defaults keep provider behavior local and deterministic:

- `OFFERFLOW_AI_PROVIDER_MODE=local-rule`
- `OFFERFLOW_AI_REAL_CALL_ENABLED=false`
- `OFFERFLOW_AI_RAW_RESPONSE_SAVE=false`

## Jobs

### backend-test

Runs on Ubuntu with Java 17 and Maven dependency caching.

Command:

```bash
mvn -B test
```

This uses the H2/Flyway test path and does not require Docker MySQL, real provider credentials, or real network calls to model providers.

### frontend-build

Runs on Ubuntu with Node 20 and npm dependency caching.

Commands:

```bash
cd frontend
npm ci
npm run build
```

### screenshots

Runs on Ubuntu with Java 17, Node 20, npm cache, Maven cache, and Playwright Chromium.

Commands:

```bash
cd frontend
npm ci
npx playwright install --with-deps chromium
npm run screenshots
```

The existing Playwright config starts the Spring Boot backend with `mvn -f ../pom.xml spring-boot:run` and the Vite frontend with `npm run dev -- --host 127.0.0.1`. The screenshot suite validates runtime pages and horizontal-overflow behavior.

On failure, CI uploads Playwright `frontend/test-results/` and `frontend/playwright-report/` artifacts when those folders exist.

## CI Does Not Do

- It does not call real DeepSeek, OpenAI-compatible, or relay providers.
- It does not require provider API keys.
- It does not read or print real API key values.
- It does not connect to recruiting platform APIs.
- It does not crawl pages.
- It does not auto-apply to jobs.
- It does not run production deployment.
- It does not require Docker MySQL for the main CI path.
- It does not save raw provider responses.

## Local Reproduction

From the repository root:

```bash
mvn test
```

From the frontend directory:

```bash
cd frontend
npm run build
npm run screenshots
```
