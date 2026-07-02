# Resume Evidence Audit

## 目标

简历证据库是 OfferFlow 的核心数据资产。P3C 将它从只读 seed demo 页面升级为可编辑、可确认、可回滚、可审计的证据管理工作流，用来表达一条原则：项目证据不是随便写的，而是需要维护、确认、追溯和必要时回滚。

## 状态模型

- `Draft`：草稿证据，可以继续编辑，尚未作为确认事实使用。
- `Confirmed`：已经人工确认，可以进入匹配报告、面试准备和人工复核引用链。
- `Returned`：证据需要补充或修正，当前不应作为确认事实使用。
- `Archived`：证据已归档，默认弱化展示，可通过恢复动作重新进入 Draft 或 Confirmed。

## 审计动作

`resume_evidence_audit_event` 记录以下动作：

- `CREATE_DRAFT`
- `UPDATE_DRAFT`
- `CONFIRM`
- `RETURN_TO_DRAFT`
- `ARCHIVE`
- `RESTORE`

每条事件记录 evidence id、动作、前后状态、操作者、操作者角色、修改字段、before/after snapshot、人工备注和时间。API 还返回可追踪的 Trace ID / Trace Hash。字段级 diff 当前保持轻量，使用 `changed_fields_json` 展示例如 `skills changed`、`evidenceSources changed`、`boundaryNote changed` 和 `strength changed`。

P3G 中单条 Evidence Audit Trail 可在卡片内展开。Archived 证据统一弱化显示并进入只读态：不能编辑、确认或退回，只允许 restore 回到 Draft 后继续维护。

P4E 后，Evidence 写接口会先经过 Local Permission Model。`EDITOR` 可创建、编辑和 return-to-draft；`OWNER` 可执行全部 evidence 写动作，包括 confirm、archive 和 restore；`VIEWER` 只读。被拒绝的动作返回 permission reason，并写入 `permission_audit_event`。

## API

- `GET /api/evidence/library`
- `GET /api/evidence/{id}`
- `GET /api/evidence/{id}/audit-events`
- `POST /api/evidence`
- `PUT /api/evidence/{id}`
- `POST /api/evidence/{id}/confirm`
- `POST /api/evidence/{id}/return-to-draft`
- `POST /api/evidence/{id}/archive`
- `POST /api/evidence/{id}/restore`

写接口支持 `actor`、`actorRole`、`humanNote` 和证据 payload。当前 actor 是 demo local actor，用于演示审计链路和本地角色权限，不是生产鉴权或生产级权限系统。

P4E 权限动作映射：

- `POST /api/evidence` -> `EVIDENCE_CREATE`
- `PUT /api/evidence/{id}` -> `EVIDENCE_UPDATE`
- `POST /api/evidence/{id}/return-to-draft` -> `EVIDENCE_UPDATE`
- `POST /api/evidence/{id}/confirm` -> `EVIDENCE_CONFIRM`
- `POST /api/evidence/{id}/archive` -> `EVIDENCE_ARCHIVE`
- `POST /api/evidence/{id}/restore` -> `EVIDENCE_RESTORE`

## 当前边界

当前仍然使用 H2 demo persistence 和脱敏 seed demo data，不接真实 LLM，不调用 DeepSeek，不调用中转站，不接招聘平台 API，不爬虫，不保存 API Key，不保存真实隐私。

证据内容不虚构真实客户、真实用户、真实流量或生产级数据，也不输出 Offer 概率、录取概率或保证通过。

Local Permission Model 只是 P4E 本地演示规则，不是生产 RBAC、登录系统或合规审计系统。`resume_evidence_audit_event` 继续记录业务状态变化；`permission_audit_event` 只记录本地权限决策。
