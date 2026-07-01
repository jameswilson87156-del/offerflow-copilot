# HANDOFF

## 当前交付

P1A + P1B 聚焦工程骨架、高保真静态工作台与 mock/local-rule API。所有岗位、项目、评分和面试材料均为演示数据。

## 启动顺序

1. 根目录执行 `mvn spring-boot:run`。
2. `frontend` 目录执行 `npm install && npm run dev`。
3. 打开 `http://localhost:5173`。

## 关键约定

- Java package 固定为 `com.offerflow.copilot`。
- 演示接口集中在 `/api`。
- 前端类型定义和 fallback 数据集中维护，UI 不散落硬编码业务对象。
- Provider 状态必须如实展示，不能把 fallback 描述为真实 LLM。
- 任何未来数据持久化都必须先设计匿名化、删除和授权边界。

