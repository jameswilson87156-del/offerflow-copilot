# Database Migration

## 当前策略

P4A 起，Flyway 是 OfferFlow schema 的唯一自动初始化入口。默认 H2 demo、test profile 和本地 MySQL profile 都读取：

```text
src/main/resources/db/migration/V1__init_offerflow_schema.sql
```

V1 包含现有 15 张业务表，字段、主键类型和 API 数据结构保持不变。复杂 JSON 继续保存为 `TEXT`，时间字段使用 H2/MySQL 都支持的 `TIMESTAMP`。

## 启动顺序

1. Spring Boot 创建 datasource。
2. Flyway 校验 `flyway_schema_history` 并执行尚未应用的 migration。
3. MyBatis-Plus mapper/repository 使用已完成的 schema。
4. `PersistenceSeedService` 仅对空表写入脱敏 demo seed。

所有 profile 都配置了 `spring.sql.init.mode=never`。原 `src/main/resources/schema.sql` 为了保留历史没有删除，但它只是特殊 fallback 参考，不会与 Flyway 一起自动执行。

## 新增 migration 规则

- 已提交并在任意环境执行过的 migration 不应原地修改。
- 后续变更新增递增文件，例如 `V2__add_example_column.sql`。
- SQL 应同时考虑 H2 和 MySQL；本项目 JSON 暂用 `TEXT`，不使用数据库专属 JSON 类型。
- migration 只管理结构或明确的静态 reference data；demo seed 继续由 `PersistenceSeedService` 管理。
- 禁止把真实招聘信息、手机号、邮箱、身份证、聊天记录、API Key 或生产凭据写入 migration。

## 验证

`PersistenceFoundationTest` 以 test profile 启动 H2，断言 Flyway 当前版本为 V1，并检查 `job_post`、`resume_evidence`、`match_report_version`、`human_review_item`、`match_report_audit_event` 和 `flyway_schema_history` 存在。

本地 MySQL smoke 可按 [local-mysql.md](local-mysql.md) 执行。P4A 已验证 V1 在 MySQL 8 上创建 15 张业务表，连续两次启动时 migration 不重复、seed 计数不增加。

P4B 没有修改 schema。Provider SPI sandbox 复用 V1 中已有的 `provider_trace_run` 和 `trace_step` 表写入 fallback 与 Trace Evidence；因此本轮不需要新增 `V2` migration，也不要求普通 `mvn test` 依赖 Docker MySQL。

## 边界

Migration 能力不等于生产数据库治理。当前没有生产级备份恢复、最小权限、密钥轮换、数据保留/删除、可观测性或高可用方案。所有 seed 都是脱敏 demo 数据，业务计算仍是 deterministic `local-rule`。
