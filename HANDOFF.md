# HANDOFF

## 当前交付

P3F 已完成 Match Report Review Sync。匹配报告现在基于当前最新 `jd_parse_version` 与 `jd_evidence_binding` 生成 `match_report_version`，每次生成形成一个独立 version，默认 `DRAFT`，写入 `match_report_audit_event`，并创建或关联 `MATCH_REPORT` 类型的 `human_review_item`。

P3F 在 P3E 基础上建立了闭环：Human Review confirm / return / flag-risk 会同步更新关联的 `match_report_version` 状态，并写入 `match_report_audit_event`。报告只有 `CONFIRMED` 后才允许复制确认版摘要；`DRAFT`、`IN_REVIEW`、`RETURNED`、`RISK_FLAGGED`、`ARCHIVED` 都不可作为正式建议使用。

P3D Structured JD Intake、P3C Resume Evidence Editable Workflow 和 P3B Human Review Audit Trail 仍然保留。核心数据仍是脱敏 seed demo data，接口语义仍是 `mock/local-rule`。本轮没有接真实 LLM、DeepSeek、中转站、招聘平台 API 或爬虫，也没有保存 API Key 或真实隐私。

## 启动顺序

1. 根目录执行 `mvn spring-boot:run`。
2. `frontend` 目录执行 `npm install && npm run dev`。
3. 打开 `http://localhost:5173`。

默认 H2 in-memory 数据库会在启动时建表并 seed 脱敏 demo 数据。重复启动或重复调用 seed 不会重复插入已有表数据。

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
- `HumanReviewService` 对 MATCH_REPORT review item 执行 confirm / return / flag-risk 时，会同步写入 `HUMAN_REVIEW_CONFIRMED`、`HUMAN_REVIEW_RETURNED` 或 `HUMAN_REVIEW_FLAGGED_RISK` 到 `match_report_audit_event`。
- `ARCHIVED` 版本只读，不能再次 send-to-review；可以 restore 后回到 Draft。
- 当前 scoring 是 deterministic local-rule，不是 Offer 概率、录取概率或真实 LLM 推理。
- 新增证据审计表为 `resume_evidence_audit_event`，由 `EvidenceLibraryService` 在证据写操作事务内写入。
- `GET /api/evidence/{id}` 已包含 `auditTrail`，也可通过 `GET /api/evidence/{id}/audit-events` 单独读取。
- Evidence 写接口支持 `actor`、`actorRole`、`humanNote` 和证据 payload；当前 actor 是 demo user，不是生产鉴权。
- 新增审计表为 `human_review_audit_event`，由 `HumanReviewService` 在状态变更事务内写入。
- `GET /api/reviews/{id}` 已包含 `auditTrail`，也可通过 `GET /api/reviews/{id}/audit-events` 单独读取。
- POST action request 支持 `actor`、`actorRole`、`humanNote`；当前 actor 是 demo user，不是生产鉴权。
- H2 是本地 demo/test persistence；`mysql` profile 仅保留后续可切换配置，本轮不连接真实 MySQL。
- Provider 状态必须如实展示，不能把 fallback 描述为真实 LLM。
- 不保存 API Key 明文，不保存真实隐私，不接招聘平台 API，不爬虫。

## 已验证

- `mvn test`：P3F 要求全部通过，包含原有 47 个测试与新增 Match Report / Human Review 同步、copy-check、restore、archived send-to-review block 覆盖。
- `npm run build`：P3F 前端状态与复制许可区通过构建。
- `npm run screenshots`：刷新 Match Report 与 Human Review 等页面截图，保留其它页面截图。
