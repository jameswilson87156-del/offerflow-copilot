# HANDOFF

## Manual Provider Dry-run Verification

P4F/P4G manual real Provider dry-run has been verified with sanitized input for DeepSeek and an OpenAI-compatible relay. This handoff records only status metadata and trace IDs; it does not record API keys or raw model responses.

| Provider | Result | Final provider | Model | Trace ID | Review/copy boundary |
| --- | --- | --- | --- | --- | --- |
| DeepSeek | `success=true`, `externalCallAttempted=true`, `externalCallBlocked=false`, `fallbackUsed=false`, `schemaValidated=true`, `riskGuardPassed=true`, `rawResponseSaved=false` | `deepseek` | `deepseek-v4-pro` | `P4F-96549DC4` | `humanReviewRequired=true`, `copyAllowed=false` |
| OpenAI-compatible relay | `success=true`, `externalCallAttempted=true`, `externalCallBlocked=false`, `fallbackUsed=false`, `schemaValidated=true`, `riskGuardPassed=true`, `rawResponseSaved=false` | `openai-compatible` | `gpt-5.5` | `P4F-2D3FF22E` | `humanReviewRequired=true`, `copyAllowed=false` |

Boundary: this remains a manual dry-run path, not stable production provider integration. Output still requires Human Review and cannot be copied before the target is Confirmed and Copy Permission passes.

## 当前交付

P4F 已完成 Real Provider Manual Dry-run Integration。P4A 的 Flyway Migration、H2/MySQL 兼容和 Docker Compose 说明保持有效；P4B 的 Provider SPI、P4C 的 Prompt/Schema/Risk contract、P4D 的 Copy Permission Contract 与 P4E 的 Local Permission 继续保留；本轮新增 `POST /api/provider/real-dry-run`、PII Guard、real provider gateway、手动 dry-run trace、`PROVIDER_REAL_DRY_RUN` 权限，以及 `/provider-settings` 的 Real Provider Dry-run 面板。

Match Report、Human Review、Evidence Library 的审计事件均可卡内展开，展示 action、状态变化、actor/role、human note、trace、changed fields 和时间。copy-check 的 `allowed`、reason、version status、Human Review status 和 Boundary Notice 会继续随 `COPY_ENABLED` / `COPY_BLOCKED` 保存在 `match_report_audit_event`；P4D 同时写入统一 `copy_permission_audit_event`。

只有 `CONFIRMED` 后才允许复制确认版摘要；`ARCHIVED` 是只读归档状态，不能送审。Evidence Archived 仅保留 restore，结束态 Human Review 禁用动作并展示原因；Returned / Risk Flagged / Archived 统一使用只读视觉提示。

核心业务表由 Flyway V1 建立；P4D 的 `db/migration/V2__copy_permission_audit_event.sql` 新增统一复制门禁审计表；P4E 的 `db/migration/V3__permission_audit_event.sql` 新增本地权限判断审计表。默认/test 使用 H2，`mysql` profile 可连接本地 MySQL 8；Flyway 完成后才运行 count-guarded demo seed。核心数据仍是脱敏 seed demo data。Provider 默认仍是 `local-rule`，sandbox-run 仍是 local-rule/no-op；P4F 只新增 DeepSeek / OpenAI-compatible 手动 real dry-run 路径，默认关闭，必须手动开启、显式允许外呼、确认无 PII、配置环境变量并通过权限检查。失败、缺配置、验证失败或风险命中都 fallback 到 local-rule 或在网络前 blocked。本轮没有接招聘平台 API 或爬虫，也没有保存 API Key、raw model response 或真实隐私。

## 启动顺序

1. 根目录执行 `mvn spring-boot:run`。
2. `frontend` 目录执行 `npm install && npm run dev`。
3. 打开 `http://localhost:5173`。

默认 H2 in-memory 数据库由 Flyway 建表并 seed 脱敏 demo 数据。重复启动或重复调用 seed 不会重复插入已有表数据。本地 MySQL 可执行 `docker compose up -d mysql` 后，以 `mvn spring-boot:run -Dspring-boot.run.profiles=mysql` 启动；结束后执行 `docker compose stop mysql`，named volume 会保留。

## 关键约定

