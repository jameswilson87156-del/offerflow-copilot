# Human Review Audit Trail

## 定位

Human Review 是 OfferFlow 的核心安全链路。系统中的 AI 或 local-rule 输出默认只是 `Draft`，不能直接复制、投递或对外使用。用户需要先确认事实、证据来源和表达边界，再将内容标记为 `Confirmed`。

P4D 后，Human Review Confirmed 是复制的必要条件，但不是页面直接复制的旁路。正式复制还必须经过 Copy Permission Contract，确认 schema validate、risk guard、target status 和 Human Review 状态均通过，并写入 `copy_permission_audit_event`。

P4E 后，Human Review 的 confirm / return / flag-risk 还会先经过 Local Permission Model。只有本地 `REVIEWER` 或 `OWNER` 可以执行人工复核写操作；`EDITOR`、`VIEWER` 和 `SYSTEM` 会收到清晰的 denied reason，并写入 `permission_audit_event`。

## 状态流转

当前兼容状态：

- `Draft`：默认草稿，需要人工确认。
- `In Review`：已由上游页面送入人工复核。
- `Confirmed`：人工确认后可复制使用。
- `Returned`：退回修改，仍不可复制。
- `Risk Flagged`：标记风险，通常需要调整措辞或证据。
- `Archived`：来源版本已归档，不再作为当前可用建议。

当前动作：

- `CONFIRM`：确认可用。
- `RETURN`：退回修改。
- `FLAG_RISK`：标记风险并提升风险等级。
- `ADD_NOTE`：预留人工备注动作。
- `AUTO_RISK_GUARD`：seed/demo 中用于展示自动风险守卫历史。

## 审计记录

每次状态变更都会写入 `human_review_audit_event`，记录：

- 操作者和角色。
- 动作类型。
- 前后状态。
- 前后风险等级。
- 人工备注。
- Trace ID / Trace Hash。
- changed fields。
- 创建时间。

`GET /api/reviews/{id}` 会返回包含 `auditTrail` 的 review detail；`GET /api/reviews/{id}/audit-events` 可以单独读取审计历史。

P3G 前端支持点击单条事件在卡片内展开完整详情。Confirmed、Returned、Risk Flagged、Archived 属于已结束或只读状态，操作按钮会禁用并解释原因；状态标签统一显示中文语义。

P4E 会在业务审计之前写入 permission audit：allowed 的人工复核动作和 blocked 的越权尝试都会保留 actor、role、action、target、reason 和 boundary notice。业务审计仍保留原有 `human_review_audit_event`。

## Match Report Handoff & Sync

P3E 中，`POST /api/jobs/{id}/match-reports/generate` 会在写入 `match_report_version` 后创建或关联一个 Human Review item：

- `review_type = MATCH_REPORT`
- `source_page = Match Report`
- `trace_id = match_report_version.trace_id`
- `original_text = 匹配摘要与推荐行动`
- `evidence_refs_json = 当前 JD evidence bindings 摘要`
- `status = Draft`

`POST /api/match-reports/{versionId}/send-to-review` 会将报告版本状态更新为 `IN_REVIEW`，并把关联 review item 显示为 `In Review`。归档报告版本会把关联 review item 显示为 `Archived`。这些 handoff 动作同时写入 `match_report_audit_event`。

P3F 中，Human Review 自身的 confirm / return / flag-risk 仍写入 `human_review_audit_event`，并在 `review_type = MATCH_REPORT` 且存在关联 `match_report_version` 时额外同步写入 `match_report_audit_event`：

- Human Review `Confirmed` -> Match Report `CONFIRMED`，action = `HUMAN_REVIEW_CONFIRMED`
- Human Review `Returned` -> Match Report `RETURNED`，action = `HUMAN_REVIEW_RETURNED`
- Human Review `Risk Flagged` -> Match Report `RISK_FLAGGED`，action = `HUMAN_REVIEW_FLAGGED_RISK`

匹配报告只有 `CONFIRMED` 后才可能复制使用。`RETURNED`、`RISK_FLAGGED` 和 `ARCHIVED` 版本不可作为正式建议；`ARCHIVED` 版本只读，不能 send-to-review，可 restore 到 `DRAFT` 后重新处理。P4D 的旧 match-report copy-check 会复用 `CopyPermissionService`，同时写旧 `match_report_audit_event` 和新 `copy_permission_audit_event`。

## 当前边界

- 当前 actor 是 demo local actor，例如 `demo.reviewer`，不是生产鉴权主体。
- 当前 Local Permission Model 不是生产级权限系统，也不包含真实登录、审批角色、租户隔离、不可篡改日志或合规归档。
- 当前数据是脱敏 seed demo，不是真实招聘数据或真实用户数据。
- 不保存真实隐私，不保存 API Key，不接招聘平台，不爬虫。
- 不接真实 LLM、不调用 DeepSeek、不调用中转站。
- 不输出 Offer 概率、录取概率或保证通过。
- Schema Validate / Risk Guard 通过不代表可复制；只有 Human Review Confirmed 且 Copy Permission Contract 通过后才允许复制。

## 后续扩展方向

P3C 已将同样的审计模式扩展到证据库编辑；P3D 已扩展到 JD Intake、parse version 和 evidence binding；P3E 已将 Match Report 版本生成纳入 Human Review；P3F 已将 MATCH_REPORT 复核动作同步回 `match_report_version`；P4D 已将复制动作抽象为统一 Copy Permission Contract；P4E 已增加本地角色权限与 permission audit。生产化前还需要真实鉴权、租户隔离和审计日志防篡改策略。
