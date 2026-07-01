# Persistence Foundation

## 当前状态

P3C 使用 H2 in-memory 数据库作为本地 demo/test persistence。应用启动时执行 `schema.sql` 建表，并由 `PersistenceSeedService` 在空表中插入脱敏 seed demo 数据。重复调用 seed 不会重复插入已有数据。

当前数据仍然是 `mock/local-rule` 演示数据，不是真实招聘数据，也不代表真实 Provider 能力。

## 数据表

- `resume_evidence`：匿名化项目证据、技能、来源链和边界说明。
- `resume_evidence_audit_event`：简历证据创建、编辑、确认、退回、归档和恢复的审计事件，记录 changed fields 与 before/after snapshot。
- `job_post`：手动录入的脱敏岗位描述。
- `match_report`：匹配报告摘要、评分拆解、证据来源、技能差距和 Trace。
- `interview_prep`：面试前准备重点、问题分组、STAR 草稿、风险提醒和复盘 Timeline。
- `application_record`：手动投递记录与沟通 Timeline。
- `human_review_item`：人工复核队列、风险词、证据引用、人工备注和当前状态。
- `human_review_audit_event`：人工复核动作审计事件，记录状态流转、风险等级变化、备注、Trace ID 和 Trace Hash。
- `provider_trace_run`：分析运行的 Provider 模式、fallback、schema、risk flags 和 trace hash。
- `trace_step`：Run Pipeline 每一步的输入摘要、输出摘要、耗时和证据引用。

## 已读库或写库接口

- `GET /api/evidence/library`
- `GET /api/evidence/coverage`
- `GET /api/evidence/{id}`
- `GET /api/evidence/{id}/audit-events`
- `POST /api/evidence`
- `PUT /api/evidence/{id}`
- `POST /api/evidence/{id}/confirm`
- `POST /api/evidence/{id}/return-to-draft`
- `POST /api/evidence/{id}/archive`
- `POST /api/evidence/{id}/restore`
- `GET /api/reviews`
- `GET /api/reviews/{id}`
- `GET /api/reviews/{id}/audit-events`
- `POST /api/reviews/{id}/confirm`
- `POST /api/reviews/{id}/return`
- `POST /api/reviews/{id}/flag-risk`
- `GET /api/provider/traces`
- `GET /api/provider/traces/{runId}`
- `GET /api/match-report/demo`
- `GET /api/interview-prep/demo`
- `GET /api/applications`

## 简历证据审计事件字段

`resume_evidence_audit_event` 当前保存：

- `evidence_id`
- `action`
- `previous_status`
- `next_status`
- `actor`
- `actor_role`
- `changed_fields_json`
- `before_snapshot_json`
- `after_snapshot_json`
- `human_note`
- `created_at`

当前 actor 默认是 `demo-evidence-editor`，用于演示证据维护历史，不是生产鉴权主体。

## 人工复核审计事件字段

`human_review_audit_event` 当前保存：

- `review_id`
- `action`
- `previous_status`
- `next_status`
- `previous_risk_level`
- `next_risk_level`
- `actor`
- `actor_role`
- `human_note`
- `trace_id`
- `trace_hash`
- `created_at`

当前 actor 默认是 `demo-reviewer`，用于演示状态流转历史，不是生产鉴权主体。

## MySQL Profile

`application-mysql.yml` 保留 MySQL datasource 配置，供后续切换使用。本轮不要求连接真实 MySQL，也不提供真实账号、密码或 API Key。

示例：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

使用 MySQL 前需要先提供独立 migration、备份、脱敏、删除策略和权限模型。

## JSON 字段

当前复杂结构先以 `TEXT` JSON 字符串保存，例如 skills、evidence sources、STAR 草稿、risk flags 和 timeline。后端通过 `JsonCodec` 统一序列化/反序列化，避免各 service 手写 JSON。

审计事件表中的核心流转字段已拆为结构化列，方便后续查询、过滤和审计页面扩展。

## 安全边界

- 不保存真实隐私。
- 不保存 API Key 明文。
- 不接招聘平台 API。
- 不爬虫。
- 不自动投递。
- 不做实时面试辅助或作弊。
- 不输出 Offer 概率、录取概率或保证通过。
- 不虚构真实用户、客户、流量或生产级能力。
