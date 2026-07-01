# 架构说明

## P3A 结构

```text
Vue 3 Workbench
  ├─ Vue Router（7 个核心页面）
  ├─ API client（/api）
  ├─ typed demo fallback（后端不可用时明确标注）
  └─ domain views（JD、证据库、匹配报告、面试准备、投递跟踪、人工复核、Provider Trace）
           │
           ▼
Spring Boot 3
  ├─ Controllers（保持前端响应结构兼容）
  ├─ Services（组合 local-rule 语义与数据库读取）
  ├─ Repositories（MyBatis-Plus BaseMapper 封装）
  ├─ JsonCodec（TEXT JSON 字段统一编解码）
  └─ PersistenceSeedService（空库 seed 脱敏 demo）
           │
           ▼
H2 demo persistence
  ├─ resume_evidence
  ├─ job_post
  ├─ match_report
  ├─ interview_prep
  ├─ application_record
  ├─ human_review_item
  ├─ provider_trace_run
  └─ trace_step
```

## 设计取舍

- H2 用于本地 demo/test，避免引入真实用户数据和外部依赖。
- MySQL profile 只保留可切换配置，不在本阶段连接真实 MySQL。
- 响应层继续使用 Java record，数据库 entity 与 API DTO 分离，便于后续审计/状态流转扩展。
- JSON 字段暂存为 `TEXT`，由 `JsonCodec` 管理；P3B 可进一步拆表或增加审计事件表。
- Provider 设置、Dashboard、Demo JD 分析暂时仍为组合 service；核心证据、复核、Trace、报告、面试准备和投递跟踪已优先读库。

## 边界

本架构不接真实 LLM，不调用 DeepSeek，不保存 API Key，不接招聘平台 API，不爬虫，不自动投递，不保存真实隐私，也不输出 Offer/录取概率或保证通过。
