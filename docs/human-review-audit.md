# Human Review Audit Trail

## 定位

Human Review 是 OfferFlow 的核心安全链路。系统中的 AI 或 local-rule 输出默认只是 `Draft`，不能直接复制、投递或对外使用。用户需要先确认事实、证据来源和表达边界，再将内容标记为 `Confirmed`。

## 状态流转

当前兼容状态：

- `Draft`：默认草稿，需要人工确认。
- `Confirmed`：人工确认后可复制使用。
- `Returned`：退回修改，仍不可复制。
- `Risk Flagged`：标记风险，通常需要调整措辞或证据。

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

## 当前边界

- 当前 actor 是 demo user，例如 `demo-reviewer`，不是生产鉴权主体。
- 当前不是生产级权限系统，也不包含审批角色、租户隔离、不可篡改日志或合规归档。
- 当前数据是脱敏 seed demo，不是真实招聘数据或真实用户数据。
- 不保存真实隐私，不保存 API Key，不接招聘平台，不爬虫。
- 不接真实 LLM、不调用 DeepSeek、不调用中转站。
- 不输出 Offer 概率、录取概率或保证通过。

## 后续扩展方向

P3C 可将同样的审计模式扩展到证据库编辑：新增证据草稿、人工确认、回滚、字段级 diff 和修改历史。生产化前还需要真实鉴权、权限模型、租户隔离和审计日志防篡改策略。
