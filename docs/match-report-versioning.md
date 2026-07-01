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

报告版本默认 `DRAFT`。送入人工复核后版本状态为 `IN_REVIEW`，归档后为 `ARCHIVED`。

## Human Review 规则

报告生成后会自动进入 Human Review 队列。进入 Human Review 前后都不代表内容可直接外发；只有人工确认事实、证据来源和措辞边界后，相关建议才可作为可复制建议使用。

当前页面会展示：

- JD parse version
- Evidence binding count
- Provider mode: `local-rule`
- Trace ID
- 版本历史
- 审计历史
- 风险提醒

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
- 不承诺 Offer 结果。

## 后续 P3F 建议

P3F 可以把 Match Report 的 `CONFIRMED` / `RETURNED` 状态与 Human Review 的 confirm / return 操作做双向同步，并补充更明确的版本恢复、复制许可和只读归档策略。
