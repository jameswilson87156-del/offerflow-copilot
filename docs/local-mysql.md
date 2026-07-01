# Local MySQL

## 用途

`docker-compose.yml` 提供一个 MySQL 8 service，仅用于本地开发、演示和 H2/MySQL schema 兼容检查。它不是生产部署声明，也不包含 backend/frontend 容器、TLS、备份、监控或生产级密钥管理。

## 前置条件

- Docker Engine 与 Docker Compose 可用。
- 本机 3306 端口未被占用；如已占用，可通过 `OFFERFLOW_MYSQL_PORT` 修改宿主机端口，并同步覆盖 datasource URL。
- Windows + WSL 环境可在 `/mnt/d/workhome/offerflow-copilot` 执行 Compose；若 Windows 到 WSL 端口没有转发，可在 WSL 内运行 Java/Maven。

## 启动

```bash
docker compose up -d mysql
docker compose ps
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Compose 默认创建：

- database：`offerflow`
- user：`offerflow`
- password：`offerflow_dev_password`
- host port：`${OFFERFLOW_MYSQL_PORT:-3306}`
- named volume：`offerflow_mysql_data`

这些是公开的本地 demo 默认值，不得复用于生产。后端配置可覆盖：

```bash
OFFERFLOW_DB_URL=jdbc:mysql://localhost:3306/offerflow?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
OFFERFLOW_DB_USERNAME=offerflow
OFFERFLOW_DB_PASSWORD=offerflow_dev_password
OFFERFLOW_SEED_DEMO_DATA=true
```

不要把真实密码写入仓库，不要提交 `.env`、数据库数据目录、日志、`target` 或 `node_modules`。

## 停止与数据保留

```bash
docker compose stop mysql
```

该命令停止容器但保留 named volume，下一次启动可验证 Flyway 与 seed 的幂等行为。只有明确要删除本地 demo 数据时才应手动删除 volume；常规验收不要执行 `down -v`。

## 手动 smoke check

启动后端时应看到 Flyway 将 schema 迁移到 V1，或提示 schema 已是最新。可在 MySQL 中检查：

```sql
SELECT version, success FROM flyway_schema_history;
SELECT COUNT(*) FROM information_schema.tables
WHERE table_schema = 'offerflow'
  AND table_name <> 'flyway_schema_history';
```

预期 migration 为 V1 且成功，业务表数量为 15。后端连续启动两次后，`job_post`、`resume_evidence` 等 demo seed 计数应保持不变。

## 数据与产品边界

MySQL profile 保存的仍是脱敏 demo/local-rule 数据，不应写入真实手机号、邮箱、身份证、聊天记录或招聘平台数据。项目不保存 API Key、不接招聘平台 API、不爬虫、不自动投递、不接真实 LLM，也不输出 Offer/录取概率或保证通过。
