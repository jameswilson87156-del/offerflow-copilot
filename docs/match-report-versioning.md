# Match Report Versioning

## 定位

匹配报告是可版本化 AI 输出资产，不是一次性 mock 分数。每次生成报告都会形成一个 `match_report_version`，用于保留当时的 JD parse version、resume evidence bindings、评分拆解、证据引用、推荐行动、风险提醒、Trace ID 和 Human Review 状态。

当前 scoring 是 deterministic `local-rule`。它只解释 JD 与简历证据覆盖关系，不是 Offer 概率、录取概率或保证通过。

## 数据链路

生成报告时，后端读取：

- `job_post`
- 当前最新 `jd_parse_version`
- 当前 parse version 下的 `jd_evidence_binding`
- 绑定证据引用的 `resume_evidence`

随后写入：

- `match_report_version`
- `match_report_audit_event`
- `human_review_item`，其中 `review_type = MATCH_REPORT`

报告版本默认 `DRAFT`。送入人工复核后版本状态为 `IN_REVIEW`，归档后为 `ARCHIVED`。P3F 后，Human Review 的 confirm / return / flag-risk 会同步更新关联版本状态。

## 版本状态

- `DRAFT`：草稿，需要人工复核，copy-check 返回 `allowed=false`。
- `IN_REVIEW`：正在人工复核，copy-check 返回 `allowed=false`。
- `CONFIRMED`：已通过人工复核，copy-check 返回 `allowed=true`，前端允许复制确认版摘要。
- `RETURNED`：已退回，需要修改或 restore 后重新复核，copy-check 返回 `allowed=false`。
- `RISK_FLAGGED`：存在风险，不能作为正式建议，copy-check 返回 `allowed=false`。
- `ARCHIVED`：只读归档，不能 send-to-review，copy-check 返回 `allowed=false`，可 restore 到 `DRAFT`。

## Human Review 规则

报告生成后会自动进入 Human Review 队列。进入 Human Review 前后都不代表内容可直接外发；只有人工确认事实、证据来源和措辞边界后，相关建议才可作为可复制建议使用。

P3F 的状态同步映射：

- Human Review `Confirmed` -> Match Report `CONFIRMED`
- Human Review `Returned` -> Match Report `RETURNED`
- Human Review `Risk Flagged` -> Match Report `RISK_FLAGGED`

每次同步都会写入 `match_report_audit_event`，并记录 previous status、next status、actor、actor role、human note、trace id 和 changed fields。

当前页面会展示：

- JD parse version
- Evidence binding count
- Provider mode: `local-rule`
- Trace ID
- 版本历史
- 审计历史
- 风险提醒

页面顶部还会展示复制许可区：

- 当前版本状态
- Human Review 状态
- 是否允许复制
- 拒绝原因
- Boundary Notice
- 检查复制许可、复制确认版摘要、恢复版本、送入人工复核、归档版本按钮

## 当前边界

- 不接真实 LLM。
- 不调用 DeepSeek。
- 不调用中转站。
- 不保存 API Key。
- 不接招聘平台 API。
- 不爬虫。
- 不保存真实手机号、邮箱、身份证、聊天记录等隐私。
- 不自动投递。
- 不输出 Offer 概率、录取概率或保证通过。
- 当前不是生产级招聘系统。
- 当前不是生产级权限系统；actor 是 demo user。
- 当前 scoring 是 local-rule，不是录取概率。
- 不承诺 Offer 结果。

## 接口

- `POST /api/match-reports/{versionId}/send-to-review`
- `POST /api/match-reports/{versionId}/archive`
- `POST /api/match-reports/{versionId}/restore`
- `POST /api/match-reports/{versionId}/copy-check`
- `GET /api/match-reports/{versionId}/audit-events`

`copy-check` 只对 `CONFIRMED` 返回 `allowed=true`。其它状态会返回中文原因、版本状态、Human Review 状态和 Boundary Notice。
