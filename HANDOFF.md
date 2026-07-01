# HANDOFF

## 当前交付

P3A 已完成持久化基础层。核心 mock/local-rule 数据已迁移到 H2 seeded demo data，并通过 MyBatis-Plus entity/mapper/repository/service 读取。前端页面契约保持兼容。

新增持久化文件包括 `schema.sql`、`application-mysql.yml`、`JsonCodec`、`PersistenceSeedService`、8 组 entity/mapper/repository，以及 persistence 测试。新增文档见 `docs/persistence.md`。

## 启动顺序

1. 根目录执行 `mvn spring-boot:run`。
2. `frontend` 目录执行 `npm install && npm run dev`。
3. 打开 `http://localhost:5173`。

默认 H2 in-memory 数据库会在启动时建表并 seed 脱敏 demo 数据。重复启动不会重复插入已有表数据。

## 关键约定

- Java package 固定为 `com.offerflow.copilot`。
- 数据库 JSON 字段先用 `TEXT` 保存字符串，通过 `JsonCodec` 统一序列化/反序列化。
- H2 是本地 demo/test persistence；`mysql` profile 仅保留后续切换配置，本轮不连接真实 MySQL。
- Provider 状态必须如实展示，不能把 fallback 描述为真实 LLM。
- 不保存 API Key 明文，不保存真实隐私，不接招聘平台 API，不爬虫。

## 已验证

- `mvn test`：19 个测试通过，包含 API 契约与 persistence seed/json/repository 验证。
- `npm run build`：TypeScript 与 Vite 生产构建。
- `npm run screenshots`：全部页面截图与 1366 宽度横向溢出检查。
