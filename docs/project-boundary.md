# 项目边界

## 允许范围

- 使用虚构岗位与匿名化项目样例展示证据链工作流。
- 使用确定性的 `local-rule` 和 H2 seeded demo data 生成演示拆解、匹配报告、面试前准备和投递跟踪。
- 由用户主动粘贴、确认和修改非敏感内容。
- JD 只支持用户手动粘贴或脱敏 seed demo；解析和证据绑定使用 deterministic local-rule，并保留版本历史与审计记录。
- 匹配报告可以基于当前 JD parse version 和 evidence bindings 生成版本化 Draft，并自动进入 Human Review 队列。
- 生成内容默认进入 `Draft`，必须经过人工复核后才可复制使用。
- 简历证据可以先作为 `Draft` 维护，人工确认后进入 `Confirmed`，每次编辑和状态流转都保留审计历史。
- 手动记录投递状态与复盘，不自动对外执行动作。
- 持久化保存 demo 人工复核状态和审计事件，展示可追踪的状态流转历史。
- 持久化保存 demo 简历证据状态和审计事件，展示证据维护、确认、归档和恢复历史。
- Human Review 对 MATCH_REPORT 的 confirm、return、flag-risk 可以同步更新关联匹配报告版本状态，并保留匹配报告审计事件。
- Match Report、Human Review、Evidence Library 的审计事件允许在本地页面展开查看；copy-check 结果会作为审计历史保留。
- 允许使用 Flyway 管理 H2/MySQL 共用 schema，并用 Docker Compose 启动仅供本地开发/演示的 MySQL 8。
- 允许使用 Provider SPI 和 sandbox run 演示配置校验、no-op adapter、失败/超时模拟、fallback 和 Trace Evidence 写入。
- 允许使用 Provider contract sandbox 演示 PromptContract、RiskPolicy、ProviderResponseSchema 和本地 Response Validator。
- 允许使用 Copy Permission Contract 演示 AI/local-rule 输出正式使用前的最后一道复制门禁。

## 明确不做

- 不接真实 LLM，不调用 DeepSeek，不调用中转站。
- 不保存 API Key 明文，也不把 Provider 配置伪装为已稳定接入。
- 不因 `realCallEnabled=true` 配置项就发起真实外部请求；P4C/P4D adapter 仍然是 no-op。
- 不接 Boss、牛客、实习僧等招聘平台 API，不爬取网页。
- 不采集或保存真实手机号、邮箱、身份证、聊天记录等隐私。
- 不自动投递，不自动私信 HR，不抓取平台聊天。
- 不做实时面试辅助、隐蔽提示或作弊功能。
- 不计算 Offer/录取概率，不保证通过。
- 不宣称生产级招聘系统、真实客户、真实流量或商业数据。
- 不将规则 fallback 包装成真实 LLM 能力。
- 不允许未经过 Copy Permission Contract 的 AI/local-rule 输出被复制为正式建议。
- 不将本地 MySQL profile 或 Docker Compose 描述为生产部署；不在仓库保存 `.env`、真实数据库凭据、API Key 或数据库数据目录。

## 人工复核原则

AI 或规则生成的内容默认是 `Draft`。用户必须确认事实、措辞和证据来源后，才能将内容标记为 `Confirmed` 并复制使用。`Returned` 表示需要修改，`Risk Flagged` 表示命中了高风险表述或边界问题。

P3B 已将 Human Review 状态和 audit trail 保存在 H2 demo persistence 中。每次 `confirm`、`return`、`flag-risk` 都会记录操作者、角色、动作、前后状态、前后风险等级、人工备注、Trace ID、Trace Hash 和时间。

当前 actor 是 demo user，用于演示审计链路，不是生产鉴权、生产权限系统或合规审计系统。

Confirmed 是唯一允许复制正式建议的状态。Schema Validate 通过不代表可以直接使用；Risk Guard 通过不代表可以直接复制；Human Review Confirmed 后仍由 Copy Permission Contract 做最后检查。Archived 是只读归档状态；前端禁用不合法动作并展示原因，但这不等同于生产级服务端授权模型。

## 简历证据原则

