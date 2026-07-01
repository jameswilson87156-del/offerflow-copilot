# OfferFlow Copilot

OfferFlow Copilot 是一个可运行的 Java + Vue 求职辅助作品集工程。它面向实习/早期求职场景，用可审计的工作台串联“JD 要求 -> 简历证据 -> 匹配报告 -> 面试前准备 -> 投递跟踪 -> 人工复核 -> Provider Trace”。

## 当前阶段：P4D Human Review Gate + Copy Permission Contract

当前版本保留 P4A 的 Flyway + H2/MySQL persistence 基础、P4B 的 Provider SPI 和 P4C 的 Prompt/Schema/Risk contract，并新增统一 Copy Permission Contract。默认 Provider 仍是 `local-rule`；OpenAI-compatible 与 DeepSeek 只有 no-op adapter 结构，不会发起真实外部网络请求，也不会保存真实 API Key。任何未来真实 Provider 输出都必须先通过 schema validate 与 risk guard，再进入 Human Review，最后通过 Copy Permission Contract 才可复制。

匹配报告现在是可版本化、可解释、可复核、可审计的输出资产；只有 `CONFIRMED` 后才允许复制确认版摘要。P4D 新增 `copy_permission_audit_event` 作为统一复制门禁审计表；旧 `POST /api/match-reports/{versionId}/copy-check` 继续兼容，并内部复用 `CopyPermissionService`。`ARCHIVED` 是只读归档状态，不能送审；当前 actor 仍是 demo user，当前仍不是生产级权限系统。

关键边界：

- 不接真实 LLM，不调用 DeepSeek，不调用中转站。
- 不保存 API Key，Provider 配置只显示 masked / disabled / not configured。
- 不接 Boss、牛客、实习僧等招聘平台 API，不爬取网页。
- 不保存真实手机号、邮箱、身份证、聊天记录等隐私。
- 不自动投递，不做实时面试辅助或作弊功能。
- 不输出 Offer 概率、录取概率，不承诺保证通过。
- 不虚构真实用户、客户、流量或生产级能力。
- Schema Validate / Risk Guard 通过不代表可复制，只有 Human Review Confirmed 后才可复制。
- 当前 actor 是 demo user，不是生产鉴权或生产级权限系统。

## 技术栈

- 后端：Java 17、Spring Boot 3、MyBatis-Plus、Flyway、H2/MySQL、Maven
- 数据库：默认 H2 in-memory demo；`mysql` profile + Docker Compose 仅用于本地开发/演示，不是生产部署方案
- 前端：Vue 3、Vite、TypeScript、原生 CSS
- 截图：Playwright

## 当前接口

| 方法 | 路径 | 数据来源 |
| --- | --- | --- |
| GET | `/api/health` | local-rule 状态 |
| GET | `/api/provider/status` | Provider SPI 配置状态 |
| GET | `/api/dashboard/summary` | 组合 service，演示统计 |
| GET | `/api/jobs/demo-analysis` | H2 job_post + parse version + evidence binding 组合读取 |
| GET | `/api/jobs` | H2 seeded/manual JD list |
| GET | `/api/jobs/{id}` | H2 JD detail + parse versions + evidence bindings + audit trail |
| POST | `/api/jobs` | H2 创建手动粘贴 JD + audit event |
| PUT | `/api/jobs/{id}` | H2 更新 JD + audit event |
| POST | `/api/jobs/{id}/parse` | H2 创建 local-rule parse version + audit event |
| POST | `/api/jobs/{id}/bind-evidence` | H2 创建 JD evidence bindings + audit event |
| GET | `/api/jobs/{id}/parse-versions` | H2 JD parse version history |
| GET | `/api/jobs/{id}/audit-events` | H2 JD audit events |
| GET | `/api/jobs/{id}/evidence-bindings` | H2 JD evidence bindings |
| GET | `/api/evidence/library` | H2 seeded demo data |
| GET | `/api/evidence/coverage` | H2 seeded demo data + deterministic local-rule |
| GET | `/api/evidence/{id}` | H2 seeded demo data + evidence audit trail |
| GET | `/api/evidence/{id}/audit-events` | H2 evidence audit events |
| POST | `/api/evidence` | H2 创建 Draft evidence + audit event |
| PUT | `/api/evidence/{id}` | H2 更新 evidence + changed fields audit event |
| POST | `/api/evidence/{id}/confirm` | H2 evidence 状态更新 + audit event |
| POST | `/api/evidence/{id}/return-to-draft` | H2 evidence 状态更新 + audit event |
| POST | `/api/evidence/{id}/archive` | H2 evidence 状态更新 + audit event |
| POST | `/api/evidence/{id}/restore` | H2 evidence 状态更新 + audit event |
| GET | `/api/reviews` | H2 seeded demo data |
| GET | `/api/reviews/{id}` | H2 seeded demo data + audit trail |
| GET | `/api/reviews/{id}/audit-events` | H2 audit events |
| POST | `/api/reviews/{id}/confirm` | H2 状态更新 + audit event |
| POST | `/api/reviews/{id}/return` | H2 状态更新 + audit event |
| POST | `/api/reviews/{id}/flag-risk` | H2 状态/风险更新 + audit event |
| GET | `/api/provider/settings` | ProviderDescriptor 列表与安全边界 |
| GET | `/api/provider/config-check` | Provider SPI 配置校验，不泄露 API Key |
| POST | `/api/provider/sandbox-run` | no-op/local-rule 沙箱运行，写入 provider_trace_run + trace_step |
| GET | `/api/provider/contracts` | Provider taskType 合同摘要，不泄露 API Key |
| GET | `/api/provider/contracts/{taskType}` | PromptContract detail |
| POST | `/api/provider/validate-response` | 本地模拟 ProviderResponse contract validation，不发外部请求 |
| GET | `/api/provider/traces` | H2 seeded demo data |
| GET | `/api/provider/traces/{runId}` | H2 seeded demo data |
| POST | `/api/copy-permissions/check` | H2 统一复制门禁检查，写 copy_permission_audit_event |
| GET | `/api/copy-permissions/audit-events` | H2 按 targetType/targetId 查询复制门禁审计历史 |
| GET | `/api/match-report/demo` | H2 latest match_report_version，兼容旧报告字段 |
| POST | `/api/jobs/{id}/match-reports/generate` | H2 基于最新 JD parse/evidence bindings 生成报告版本 + Human Review handoff |
| GET | `/api/jobs/{id}/match-reports` | H2 匹配报告版本历史 |
| GET | `/api/match-reports/{versionId}` | H2 单个匹配报告版本详情 |
| GET | `/api/match-reports/{versionId}/audit-events` | H2 匹配报告版本审计历史 |
| POST | `/api/match-reports/{versionId}/send-to-review` | H2 版本状态更新 + audit event |
| POST | `/api/match-reports/{versionId}/archive` | H2 版本归档 + audit event |
| POST | `/api/match-reports/{versionId}/restore` | H2 从 Returned/Risk Flagged/Archived 恢复为 Draft + audit event |
| POST | `/api/match-reports/{versionId}/copy-check` | 兼容旧响应，内部复用 CopyPermissionService，并继续写 COPY_ENABLED/COPY_BLOCKED |
| GET | `/api/interview-prep/demo` | H2 seeded demo data |
| GET | `/api/applications` | H2 seeded demo data |

