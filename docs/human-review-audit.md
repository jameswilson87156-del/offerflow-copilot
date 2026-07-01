# Human Review Audit Trail

## 定位

Human Review 是 OfferFlow 的核心安全链路。系统中的 AI 或 local-rule 输出默认只是 `Draft`，不能直接复制、投递或对外使用。用户需要先确认事实、证据来源和表达边界，再将内容标记为 `Confirmed`。

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
- 创建时间。

`GET /api/reviews/{id}` 会返回包含 `auditTrail` 的 review detail；`GET /api/reviews/{id}/audit-events` 可以单独读取审计历史。

## Match Report Handoff

P3E 中，`POST /api/jobs/{id}/match-reports/generate` 会在写入 `match_report_version` 后创建或关联一个 Human Review item：

- `review_type = MATCH_REPORT`
- `source_page = Match Report`
- `trace_id = match_report_version.trace_id`
- `original_text = 匹配摘要与推荐行动`
- `evidence_refs_json = 当前 JD evidence bindings 摘要`
- `status = Draft`

`POST /api/match-reports/{versionId}/send-to-review` 会将报告版本状态更新为 `IN_REVIEW`，并把关联 review item 显示为 `In Review`。归档报告版本会把关联 review item 显示为 `Archived`。这些 handoff 动作同时写入 `match_report_audit_event`；Human Review 自身的 confirm/return/flag-risk 仍由 `human_review_audit_event` 记录。

## 当前边界

- 当前 actor 是 demo user，例如 `demo-reviewer`，不是生产鉴权主体。
- 当前不是生产级权限系统，也不包含审批角色、租户隔离、不可篡改日志或合规归档。
- 当前数据是脱敏 seed demo，不是真实招聘数据或真实用户数据。
- 不保存真实隐私，不保存 API Key，不接招聘平台，不爬虫。
- 不接真实 LLM、不调用 DeepSeek、不调用中转站。
- 不输出 Offer 概率、录取概率或保证通过。

## 后续扩展方向

P3C 已将同样的审计模式扩展到证据库编辑；P3D 已扩展到 JD Intake、parse version 和 evidence binding；P3E 已将 Match Report 版本生成纳入 Human Review。生产化前还需要真实鉴权、权限模型、租户隔离和审计日志防篡改策略。
