# Persistence Foundation

## 当前状态

P4A 使用 Flyway 统一管理 H2 与 MySQL schema。默认 demo 和 test profile 使用 H2 in-memory；本地开发也可显式启用 `mysql` profile。应用先执行 `db/migration/V1__init_offerflow_schema.sql`，再由 `PersistenceSeedService` 对空表插入脱敏 seed demo 数据。重复启动不会重复 migration，也不会重复插入已有 seed。

原 `src/main/resources/schema.sql` 保留为历史 fallback 参考，但 `spring.sql.init.mode=never`，默认、test 和 mysql profile 都不会再自动执行它，避免与 Flyway 重复建表。

当前数据仍然是 `local-rule` / no-op 演示数据，不是真实招聘数据，也不代表真实 Provider 能力。

## 数据表

- `resume_evidence`：匿名化项目证据、技能、来源链和边界说明。
- `resume_evidence_audit_event`：简历证据创建、编辑、确认、退回、归档和恢复的审计事件，记录 changed fields 与 before/after snapshot。
- `job_post`：手动录入的脱敏岗位描述。
- `jd_parse_version`：JD local-rule 解析版本，包含 parser/provider mode、schema version、requirements、keywords、risk terms 和 sanitized text。
- `jd_evidence_binding`：JD requirement 与简历证据的绑定关系，记录 evidence strength、binding reason、source 和 review status。
- `jd_audit_event`：JD 创建、更新、解析、绑定、归档和恢复的审计事件，记录 changed fields 与 before/after snapshot。
- `match_report`：匹配报告摘要、评分拆解、证据来源、技能差距和 Trace。
- `match_report_version`：可版本化匹配报告输出资产，绑定 JD parse version、score breakdown、evidence refs、Human Review item、status 和 trace id。
- `match_report_audit_event`：匹配报告版本生成、创建 Draft、送审、Human Review 同步、复制许可、恢复、归档等动作的审计事件。
- `interview_prep`：面试前准备重点、问题分组、STAR 草稿、风险提醒和复盘 Timeline。
- `application_record`：手动投递记录与沟通 Timeline。
- `human_review_item`：人工复核队列、风险词、证据引用、人工备注和当前状态。
- `human_review_audit_event`：人工复核动作审计事件，记录状态流转、风险等级变化、备注、Trace ID 和 Trace Hash。
- `provider_trace_run`：分析运行的 Provider 模式、fallback、schema、risk flags 和 trace hash。
- `trace_step`：Run Pipeline 每一步的输入摘要、输出摘要、耗时和证据引用。
- `copy_permission_audit_event`：统一复制门禁审计事件，记录 target、allowed、reason、target status、Human Review status、schema/risk gate、actor 和 boundary notice。
- `permission_audit_event`：P4E 本地权限决策审计事件，记录 actor、role、action、target、allowed、reason、request id 和 boundary notice。

## 已读库或写库接口

- `GET /api/evidence/library`
- `GET /api/evidence/coverage`
- `GET /api/jobs`
- `GET /api/jobs/{id}`
- `POST /api/jobs`
- `PUT /api/jobs/{id}`
- `POST /api/jobs/{id}/parse`
- `POST /api/jobs/{id}/bind-evidence`
- `GET /api/jobs/{id}/parse-versions`
- `GET /api/jobs/{id}/audit-events`
- `GET /api/jobs/{id}/evidence-bindings`
- `GET /api/jobs/demo-analysis`
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
- `GET /api/provider/status`
- `GET /api/provider/settings`
- `GET /api/provider/config-check`
- `POST /api/provider/sandbox-run`
- `POST /api/copy-permissions/check`
- `GET /api/copy-permissions/audit-events`
- `GET /api/permissions/current-actor`
- `POST /api/permissions/check`
- `GET /api/permissions/audit-events`
- `GET /api/match-report/demo`
- `POST /api/jobs/{id}/match-reports/generate`
- `GET /api/jobs/{id}/match-reports`
- `GET /api/match-reports/{versionId}`
- `GET /api/match-reports/{versionId}/audit-events`
- `POST /api/match-reports/{versionId}/send-to-review`
- `POST /api/match-reports/{versionId}/archive`
- `POST /api/match-reports/{versionId}/restore`
- `POST /api/match-reports/{versionId}/copy-check`
- `GET /api/interview-prep/demo`
- `GET /api/applications`

## JD Intake 字段

`jd_parse_version` 当前保存：

- `job_id`
- `version_no`
- `parser_mode`
- `provider_mode`
- `prompt_version`
- `schema_version`
- `extracted_requirements_json`
- `keywords_json`
- `risk_terms_json`
- `sanitized_text`
- `parse_status`
- `created_at`

`jd_evidence_binding` 当前保存：