## 页面路径

- `/jd-analyzer`：结构化 JD Intake、解析版本、证据绑定与 JD Audit Trail
- `/evidence-library`：简历证据库、Evidence Coverage Map、编辑工作流与 Audit Trail
- `/match-report`：匹配报告、版本历史、Copy Permission Contract、Human Review 同步状态与报告审计
- `/interview-prep`：面试前准备与 Copy Gate
- `/application-tracker`：投递跟踪
- `/human-review`：人工复核中心，包含 Review History / Audit Trail
- `/provider-settings`：Provider 设置与证据链

## 本地运行

```bash
# 后端，默认 http://localhost:8080
mvn spring-boot:run

# 前端，默认 http://localhost:5173
cd frontend
npm install
npm run dev
```

默认使用 H2 in-memory 数据库，启动时由 `PersistenceSeedService` 在空表中插入脱敏 demo 数据。H2 控制台路径为 `/h2-console`。更多说明见 [docs/persistence.md](docs/persistence.md)、[docs/provider-spi.md](docs/provider-spi.md)、[docs/provider-sandbox.md](docs/provider-sandbox.md)、[docs/provider-contracts.md](docs/provider-contracts.md)、[docs/provider-response-validation.md](docs/provider-response-validation.md)、[docs/copy-permission-contract.md](docs/copy-permission-contract.md)、[docs/human-review-audit.md](docs/human-review-audit.md)、[docs/evidence-audit.md](docs/evidence-audit.md)、[docs/jd-intake.md](docs/jd-intake.md)、[docs/match-report-versioning.md](docs/match-report-versioning.md) 和 [docs/match-report-review-sync.md](docs/match-report-review-sync.md)。

Schema 统一由 `src/main/resources/db/migration` 下的 Flyway migration 管理，`schema.sql` 仅作为未启用的历史 fallback 参考，不再由默认、test 或 mysql profile 自动执行。启动本地 MySQL：

```bash
docker compose up -d mysql
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
docker compose stop mysql
```

默认本地凭据仅供 demo 使用，可通过 `OFFERFLOW_DB_URL`、`OFFERFLOW_DB_USERNAME`、`OFFERFLOW_DB_PASSWORD` 覆盖。完整说明见 [docs/database-migration.md](docs/database-migration.md) 和 [docs/local-mysql.md](docs/local-mysql.md)。不要提交 `.env`、真实凭据、API Key 或真实招聘隐私数据。

## 验收

```bash
mvn test
cd frontend
npm run build
npm run screenshots
git diff --check
```

更多边界与实现说明见 [docs/project-boundary.md](docs/project-boundary.md)、[docs/architecture.md](docs/architecture.md)、[docs/persistence.md](docs/persistence.md)、[docs/provider-spi.md](docs/provider-spi.md)、[docs/provider-sandbox.md](docs/provider-sandbox.md)、[docs/provider-contracts.md](docs/provider-contracts.md)、[docs/provider-response-validation.md](docs/provider-response-validation.md)、[docs/copy-permission-contract.md](docs/copy-permission-contract.md)、[docs/database-migration.md](docs/database-migration.md)、[docs/local-mysql.md](docs/local-mysql.md) 和 [docs/design/README.md](docs/design/README.md)。
