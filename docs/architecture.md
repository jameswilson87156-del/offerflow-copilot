# 架构说明

## P4D 结构

```text
Vue 3 Workbench
  |-- Vue Router（7 个核心页面）
  |-- API client（api.ts）
  |-- typed demo fallback（后端不可用时明确标注）
  |-- domain views（JD、证据库、匹配报告、面试准备、投递跟踪、人工复核、Provider Trace）
          |
          v
Spring Boot 3
  |-- Flyway（应用数据访问前验证并迁移 schema）
  |-- Controllers（保持前端响应结构兼容）
  |-- Services（组合 local-rule 语义、数据库读取和状态流转）
  |-- Provider SPI（local-rule/no-op adapters、router、sandbox execution）
  |-- Provider Contracts（taskType prompt/schema/risk policy、response validator）
  |-- Copy Permission Contract（统一复制门禁、Human Review gate、audit event）
  |-- Repositories（MyBatis-Plus BaseMapper 封装）
  |-- JsonCodec（TEXT JSON 字段统一编解码）
  |-- PersistenceSeedService（空库 seed 脱敏 demo）
          |
          v
H2 demo/test 或本地 MySQL 8 persistence
  |-- resume_evidence
  |-- resume_evidence_audit_event
  |-- job_post
  |-- jd_parse_version
  |-- jd_evidence_binding
  |-- jd_audit_event
  |-- match_report
  |-- match_report_version
  |-- match_report_audit_event
  |-- copy_permission_audit_event
  |-- interview_prep
  |-- application_record
  |-- human_review_item
  |-- human_review_audit_event
  |-- provider_trace_run
  |-- trace_step
```

## Provider SPI、Contract 与 Sandbox 链路

P4B 新增 `com.offerflow.copilot.provider` 包，为后续 OpenAI-compatible、DeepSeek 或中转站接入预留稳定边界；P4C 新增 `com.offerflow.copilot.provider.contract`，把 taskType 到 promptVersion、schemaVersion、riskPolicyVersion 的映射固化下来。当前仍不发起真实外部请求。

核心结构：

1. `AiProviderClient`：统一 `analyze`、`health`、`descriptor`。
2. `LocalRuleProviderClient`：默认 deterministic provider，不访问网络。
3. `NoOpOpenAiCompatibleProviderClient`：读取配置并返回 fallback-required，不访问网络。
4. `NoOpDeepSeekProviderClient`：读取配置并返回 fallback-required，不访问网络。
5. `ProviderRouter`：根据 provider mode、配置状态、simulate failure/timeout 决定是否 fallback 到 local-rule。
6. `ProviderExecutionService`：创建 runId/traceId，加载 PromptContract/RiskPolicy，执行 sandbox run，验证 ProviderResponse，写入 `provider_trace_run` 与 `trace_step`。
7. `PromptContractRegistry`：为 JD_ANALYSIS、EVIDENCE_BINDING、MATCH_REPORT、INTERVIEW_PREP、OPENING_MESSAGE、HUMAN_REVIEW_REWRITE、PROVIDER_SANDBOX 提供合同。
8. `ProviderResponseValidator`：校验 required fields、schemaVersion、禁用表述、rawResponseSaved=false 和 realCall disabled 边界。

配置默认值为 `offerflow.ai.provider.mode=local-rule`、`real-call-enabled=false`、`raw-response-save=false`。API Key 只通过环境变量占位读取，接口和页面只返回 `masked` / `not configured` / `disabled`，不会返回明文。

`GET /api/provider/contracts` 与 `GET /api/provider/contracts/{taskType}` 只返回合同摘要/detail，不包含 API Key。`POST /api/provider/validate-response` 只验证本地模拟 ProviderResponse，不发网络、不保存 raw model response。

`POST /api/provider/sandbox-run` 每次至少写入 12 个 Trace Evidence 步骤：Provider Config Check、Prompt Contract Load、Risk Policy Load、Prompt Build、Provider Select、Provider No-op/Call、Fallback Decision、Provider Response Validate、Schema Contract Validate、Risk Policy Guard、Contract Violation Check、Human Review Required。外部 provider 未配置、模拟失败或模拟超时时，`ProviderResponse` 会标记 `fallbackUsed=true`、`finalProvider=local-rule` 并记录 fallback reason。所有输出仍是 Draft，需要 Human Review。