- `job_id`
- `parse_version_id`
- `requirement_key`
- `requirement_label`
- `evidence_id`
- `evidence_strength`
- `binding_reason`
- `evidence_source`
- `review_status`
- `created_at`
- `updated_at`

`jd_audit_event` 当前保存：

- `job_id`
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

当前 actor 默认是 `demo-jd-editor`，用于演示 JD Intake 状态流转，不是生产鉴权主体。

## 匹配报告版本字段

`match_report_version` 当前保存：

- `report_id`
- `job_id`
- `parse_version_id`
- `version_no`
- `score`
- `skill_score`
- `evidence_score`
- `risk_score`
- `interview_score`
- `recommended_resume`
- `status`
- `summary_json`
- `score_breakdown_json`
- `evidence_refs_json`
- `skill_gaps_json`
- `recommended_actions_json`
- `risk_notes_json`
- `generated_by`
- `provider_mode`
- `prompt_version`
- `schema_version`
- `trace_id`
- `human_review_id`
- `created_at`
- `updated_at`

`match_report_audit_event` 当前保存：

- `report_version_id`
- `action`
- `previous_status`
- `next_status`
- `actor`
- `actor_role`
- `changed_fields_json`
- `human_note`
- `trace_id`
- `created_at`

当前 actor 默认是 `local-rule report generator` 或 `demo-reviewer`，用于演示报告版本状态流转，不是生产鉴权主体。

P3F 支持的 `match_report_version.status` 为：

- `DRAFT`
- `IN_REVIEW`
- `CONFIRMED`
- `RETURNED`
- `RISK_FLAGGED`
- `ARCHIVED`

P3F 新增或扩展的 `match_report_audit_event.action` 包括：

- `HUMAN_REVIEW_CONFIRMED`
- `HUMAN_REVIEW_RETURNED`
- `HUMAN_REVIEW_FLAGGED_RISK`
- `COPY_ENABLED`
- `COPY_BLOCKED`
- `RESTORE_VERSION`
- `ARCHIVE`

`changed_fields_json` 当前以字段名列表记录，例如 `["status","humanReviewStatus"]` 或 `["copyPermission"]`。当前不是不可篡改审计系统，生产化前还需要真实鉴权、权限模型、租户隔离和日志防篡改策略。

P4D 新增 `copy_permission_audit_event` 作为统一复制门禁审计表。它保存 target type/id、COPY_ALLOWED 或 COPY_BLOCKED、allowed、reason、target status、Human Review status、schemaValidated、riskGuardPassed、actor、traceId、providerRunId、boundaryNotice 和 createdAt；不保存 requested text、raw model response、真实隐私或 API Key。

P4E 新增 `permission_audit_event` 作为本地角色权限审计表。它保存 actor、actor role、permission action、target type/id、allowed、reason、boundaryNotice、requestId 和 createdAt；不保存真实登录凭据、密码、API Key、真实隐私或业务输出正文。

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

## Provider Trace 字段

P4B 的 `ProviderExecutionService` 会在每次 sandbox run 中写入 `provider_trace_run`：

- `run_id`
- `provider_mode`
- `final_provider`
- `model`
- `fallback_reason`
- `prompt_version`
- `schema_version`
- `risk_flags_json`
- `evidence_count`
- `human_review_status`
- `duration_ms`
- `trace_hash`
- `evidence_detail_json`
- `technical_tags_json`
- `created_at`

同一 run 会写入 8 条 `trace_step`，步骤包括 Provider Config Check、Prompt Build、Provider Select、Provider No-op/Call、Fallback Decision、Schema Validate、Risk Guard 和 Human Review Required。状态可为 `SUCCESS`、`FALLBACK`、`WARNING`、`BLOCKED` 或 `ERROR`；seed demo 中仍保留历史 lowercase status 以兼容旧截图数据。

当前 Provider Trace 不是生产审计系统，也不是模型网关调用日志。OpenAI-compatible 和 DeepSeek adapter 只记录 no-op / fallback，不保存原始模型响应，`rawResponseSaved` 固定为 false。

## MySQL Profile

`application-mysql.yml` 提供可运行的本地 MySQL datasource 配置。先用 `docker compose up -d mysql` 启动 MySQL 8，再启用 profile：

示例：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

默认本地 database/user 是 `offerflow`，默认密码是公开的开发演示密码；可通过 `OFFERFLOW_DB_URL`、`OFFERFLOW_DB_USERNAME`、`OFFERFLOW_DB_PASSWORD` 覆盖。该 profile 和 Docker Compose 不是生产部署声明，不包含生产级备份、密钥管理、权限模型、脱敏或删除策略。详见 [database-migration.md](database-migration.md) 与 [local-mysql.md](local-mysql.md)。

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
