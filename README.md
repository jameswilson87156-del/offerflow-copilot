# OfferFlow Copilot

OfferFlow Copilot 是一个可运行的 Java + Vue 求职辅助作品集工程。它面向实习/早期求职场景，用可审计的工作台串联“JD 要求 -> 简历证据 -> 匹配报告 -> 面试前准备 -> 投递跟踪 -> 人工复核 -> Provider Trace”。

## 当前阶段：P3E Match Report Versioning & Human Review Handoff

当前版本在 P3A H2/MyBatis-Plus 持久化基础层、P3B 人工复核审计链路、P3C 简历证据编辑工作流和 P3D 结构化 JD Intake 上，新增了 P3E 匹配报告版本化。每次生成匹配报告都会基于当前 `jd_parse_version` 与 `jd_evidence_binding` 写入一个 `match_report_version`，默认 `DRAFT`，并创建或关联 `MATCH_REPORT` 类型的 Human Review item。匹配报告现在是可版本化、可解释、可复核、可审计的 AI 输出资产；当前 scoring 仍为 `local-rule`，不是 Offer/录取概率。

关键边界：

- 不接真实 LLM，不调用 DeepSeek，不调用中转站。
- 不保存 API Key，Provider 配置只显示 masked / disabled / not configured。
- 不接 Boss、牛客、实习僧等招聘平台 API，不爬取网页。
- 不保存真实手机号、邮箱、身份证、聊天记录等隐私。
- 不自动投递，不做实时面试辅助或作弊功能。
- 不输出 Offer 概率、录取概率，不承诺保证通过。
- 不虚构真实用户、客户、流量或生产级能力。
- 当前 actor 是 demo user，不是生产鉴权或生产级权限系统。

## 技术栈

- 后端：Java 17、Spring Boot 3、MyBatis-Plus、H2、Maven
- 数据库：默认 H2 in-memory demo；`mysql` profile 仅保留后续切换配置，本轮不连接真实 MySQL
- 前端：Vue 3、Vite、TypeScript、原生 CSS
- 截图：Playwright

## 当前接口

| 方法 | 路径 | 数据来源 |
| --- | --- | --- |
| GET | `/api/health` | local-rule 状态 |
| GET | `/api/provider/status` | 组合 service，静态边界状态 |
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
| GET | `/api/provider/settings` | 组合 service，静态边界状态 |
| GET | `/api/provider/traces` | H2 seeded demo data |
| GET | `/api/provider/traces/{runId}` | H2 seeded demo data |
| GET | `/api/match-report/demo` | H2 latest match_report_version，兼容旧报告字段 |
| POST | `/api/jobs/{id}/match-reports/generate` | H2 基于最新 JD parse/evidence bindings 生成报告版本 + Human Review handoff |
| GET | `/api/jobs/{id}/match-reports` | H2 匹配报告版本历史 |
| GET | `/api/match-reports/{versionId}` | H2 单个匹配报告版本详情 |
| GET | `/api/match-reports/{versionId}/audit-events` | H2 匹配报告版本审计历史 |
| POST | `/api/match-reports/{versionId}/send-to-review` | H2 版本状态更新 + audit event |
| POST | `/api/match-reports/{versionId}/archive` | H2 版本归档 + audit event |
| GET | `/api/interview-prep/demo` | H2 seeded demo data |
| GET | `/api/applications` | H2 seeded demo data |

## 页面路径

- `/jd-analyzer`：结构化 JD Intake、解析版本、证据绑定与 JD Audit Trail
- `/evidence-library`：简历证据库、Evidence Coverage Map、编辑工作流与 Audit Trail
- `/match-report`：匹配报告、版本历史、来源元数据、Human Review handoff 与报告审计
- `/interview-prep`：面试前准备
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

默认使用 H2 in-memory 数据库，启动时由 `PersistenceSeedService` 在空表中插入脱敏 demo 数据。H2 控制台路径为 `/h2-console`。更多说明见 [docs/persistence.md](docs/persistence.md)、[docs/human-review-audit.md](docs/human-review-audit.md)、[docs/evidence-audit.md](docs/evidence-audit.md)、[docs/jd-intake.md](docs/jd-intake.md) 和 [docs/match-report-versioning.md](docs/match-report-versioning.md)。

## 验收

```bash
mvn test
cd frontend
npm run build
npm run screenshots
git diff --check
```

更多边界与实现说明见 [docs/project-boundary.md](docs/project-boundary.md)、[docs/architecture.md](docs/architecture.md)、[docs/persistence.md](docs/persistence.md)、[docs/match-report-versioning.md](docs/match-report-versioning.md) 和 [docs/design/README.md](docs/design/README.md)。
