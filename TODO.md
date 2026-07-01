# TODO

## 已完成

- [x] P1A：Spring Boot 工程骨架与 mock/local-rule 接口
- [x] P1B：Vue 高保真 JD 证据匹配工作台
- [x] P2A：匿名化简历证据库与 Coverage Map
- [x] P2B：人工复核中心
- [x] P2C：Provider 设置与证据链页面
- [x] P2D：匹配报告、面试准备、投递跟踪页面闭环
- [x] P3A：H2/MyBatis-Plus 持久化基础、schema、seed、repository 和核心接口读库
- [x] P3B：Human Review 审计日志与状态流转历史
- [x] P3C：证据库人工编辑草稿、确认、回滚与审计
- [x] P3D：结构化 JD Intake、JD 解析版本历史与证据绑定审计
- [x] P3E：基于 JD parse version 与 evidence binding 生成可版本化 Match Report，并进入 Human Review
- [x] P3F：Match Report 与 Human Review 双向状态同步、复制许可检查、恢复与归档闭环
- [x] P3G：三处 Audit Trail 卡内展开、copy-check 结构化历史、状态标签与 Archived 只读视觉态统一
- [x] P4A：Flyway V1 migration、H2/MySQL schema 兼容验证、本地 MySQL 8 Docker Compose 与启动文档

## 下一阶段候选

- [ ] P4B：抽象 Provider SPI、超时、审计与失败回退；默认继续 local-rule，不配置时禁止外部调用
- [ ] 增加 service 层单元测试、前端组件测试和无障碍自动检查