- Java package 固定为 `com.offerflow.copilot`。
- 数据库 JSON 字段先用 `TEXT` 保存字符串，通过 `JsonCodec` 统一序列化/反序列化。
- Provider SPI 位于 `com.offerflow.copilot.provider`，核心类型包括 `AiProviderClient`、`ProviderRequest`、`ProviderResponse`、`ProviderDescriptor`、`ProviderHealth`、`ProviderRouter` 和 `ProviderExecutionService`。
- `AiProviderProperties` 默认 `mode=local-rule`、`realCallEnabled=false`、`rawResponseSave=false`、`timeoutMs=8000`。API Key 通过环境变量占位读取，不写入仓库配置，不在响应中明文展示。
- OpenAI-compatible 与 DeepSeek adapter 是 `NoOpOpenAiCompatibleProviderClient` / `NoOpDeepSeekProviderClient`，继续服务于 sandbox-run 的 no-op/fallback 路径。
- `POST /api/provider/real-dry-run` 是 P4F 手动 dry-run 入口，只支持 `deepseek` 和 `openai-compatible`。它先走 Local Permission、PII Guard、Prompt Contract、Risk Policy、Provider Config Check，再决定是否尝试外部调用。
- Real dry-run 只有在 `realCallEnabled=true`、`allowExternalCall=true`、`confirmNoPii=true`、Provider base URL/API key/model 配置完整且 PII Guard 通过时才可能外呼；API Key 只从环境变量读取，不返回、不记录、不入库。
- Real dry-run 不保存 raw model response，返回 `rawResponseSaved=false`，输出必须通过 Schema Validate 和 Risk Guard，并且仍然 `humanReviewRequired=true`、`copyAllowed=false`。
- Real dry-run 每次写入 `provider_trace_run` 和 14 条 `trace_step`：Actor Permission Check、Real Call Flag Check、PII Guard、Prompt Contract Load、Risk Policy Load、Provider Config Check、Provider Select、External Provider Call/Blocked、Provider Response Normalize、Schema Validate、Risk Guard、Fallback Decision、Human Review Required、Copy Permission Blocked。
- Provider contracts 位于 `com.offerflow.copilot.provider.contract`，核心类型包括 `ProviderTaskType`、`PromptContract`、`PromptContractRegistry`、`RiskPolicy`、`RiskPolicyRegistry`、`ProviderResponseSchema`、`ProviderResponseSchemaRegistry`、`ProviderResponseValidator` 和 `ProviderValidatedResult`。
- `GET /api/provider/contracts` 返回 taskType 合同摘要；`GET /api/provider/contracts/{taskType}` 返回 PromptContract detail；`POST /api/provider/validate-response` 只验证本地模拟 ProviderResponse，不发网络、不保存 raw model response。
- Copy Permission Contract 位于 `com.offerflow.copilot.copy`，核心类型包括 `CopyTargetType`、`CopyPermissionRequest`、`CopyPermissionResult`、`CopyPermissionPolicy` 和 `CopyPermissionService`。
- `POST /api/copy-permissions/check` 是统一复制门禁入口；`GET /api/copy-permissions/audit-events` 按 targetType/targetId 返回统一审计历史。该表不保存 requested text 或真实隐私。
- Local Permission 位于 `com.offerflow.copilot.security.local`，核心类型包括 `LocalActorContext`、`LocalActorRole`、`PermissionAction`、`PermissionDecision`、`LocalActorResolver`、`LocalPermissionPolicy` 和 `LocalPermissionAuditService`。
- 本地角色为 `OWNER`、`REVIEWER`、`EDITOR`、`VIEWER`、`SYSTEM`。它们只用于 demo/local-rule 审计，不是生产登录、注册、认证或授权。`OWNER` / `EDITOR` 可执行 `PROVIDER_REAL_DRY_RUN`；`VIEWER` 禁止；`SYSTEM` 禁止作为人工 real dry-run actor。
- `POST /api/permissions/check` 会返回 `PermissionDecision` 并写入 `permission_audit_event`；`GET /api/permissions/audit-events` 支持 actor/action/target/allowed 查询；`GET /api/permissions/current-actor` 返回本地 actor、role 和允许动作。
- Human Review、Evidence、JD Intake、Match Report、Copy Permission 和 Provider Sandbox 的关键写接口都会先写 permission audit；denied 返回 403 和清晰 reason，allowed 后继续写原业务审计表。
- `POST /api/provider/sandbox-run` 会先加载 PromptContract 和 RiskPolicy，再执行 local-rule/no-op provider，随后通过 ProviderResponseValidator。Sandbox run 支持 `simulateFailure`、`simulateTimeout`，外部 provider 未配置或失败时必须 fallback 到 `local-rule`。
- 每次 sandbox run 都写入 `provider_trace_run` 和 12 条 `trace_step`：Provider Config Check、Prompt Contract Load、Risk Policy Load、Prompt Build、Provider Select、Provider No-op/Call、Fallback Decision、Provider Response Validate、Schema Contract Validate、Risk Policy Guard、Contract Violation Check、Human Review Required。
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
- `POST /api/match-reports/{versionId}/copy-check` 会按版本状态返回复制许可，内部复用 `CopyPermissionService`，同时写入 `copy_permission_audit_event` 和旧 `COPY_ENABLED` / `COPY_BLOCKED` audit event。
- copy-check audit event 额外保存 `copy_allowed`、`copy_reason`、`version_status`、`human_review_status` 和 `boundary_notice`，供前端展开查看历史。
- 三处 Audit Trail 共用 `AuditEventDisclosure` 组件；Match Report 与 Interview Prep 复用 `CopyPermissionPanel` 展示 allowed、reason、target status、Human Review、Schema Validate、Risk Guard 和统一复制审计历史。
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
- Provider 状态必须如实展示，不能把 fallback 描述为真实 LLM。页面、日志和接口只能展示 API Key `masked` / `not configured` / `disabled`。
- 不要在 README、文档或提交信息中写“真实 DeepSeek / OpenAI-compatible 已调用成功”，除非另一次人工验收确实用脱敏输入完成真实 dry-run 且只记录脱敏摘要。
- 不保存 API Key 明文，不保存真实隐私，不接招聘平台 API，不爬虫。

## 验收范围

- 后端覆盖 COPY_ENABLED / COPY_BLOCKED 结构化详情、Archived send-to-review block、Archived copy block、restore to Draft 和审计详情字段。
- 前端覆盖 Match Report、Human Review、Evidence Library 的 1366x768、1440x900、1920x1080 截图与横向溢出检查。
- `mvn test`：当前包含 Provider SPI、Provider Contract hardening、Copy Permission Contract、Local Permission Workflow 与 Real Provider Dry-run 测试；覆盖 Flyway history、关键表存在性、Provider SPI 默认值、fallback、trace 写入、key masking、contract registry、response validation、风险策略、复制门禁、本地角色 allowed/blocked、permission audit、PII blocked、external failure fallback、rawResponseSaved=false 和测试不依赖真实网络/API Key/Docker MySQL。
- `npm run build`：Vue TypeScript 与 Vite production build 通过。
- `npm run screenshots`：18 tests 通过。
- MySQL smoke：MySQL 8 上 V1+V2 migration 成功，16 张业务表存在；连续两次后端启动的 seed 计数稳定。
