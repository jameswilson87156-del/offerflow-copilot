# OfferFlow Copilot

OfferFlow Copilot 是一个可运行的 Java + Vue 求职辅助作品集工程。它面向实习/早期求职场景，用可审计的工作台串联“JD 要求 -> 简历证据 -> 匹配报告 -> 面试前准备 -> 投递跟踪 -> 人工复核 -> Provider Trace”。

## 当前阶段：P3B Human Review Audit Trail

当前版本在 P3A H2/MyBatis-Plus 持久化基础层上，新增了人工复核审计日志与状态流转历史。核心页面仍然使用 `mock/local-rule` 语义，但数据来源优先读取 H2 seeded demo database。所有 AI 或规则生成内容默认是 Draft，必须经过人工确认后才允许复制使用。

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
| GET | `/api/jobs/demo-analysis` | 组合 service，演示 JD 分析 |
| GET | `/api/evidence/library` | H2 seeded demo data |
| GET | `/api/evidence/coverage` | H2 seeded demo data + deterministic local-rule |
| GET | `/api/reviews` | H2 seeded demo data |
| GET | `/api/reviews/{id}` | H2 seeded demo data + audit trail |
| GET | `/api/reviews/{id}/audit-events` | H2 audit events |
| POST | `/api/reviews/{id}/confirm` | H2 状态更新 + audit event |
| POST | `/api/reviews/{id}/return` | H2 状态更新 + audit event |
| POST | `/api/reviews/{id}/flag-risk` | H2 状态/风险更新 + audit event |
| GET | `/api/provider/settings` | 组合 service，静态边界状态 |
| GET | `/api/provider/traces` | H2 seeded demo data |
| GET | `/api/provider/traces/{runId}` | H2 seeded demo data |
| GET | `/api/match-report/demo` | H2 seeded demo data |
| GET | `/api/interview-prep/demo` | H2 seeded demo data |
| GET | `/api/applications` | H2 seeded demo data |

## 页面路径

- `/jd-analyzer`：JD 证据匹配工作台
- `/evidence-library`：简历证据库与 Evidence Coverage Map
- `/match-report`：匹配报告
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

默认使用 H2 in-memory 数据库，启动时由 `PersistenceSeedService` 在空表中插入脱敏 demo 数据。H2 控制台路径为 `/h2-console`。更多说明见 [docs/persistence.md](docs/persistence.md) 和 [docs/human-review-audit.md](docs/human-review-audit.md)。

## 验收

```bash
mvn test
cd frontend
npm run build
npm run screenshots
git diff --check
```

更多边界与实现说明见 [docs/project-boundary.md](docs/project-boundary.md)、[docs/architecture.md](docs/architecture.md)、[docs/persistence.md](docs/persistence.md) 和 [docs/design/README.md](docs/design/README.md)。