简历证据不是随便写入的宣传素材。证据默认可以是 `Draft`，经过人工确认后才进入 `Confirmed`；当证据需要补充、风险边界不清或不应继续使用时，可以退回 Draft、归档或恢复。每次创建、编辑、确认、退回、归档和恢复都会写入 `resume_evidence_audit_event`。

当前证据库仍使用脱敏 seed demo data，不保存真实手机号、邮箱、身份证、聊天记录等隐私，不虚构真实客户、真实用户、真实流量或生产级数据。

## JD Intake 原则

JD 分析台只接受用户手动粘贴的岗位描述或脱敏 seed demo，不接招聘平台 API，不爬取网页，不抓取 HR 聊天记录。每次 JD 创建、更新、local-rule 解析和证据绑定都会写入 `jd_audit_event`，每次解析都会生成新的 `jd_parse_version`，每次绑定都会生成或刷新 `jd_evidence_binding`。

当前 JD 解析是 local-rule parsing，不是真实 LLM Provider 推理；证据绑定是关键词匹配和脱敏 demo 证据组合，不代表生产级招聘系统能力。

## Match Report 原则

匹配报告是可版本化 AI 输出资产，不是一次性 mock 分数。每个 `match_report_version` 必须绑定一个 JD parse version 和一组 resume evidence bindings，并记录 provider mode、prompt/schema version、Trace ID、状态和审计事件。

当前匹配报告 scoring 是 local-rule，不是真实 LLM 推理，不是 Offer 概率、录取概率或保证通过。报告默认 `DRAFT`，生成后进入 Human Review；只有 `CONFIRMED` 版本才可以复制确认版摘要。`RETURNED`、`RISK_FLAGGED` 和 `ARCHIVED` 版本不可作为正式建议使用。

`ARCHIVED` 版本是只读版本，不能再次 send-to-review；如需继续处理，必须先 restore 到 `DRAFT` 并重新进入复核链路。当前 restore、copy-check 和审计 actor 都是 demo user，不是生产级权限系统。

每次 Match Report copy-check 都复用 `CopyPermissionService`，写入统一 `copy_permission_audit_event`，同时继续写旧 `COPY_ENABLED` 或 `COPY_BLOCKED`，并保存许可结果、原因、版本状态、Human Review 状态和 Boundary Notice，供 Audit Trail 展开查看。

## Copy Permission 原则

Copy Permission Contract 是 AI/local-rule 输出正式使用前的最后一道门禁。它要求：

- `schemaValidated=true`
- `riskGuardPassed=true`
- target status = `CONFIRMED`
- Human Review status = `Confirmed`

`DRAFT`、`IN_REVIEW`、`RETURNED`、`RISK_FLAGGED` 和 `ARCHIVED` 一律不能复制为正式建议。当前支持 Match Report 和 Interview Prep 的页面展示；Opening Message、Human Review Rewrite 等 target type 已在 policy 类型中预留，但仍是 demo/mock 支持，不强行扩大业务表。

## Provider SPI 原则

Provider SPI 只建立抽象和审计基础，不代表已经接入真实模型网关。默认 provider 是 `local-rule`；OpenAI-compatible 和 DeepSeek 当前是 no-op adapter，用于读取配置状态、展示 descriptor、模拟失败/超时并触发 fallback。

`GET /api/provider/config-check` 和 `GET /api/provider/settings` 不返回 API Key 明文，只显示 `masked`、`not configured` 或 `disabled`。`POST /api/provider/sandbox-run` 不发起真实外部 Provider 调用，任何未配置、失败或超时都必须 fallback 到 local-rule，并写入 `provider_trace_run` 与 `trace_step`。

`GET /api/provider/contracts`、`GET /api/provider/contracts/{taskType}` 和 `POST /api/provider/validate-response` 只用于本地 contract/validation 演示。真实 Provider 接入前，任何输出都必须通过 schema validate、risk guard、Human Review 和 Copy Permission Contract；未校验或未确认输出不得进入页面复制流程。

所有 Provider 输出仍需 Human Review；模型失败不能伪装成成功，fallback reason 必须保留在响应和 Trace Evidence 中。
