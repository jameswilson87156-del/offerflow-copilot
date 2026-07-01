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

## 下一阶段候选

- [ ] P3E：基于 JD parse version 与 evidence binding 生成可复核 Match Report 版本，并进入 Human Review
- [ ] 增加 MySQL migration 脚本和本地 Docker Compose 示例
- [ ] 引入 Provider SPI、超时、审计与失败回退；仍需显式配置后才允许沙箱调用
- [ ] 增加 service 层单元测试、前端组件测试和无障碍自动检查
