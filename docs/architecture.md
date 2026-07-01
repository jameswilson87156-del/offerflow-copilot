# 架构说明

## P1 + P2A 结构

```text
Vue 3 Workbench
  ├─ Vue Router（/jd-analyzer、/evidence-library）
  ├─ API client（/api）
  ├─ typed demo fallback（后端不可用时明确标注）
  └─ domain components（要求、证据卡、来源链、复核、Coverage Map）
           │
           ▼
Spring Boot 3
  ├─ HealthController
  ├─ ProviderController
  ├─ DashboardController
  ├─ JobAnalysisController
  └─ EvidenceController
           │
           ▼
deterministic mock / local-rule data（无数据库、无外部调用）
```

## 设计取舍

- 本轮不引入数据库，避免制造未要求的隐私与迁移复杂度。
- 返回结构使用 Java record，保持契约紧凑、不可变。
- 前端保留 API 失败 fallback，便于纯静态展示；界面会显式区分实时本地 API 与演示快照。
- P2A 证据库为只读 mock/local-rule 视图，不上传、不持久化、不采集真实隐私。
- P2 如接 Provider，应通过独立 SPI、配置校验、超时、审计与降级层实现。
