# HANDOFF

## 当前交付

P3B 已完成 Human Review Audit Trail。人工复核中心现在可以持久化记录 `confirm`、`return`、`flag-risk` 的状态流转，每条审计事件包含操作者、角色、动作、前后状态、前后风险等级、人工备注、Trace ID、Trace Hash 和时间。

核心数据仍是脱敏 seed demo data，接口语义仍是 `mock/local-rule`。本轮没有接真实 LLM、DeepSeek、中转站、招聘平台 API 或爬虫，也没有保存 API Key 或真实隐私。

## 启动顺序

1. 根目录执行 `mvn spring-boot:run`。
2. `frontend` 目录执行 `npm install && npm run dev`。
3. 打开 `http://localhost:5173`。

默认 H2 in-memory 数据库会在启动时建表并 seed 脱敏 demo 数据。重复启动或重复调用 seed 不会重复插入已有表数据。

## 关键约定

- Java package 固定为 `com.offerflow.copilot`。
- 数据库 JSON 字段先用 `TEXT` 保存字符串，通过 `JsonCodec` 统一序列化/反序列化。
- 新增审计表为 `human_review_audit_event`，由 `HumanReviewService` 在状态变更事务内写入。
- `GET /api/reviews/{id}` 已包含 `auditTrail`，也可通过 `GET /api/reviews/{id}/audit-events` 单独读取。
- POST action request 支持 `actor`、`actorRole`、`humanNote`；当前 actor 是 demo user，不是生产鉴权。
- H2 是本地 demo/test persistence；`mysql` profile 仅保留后续可切换配置，本轮不连接真实 MySQL。
- Provider 状态必须如实展示，不能把 fallback 描述为真实 LLM。
- 不保存 API Key 明文，不保存真实隐私，不接招聘平台 API，不爬虫。

## 已验证

- `mvn test`：23 个测试通过，包含 API 合约、audit event 写入、audit endpoint、seed 幂等和 JSON 字段验证。
- `npm run build`：待本轮最终验收刷新。
- `npm run screenshots`：待本轮最终验收刷新，会更新 Human Review 审计时间线截图。
