# Structured JD Intake

P3D 将 JD 分析台从只读 demo-analysis 升级为结构化 JD Intake 工作流。OfferFlow 只支持用户手动粘贴岗位描述，或者读取脱敏 seed demo data；本阶段不接招聘平台 API，不爬取网页，不保存 HR 隐私或聊天记录。

## 工作流

1. 用户在 `/jd-analyzer` 手动填写岗位标题、公司、城市、来源备注和 JD 文本。
2. `POST /api/jobs` 将 JD 保存到 `job_post`，并写入 `jd_audit_event`。
3. `POST /api/jobs/{id}/parse` 使用 local-rule parser 生成新的 `jd_parse_version`。
4. `POST /api/jobs/{id}/bind-evidence` 基于当前 parse version 和 `resume_evidence` 做关键词匹配，生成 `jd_evidence_binding`。
5. `GET /api/jobs/{id}` 返回 JD detail，包含当前解析版本、版本历史、证据绑定和 audit trail。

## 当前解析方式

JD 解析当前是 deterministic local-rule，不是真实 LLM Provider 推理。规则会提取：

- 核心要求
- 加分要求
- 风险要求
- keywords
- risk terms
- sanitized text

基础脱敏会将邮箱和手机号替换为 redacted 标记。当前不会保存真实手机号、邮箱、身份证、聊天记录或招聘平台私信。

## 数据表

- `job_post`：手动粘贴 JD 的基础信息和脱敏文本。
- `jd_parse_version`：每次 local-rule 解析产生一个版本。
- `jd_evidence_binding`：JD requirement 与简历证据的绑定关系。
- `jd_audit_event`：JD 创建、更新、解析和绑定证据的审计记录。

## 边界

- 不接真实 LLM。
- 不调用 DeepSeek。
- 不调用中转站。
- 不保存 API Key。
- 不接 Boss、牛客、实习僧等招聘平台 API。
- 不爬取网页。
- 不保存 HR 隐私、真实联系方式或聊天记录。
- 不自动投递。
- 不输出 Offer 概率、录取概率或保证通过。
- 不宣称生产级招聘系统能力。

当前 actor 是 demo user，用于展示审计链路，不是生产鉴权或生产级权限系统。
