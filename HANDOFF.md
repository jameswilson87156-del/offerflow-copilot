# HANDOFF

## 当前交付

P4A 已完成 Flyway Migration + MySQL/H2 Compatibility + Docker Compose。P3G 的页面、接口、审计和只读状态保持不变；本轮只收口数据库 migration、本地 MySQL 启动与兼容验证。

Match Report、Human Review、Evidence Library 的审计事件均可卡内展开，展示 action、状态变化、actor/role、human note、trace、changed fields 和时间。copy-check 的 `allowed`、reason、version status、Human Review status 和 Boundary Notice 会随 `COPY_ENABLED` / `COPY_BLOCKED` 保存在 `match_report_audit_event`。

只有 `CONFIRMED` 后才允许复制确认版摘要；`ARCHIVED` 是只读归档状态，不能送审。Evidence Archived 仅保留 restore，结束态 Human Review 禁用动作并展示原因；Returned / Risk Flagged / Archived 统一使用只读视觉提示。

所有 15 张业务表由 `db/migration/V1__init_offerflow_schema.sql` 建立。默认/test 使用 H2，`mysql` profile 可连接本地 MySQL 8；Flyway 完成后才运行 count-guarded demo seed。核心数据仍是脱敏 seed demo data，接口语义仍是 `mock/local-rule`。本轮没有接真实 LLM、DeepSeek、中转站、招聘平台 API 或爬虫，也没有保存 API Key 或真实隐私。

## 启动顺序

1. 根目录执行 `mvn spring-boot:run`。
2. `frontend` 目录执行 `npm install && npm run dev`。
3. 打开 `http://localhost:5173`。

默认 H2 in-memory 数据库由 Flyway 建表并 seed 脱敏 demo 数据。重复启动或重复调用 seed 不会重复插入已有表数据。本地 MySQL 可执行 `docker compose up -d mysql` 后，以 `mvn spring-boot:run -Dspring-boot.run.profiles=mysql` 启动；结束后执行 `docker compose stop mysql`，named volume 会保留。

## 关键约定

- Java package 固定为 `com.offerflow.copilot`。
- 数据库 JSON 字段先用 `TEXT` 保存字符串，通过 `JsonCodec` 统一序列化/反序列化。
- 新增 JD Intake 表为 `jd_parse_version`、`jd_evidence_binding` 和 `jd_audit_event`，由 `JobIntakeService` 在 JD 创建、更新、解析和绑定证据时写入。
- `GET /api/jobs/{id}` 已包含当前 parse version、版本历史、evidence bindings 和 audit trail；也可通过 `GET /api/jobs/{id}/parse-versions`、`GET /api/jobs/{id}/evidence-bindings`、`GET /api/jobs/{id}/audit-events` 单独读取。
- JD 写接口支持 `actor`、`actorRole`、`humanNote`；当前 actor 是 demo user，不是生产鉴权。
- JD 来源固定为用户手动粘贴和脱敏 seed demo，不接招聘平台 API，不爬取网页。
- 新增匹配报告版本表为 `match_report_version`，审计表为 `match_report_audit_event`。
- `GET /api/match-report/demo` 继续兼容旧前端字段，但数据来自当前最新 match report version。
- `POST /api/jobs/{id}/match-reports/generate` 会生成新版本、写审计事件，并创建 Human Review handoff item。
- `GET /api/jobs/{id}/match-reports`、`GET /api/match-reports/{versionId}`、`GET /api/match-reports/{versionId}/audit-events` 分别读取版本列表、详情和审计历史。
- `POST /api/match-reports/{versionId}/send-to-review` 和 `/archive` 会更新版本状态并写入 match report audit event。
- `POST /api/match-reports/{versionId}/restore` 会把 `ARCHIVED`、`RETURNED` 或 `RISK_FLAGGED` 版本恢复到 `DRAFT`，并写入 `RESTORE_VERSION` audit event。
- `POST /api/match-reports/{versionId}/copy-check` 会按版本状态返回复制许可，并写入 `COPY_ENABLED` 或 `COPY_BLOCKED` audit event。
- copy-check audit event 额外保存 `copy_allowed`、`copy_reason`、`version_status`、`human_review_status` 和 `boundary_notice`，供前端展开查看历史。
- 三处 Audit Trail 共用 `AuditEventDisclosure` 组件；状态文案共用 `utils/status.ts`，避免页面间颜色和翻译漂移。
- `HumanReviewService` 对 MATCH_REPORT review item 执行 confirm / return / flag-risk 时，会同步写入 `HUMAN_REVIEW_CONFIRMED`、`HUMAN_REVIEW_RETURNED` 或 `HUMAN_REVIEW_FLAGGED_RISK` 到 `match_report_audit_event`。
- `ARCHIVED` 版本只读，不能再次 send-to-review；可以 restore 后回到 Draft。
- 当前 scoring 是 deterministic local-rule，不是 Offer 概率、录取概率或真实 LLM 推理。
- 新增证据审计表为 `resume_evidence_audit_event`，由 `EvidenceLibraryService` 在证据写操作事务内写入。
- `GET /api/evidence/{id}` 已包含 `auditTrail`，也可通过 `GET /api/evidence/{id}/audit-events` 单独读取。
- Evidence 写接口支持 `actor`、`actorRole`、`humanNote` 和证据 payload；当前 actor 是 demo user，不是生产鉴权。
- 新增审计表为 `human_review_audit_event`，由 `HumanReviewService` 在状态变更事务内写入。
- `GET /api/reviews/{id}` 已包含 `auditTrail`，也可通过 `GET /api/reviews/{id}/audit-events` 单独读取。
- POST action request 支持 `actor`、`actorRole`、`humanNote`；当前 actor 是 demo user，不是生产鉴权。
- Flyway 是 schema 唯一自动初始化入口；默认、test、mysql profile 均设置 `spring.sql.init.mode=never`。
- `schema.sql` 仅保留为历史 fallback 参考，不自动执行，避免与 Flyway 重复建表。
- H2 是默认 demo/test persistence；`mysql` profile 与 `docker-compose.yml` 仅用于本地开发/演示，不是生产部署声明。
- Provider 状态必须如实展示，不能把 fallback 描述为真实 LLM。
- 不保存 API Key 明文，不保存真实隐私，不接招聘平台 API，不爬虫。

## 验收范围

- 后端覆盖 COPY_ENABLED / COPY_BLOCKED 结构化详情、Archived send-to-review block、Archived copy block、restore to Draft 和审计详情字段。
- 前端覆盖 Match Report、Human Review、Evidence Library 的 1366x768、1440x900、1920x1080 截图与横向溢出检查。
- `mvn test`：56 tests，0 failures / errors；包含 Flyway history 与关键表存在性检查。
- `npm run build`：Vue TypeScript 与 Vite production build 通过。
- `npm run screenshots`：18 tests 通过。
- MySQL smoke：MySQL 8 上 V1 migration 成功，15 张业务表存在；连续两次后端启动的 seed 计数稳定。
