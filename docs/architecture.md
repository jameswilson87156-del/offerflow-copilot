# 架构说明

## P3C 结构

```text
Vue 3 Workbench
  |-- Vue Router（7 个核心页面）
  |-- API client（api.ts）
  |-- typed demo fallback（后端不可用时明确标注）
  |-- domain views（JD、证据库、匹配报告、面试准备、投递跟踪、人工复核、Provider Trace）
          |
          v
Spring Boot 3
  |-- Controllers（保持前端响应结构兼容）
  |-- Services（组合 local-rule 语义、数据库读取和状态流转）
  |-- Repositories（MyBatis-Plus BaseMapper 封装）
  |-- JsonCodec（TEXT JSON 字段统一编解码）
  |-- PersistenceSeedService（空库 seed 脱敏 demo）
          |
          v
H2 demo persistence
  |-- resume_evidence
  |-- resume_evidence_audit_event
  |-- job_post
  |-- match_report
  |-- interview_prep
  |-- application_record
  |-- human_review_item
  |-- human_review_audit_event
  |-- provider_trace_run
  |-- trace_step
```

## Resume Evidence 审计链路

简历证据库是 OfferFlow 的核心数据资产。P3C 中，证据不再只是只读 seed demo，而是可维护、可确认、可回滚、可审计的工作流。`EvidenceLibraryService` 在同一个事务内完成：

1. 读取或创建当前 `resume_evidence`。
2. 记录 previous status。
3. 计算 changed fields，并生成 before/after snapshot。
4. 根据动作更新 Draft、Confirmed、Returned、Archived 等状态。
5. 写入 `resume_evidence_audit_event`。
6. 返回包含 `auditTrail` 的 evidence detail。

支持的动作包括 `CREATE_DRAFT`、`UPDATE_DRAFT`、`CONFIRM`、`RETURN_TO_DRAFT`、`ARCHIVE` 和 `RESTORE`。当前 actor 是 demo user，用于展示状态流转，不是生产鉴权或生产级权限系统。

## Human Review 审计链路

人工复核是 OfferFlow 的核心安全链路。AI 或规则生成内容默认是 `Draft`，复制或投递前必须进入人工复核。P3B 中，`HumanReviewService` 在同一个事务内完成：

1. 读取当前 `human_review_item`。
2. 记录 previous status / risk level。
3. 根据动作更新当前状态和风险等级。
4. 写入 `human_review_audit_event`。
5. 返回包含 `auditTrail` 的 review detail。

支持的动作包括 `CONFIRM`、`RETURN`、`FLAG_RISK`、`ADD_NOTE`、`AUTO_RISK_GUARD`。当前接口已覆盖 confirm、return 和 flag-risk；seed 数据中包含自动风险守卫和历史人工复核示例。

## 设计取舍

- H2 用于本地 demo/test，避免引入真实用户数据和外部依赖。
- MySQL profile 只保留可切换配置，不在本阶段连接真实 MySQL。
- 响应层继续使用 Java record，数据库 entity 与 API DTO 分离，便于后续审计、权限和状态机扩展。
- JSON 字段暂存为 `TEXT`，由 `JsonCodec` 管理；审计事件先用结构化列保存关键字段，便于后续查询。
- Provider 设置、Dashboard、Demo JD 分析暂时仍为组合 service；核心证据、复核、Trace、报告、面试准备和投递跟踪已优先读库。

## 边界

本架构不接真实 LLM，不调用 DeepSeek，不保存 API Key，不接招聘平台 API，不爬虫，不自动投递，不保存真实隐私，也不输出 Offer/录取概率或保证通过。当前 actor 是 demo user，不是生产鉴权或生产级权限系统。
