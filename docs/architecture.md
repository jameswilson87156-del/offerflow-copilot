# 架构说明

## P1 结构

```text
Vue 3 Workbench
  ├─ API client（/api）
  ├─ typed demo fallback（后端不可用时明确标注）
  └─ domain components（要求、证据、复核、评分、时间线）
           │
           ▼
Spring Boot 3
  ├─ HealthController
  ├─ ProviderController
  ├─ DashboardController
  └─ JobAnalysisController
           │
           ▼
deterministic mock / local-rule data（无数据库、无外部调用）
```

## 设计取舍

- 本轮不引入数据库，避免制造未要求的隐私与迁移复杂度。
- 返回结构使用 Java record，保持契约紧凑、不可变。
- 前端保留 API 失败 fallback，便于纯静态展示；界面会显式区分实时本地 API 与演示快照。
- P2 如接 Provider，应通过独立 SPI、配置校验、超时、审计与降级层实现。

