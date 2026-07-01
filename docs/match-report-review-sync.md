# Match Report Review Sync

## 目标

P3F 建立 `match_report_version` 与 `human_review_item` 的闭环。匹配报告送入人工复核后，Human Review 的 confirm、return、flag-risk 操作会同步更新关联版本状态，并写入 `match_report_audit_event`。

核心规则：匹配报告只有 `CONFIRMED` 后才允许复制使用。Human Review 是正式使用前的安全门。

## 状态同步

同步入口在 `HumanReviewService` 的状态变更事务内触发：

- Human Review `Confirmed` -> Match Report `CONFIRMED`
- Human Review `Returned` -> Match Report `RETURNED`
- Human Review `Risk Flagged` -> Match Report `RISK_FLAGGED`

同步通过 `human_review_id` 查找关联 `match_report_version`。如果 review item 不是 `MATCH_REPORT` 类型，则不会影响匹配报告版本。

## 审计事件

每次同步都会写入 `match_report_audit_event`：

- `HUMAN_REVIEW_CONFIRMED`
- `HUMAN_REVIEW_RETURNED`
- `HUMAN_REVIEW_FLAGGED_RISK`

事件记录 previous status、next status、actor、actor role、human note、trace id 和 changed fields。copy-check 和 restore 也会写入：

- `COPY_ENABLED`
- `COPY_BLOCKED`
- `RESTORE_VERSION`
- `ARCHIVE`

P3G 会在页面中以卡内展开方式显示单条事件详情。`COPY_ENABLED` / `COPY_BLOCKED` 还会结构化保存并展示是否允许复制、原因、version status、Human Review status 和 Boundary Notice。

## 复制许可

`POST /api/match-reports/{versionId}/copy-check` 返回：

- `allowed`
- `reason`
- `versionStatus`
- `humanReviewStatus`
- `boundaryNotice`

规则：

- `CONFIRMED`：`allowed=true`
- `DRAFT`：`allowed=false`，需要人工复核
- `IN_REVIEW`：`allowed=false`，正在复核
- `RETURNED`：`allowed=false`，已退回
- `RISK_FLAGGED`：`allowed=false`，存在风险
- `ARCHIVED`：`allowed=false`，已归档

前端“复制确认版摘要”按钮只在 `CONFIRMED` 时真正复制。其它状态会提示：当前报告尚未通过人工复核，不能复制为正式投递建议。

Confirmed 是唯一可复制状态。状态标签统一显示为草稿、复核中、已确认、已退回、风险标记、已归档。

## Restore 与归档

`ARCHIVED` 版本只读：

- 不能 send-to-review
- copy-check 不会允许复制
- 可以 restore 到 `DRAFT`

`RETURNED` 和 `RISK_FLAGGED` 版本也可以 restore 到 `DRAFT`，再重新送入 Human Review。restore 会写入 `RESTORE_VERSION` audit event。

## 当前边界

- 当前不是生产级权限系统。
- 当前 actor 是 demo user，例如 `demo-reviewer`。
- 当前数据是脱敏 seed demo data。
- 当前 scoring 是 local-rule，不是录取概率。
- 不接真实 LLM，不调用 DeepSeek，不调用中转站。
- 不保存 API Key，不接招聘平台 API，不爬虫。
- 不保存真实手机号、邮箱、身份证或聊天记录。
- 不自动投递。
- 不做实时面试辅助或作弊功能。
- 不承诺 Offer 结果。