## Copy Permission Contract 链路

P4D 新增 `com.offerflow.copilot.copy`，把“能否复制 AI/local-rule 输出”从页面局部判断抽象为统一门禁。核心结构：

1. `CopyTargetType`：覆盖 `MATCH_REPORT`、`INTERVIEW_PREP`、`OPENING_MESSAGE`、`HUMAN_REVIEW_REWRITE`、`JD_ANALYSIS_SUMMARY`、`EVIDENCE_BINDING_SUMMARY` 和 `PROVIDER_SANDBOX_OUTPUT`。
2. `CopyPermissionPolicy`：只有 target status = `CONFIRMED`、Human Review = `Confirmed`、`schemaValidated=true`、`riskGuardPassed=true` 时返回 `allowed=true`。
3. `CopyPermissionService`：解析目标状态、调用 policy、写入 `copy_permission_audit_event`，并返回 `CopyPermissionResult`。
4. `CopyPermissionController`：提供 `POST /api/copy-permissions/check` 和 `GET /api/copy-permissions/audit-events`。

`POST /api/match-reports/{versionId}/copy-check` 保持旧响应兼容，但内部复用 `CopyPermissionService`；因此每次旧 copy-check 会同时写旧 `match_report_audit_event` 和新 `copy_permission_audit_event`。`/interview-prep` 当前基于 `interview_prep.review_status` 做 demo gate，默认 Draft 被拦截。`copy_permission_audit_event` 不保存 requested text 或真实隐私。

## JD Intake 审计链路

OfferFlow 不从招聘平台抓取 JD，也不接 Boss、牛客、实习僧等平台 API。P3D 中，JD 只来自用户手动粘贴或脱敏 seed demo。`JobIntakeService` 在同一个事务内完成：

1. 创建或更新 `job_post`。
2. 使用 deterministic local-rule parser 生成 `jd_parse_version`。
3. 基于当前 parse version 和 `resume_evidence` 做关键词匹配，生成 `jd_evidence_binding`。
4. 为 CREATE_JD、UPDATE_JD、PARSE_LOCAL_RULE、BIND_EVIDENCE、REBIND_EVIDENCE 写入 `jd_audit_event`。
5. 组合返回 JD detail，包含当前解析版本、版本历史、证据绑定和 audit trail。

当前解析不是 LLM 推理，不调用真实 Provider，不保存 API Key。解析文本会做基础脱敏，例如邮箱和手机号会替换为 redacted 标记。

## Match Report Versioning & Review Sync 链路

匹配报告在 P3E 中从一次性 demo 结果升级为可版本化输出资产，P3F 又把 Human Review 操作接回 `match_report_version` 状态机。`MatchReportVersionService` 在生成报告时完成：

1. 读取目标 `job_post`、当前最新 `jd_parse_version` 和该版本下的 `jd_evidence_binding`。
2. 使用 deterministic local-rule scoring 生成 summary、score breakdown、evidence refs、skill gaps、recommended actions 和 risk notes。
3. 写入 `match_report_version`，默认状态为 `DRAFT`。
4. 写入 `match_report_audit_event`，记录 `GENERATE_LOCAL_RULE` 与 `CREATE_DRAFT`。
5. 创建或更新 `MATCH_REPORT` 类型的 `human_review_item`，让报告进入人工复核队列。

`GET /api/match-report/demo` 继续返回旧页面需要的 summary、score、evidenceSources、skillGaps、recommendedActions 和 traceEvidence 字段，但来源改为最新 report version。版本列表、详情、审计、送审、归档、恢复和复制许可接口围绕 `match_report_version` 工作。

P3F 中，`HumanReviewService` 对 `review_type = MATCH_REPORT` 的 item 执行 confirm / return / flag-risk 后，会调用 `MatchReportVersionService.syncFromHumanReview`：

