# HANDOFF

## 当前交付

P3D 已完成 Structured JD Intake。JD 分析台现在支持用户手动粘贴 JD、保存 JD、更新 JD、使用 local-rule 生成解析版本、基于简历证据库生成 evidence bindings，并持久化 `jd_audit_event`，记录操作者、角色、动作、前后状态、changed fields、before/after snapshot、人工备注和时间。

P3C Resume Evidence Editable Workflow 和 P3B Human Review Audit Trail 仍然保留。核心数据仍是脱敏 seed demo data，接口语义仍是 `mock/local-rule`。本轮没有接真实 LLM、DeepSeek、中转站、招聘平台 API 或爬虫，也没有保存 API Key 或真实隐私。

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

- `mvn test`：38 个测试通过，包含 JD Intake 创建/更新/解析/绑定、JD audit endpoint、API 合约、evidence audit event 写入、human review audit event 写入、seed 幂等和 JSON 字段验证。
- `npm run build`：P3D 已通过。
- `npm run screenshots`：待本轮最终验收刷新，会更新 JD Analyzer 截图。