1. 通过 `human_review_id` 找到关联的 `match_report_version`。
2. 将 Human Review Confirmed / Returned / Risk Flagged 映射为 `CONFIRMED` / `RETURNED` / `RISK_FLAGGED`。
3. 写入 `match_report_audit_event`，action 为 `HUMAN_REVIEW_CONFIRMED`、`HUMAN_REVIEW_RETURNED` 或 `HUMAN_REVIEW_FLAGGED_RISK`。
4. 保持 `ARCHIVED` 版本只读；归档版本不能 send-to-review，只能 restore 到 `DRAFT` 后重新进入复核链路。

复制许可由 Copy Permission Contract 统一判断。只有 `CONFIRMED` + Human Review Confirmed + schema validated + risk guard passed 才返回 `allowed=true`；其它状态都会返回原因、Human Review 状态和 Boundary Notice，并写入统一 copy permission audit event。旧 Match Report copy-check 继续写 `COPY_ENABLED` / `COPY_BLOCKED` 以兼容页面历史。

当前 scoring 不调用真实 LLM、DeepSeek 或中转站，不输出 Offer 概率或录取概率。报告建议只有进入 Human Review 并经人工确认后，才可作为可复制建议使用。

## Resume Evidence 审计链路

简历证据库是 OfferFlow 的核心数据资产。P3C 中，证据不再只是只读 seed demo，而是可维护、可确认、可回滚、可审计的工作流。`EvidenceLibraryService` 在同一个事务内完成：

1. 读取或创建当前 `resume_evidence`。
2. 记录 previous status。
3. 计算 changed fields，并生成 before/after snapshot。
4. 根据动作更新 Draft、Confirmed、Returned、Archived 等状态。
5. 写入 `resume_evidence_audit_event`。
6. 返回包含 `auditTrail` 的 evidence detail。

支持的动作包括 `CREATE_DRAFT`、`UPDATE_DRAFT`、`CONFIRM`、`RETURN_TO_DRAFT`、`ARCHIVE` 和 `RESTORE`。当前 actor 是 demo user，用于展示状态流转，不是生产鉴权或生产级权限系统。

## Human Review 审计链路

人工复核是 OfferFlow 的核心安全链路。AI 或规则生成内容默认是 `Draft`，复制或投递前必须进入人工复核。P3B 中，`HumanReviewService` 在同一个事务内完成：

1. 读取当前 `human_review_item`。
2. 记录 previous status / risk level。
3. 根据动作更新当前状态和风险等级。
4. 写入 `human_review_audit_event`。
5. 返回包含 `auditTrail` 的 review detail。

支持的动作包括 `CONFIRM`、`RETURN`、`FLAG_RISK`、`ADD_NOTE`、`AUTO_RISK_GUARD`。当前接口已覆盖 confirm、return 和 flag-risk；seed 数据中包含自动风险守卫和历史人工复核示例。

## 设计取舍

- H2 继续作为默认 demo/test 数据库，避免默认启动依赖 Docker 或真实外部服务。
- Flyway 是 H2/MySQL 的统一 schema source of truth；初始化顺序为 Flyway migration -> MyBatis-Plus repository 可用 -> `PersistenceSeedService` 空表 seed。
- `schema.sql` 不再自动执行，只保留作历史 fallback 参考，避免两套初始化同时建表。
- MySQL 8 profile 与 Docker Compose 仅用于本地开发/演示；不代表生产部署、备份、权限或密钥管理已经完成。
- 响应层继续使用 Java record，数据库 entity 与 API DTO 分离，便于后续审计、权限和状态机扩展。
- JSON 字段暂存为 `TEXT`，由 `JsonCodec` 管理；审计事件先用结构化列保存关键字段，便于后续查询。
- Provider 设置已接入 Provider SPI、contract registry、response validation 与 sandbox execution；Copy Permission Contract 已接入 Match Report 与 Interview Prep。Dashboard 仍为组合 service。核心 JD Intake、证据、复核、Trace、报告版本、面试准备和投递跟踪已优先读库。

## 边界

本架构不接真实 LLM，不调用 DeepSeek，不保存 API Key，不接招聘平台 API，不爬虫，不自动投递，不保存真实隐私，也不输出 Offer/录取概率或保证通过。当前 actor 是 demo user，不是生产鉴权或生产级权限系统。
