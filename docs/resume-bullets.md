# OfferFlow Copilot Resume Bullets And Talk Track

## Project Name

OfferFlow Copilot

## 中文项目定位

面向求职场景的 Java + AI 工作流作品集项目，覆盖 JD 入库、简历证据管理、匹配报告版本化、人工复核、Provider Trace、Schema/Risk 校验与复制门禁治理。

## English Positioning

Java + AI workflow demo for JD intake, resume evidence management, match report versioning, human review, provider traceability, and copy permission governance.

## Resume Bullets

### A. Java 后端 / AI 应用实习版

- 基于 Spring Boot 3 + Java 17 + MyBatis-Plus 搭建 JD intake、简历证据、匹配报告版本、Human Review 与 Copy Permission 的后端工作流接口。
- 使用 Flyway 管理 H2/MySQL 兼容 schema migration，支持 H2 demo/test mode、MySQL profile 与 Docker Compose 本地验证。
- 设计审计日志和状态流转，覆盖 evidence draft/confirm/archive、match report review sync、copy check 和 local permission audit 等流程。
- 实现 Provider SPI、Prompt/Schema Contract、Provider Response Validator 与 Risk Guard，并接入 GitHub Actions CI；当前本地验收为 `mvn test` 126 tests passed。

### B. AI 工具开发 / VibeCoding 方向版

- 设计 Provider SPI，将 `local-rule`、no-op adapters 与 optional manual real Provider dry-run path 放入同一套 trace/fallback/review 边界。
- 建立 Prompt / Schema Contract、Provider Response Validator 和 Risk Guard，要求输出先 schema validate，再进入 Human Review 与 Copy Permission。
- 将 Provider Trace、risk flags、fallback reason、schema validation、copyAllowed 等信息可视化，证明 AI 输出从生成到复制的全链路状态。
- 完成 DeepSeek 与 OpenAI-compatible relay 的 optional manual real Provider dry-run metadata verification；默认关闭真实调用，`rawResponseSaved=false`，确认前 `copyAllowed=false`。

### C. 中文 Boss / HR 简短版

- 做了一个完整可运行的 Java + Vue AI 工作流项目，把 JD 入库、简历证据、匹配报告、人工复核和复制门禁串成可追踪流程。
- 项目有真实浏览器截图、126 个后端测试、前端 build、Playwright screenshots 和 GitHub Actions CI，不只是静态页面。
- 支持 optional manual real Provider dry-run，但默认关闭；边界清楚，不接招聘平台 API、不爬虫、不自动投递、不保存真实隐私。

## 30 秒面试介绍

我做了一个叫 OfferFlow Copilot 的 Java + AI 工作流项目，场景是求职里的 JD 分析和简历证据管理。它不是普通的 JD 分析器，而是把 JD 入库、简历证据、匹配报告、Provider Trace、人工复核和复制权限串成一条可审计流程。技术上主要用 Spring Boot 3、MyBatis-Plus、Flyway、Vue 3 和 Playwright。比较有亮点的是 Human Review 和 Copy Permission 分开设计，AI 或 local-rule 生成的内容不能直接复制，必须先经过复核和复制契约。这个项目是作品集工程 demo，不接招聘平台、不爬虫、不自动投递，也不预测 Offer。

## 1 分钟面试介绍

OfferFlow Copilot 是我做的一个 Java + AI workflow 作品集项目，目标是把求职场景里的 JD 分析做得更可追踪。流程从 JD Intake 开始，用户手动粘贴 JD，系统生成结构化解析版本；然后在 Resume Evidence Library 里维护简历证据、状态和审计历史；再把 JD 要求和证据绑定，生成 Match Report，并保留版本化记录和评分拆解。

后面我重点做了 Human Review、Provider Trace 和 Copy Permission。Provider 输出不管来自 local-rule 还是 optional manual real Provider dry-run，都要经过 Schema Validate、Risk Guard，再进入人工复核；复核 confirmed 之前 `copyAllowed=false`。后端使用 Spring Boot 3、MyBatis-Plus、Flyway，默认 H2 demo/test mode，也支持 MySQL profile 和 Docker Compose 本地验证。工程验收上有 126 个 Maven tests、前端 build、18 个 Playwright screenshot checks 和 GitHub Actions CI。边界上它不是生产招聘平台，不接平台 API，不爬虫，不自动投递，也不保存真实隐私。

## 面试深挖 Q&A

### 1. 这个项目解决什么问题？

它解决的是 AI 求职辅助里“生成内容不可信、证据不可追踪、复制边界不清楚”的问题。OfferFlow 把 JD、简历证据、报告版本、Provider Trace、Human Review 和 Copy Permission 放在同一条 workflow 里，让每个输出都能回到证据和审核状态。

### 2. 为什么不是普通 JD 分析器？

普通 JD 分析器通常是输入 JD、输出一段分析。OfferFlow 更关注流程治理：JD 会形成 parse version，简历证据要有状态和审计记录，匹配报告要版本化，Provider 输出要 schema/risk 校验，最后还要 Human Review 和 Copy Permission。

### 3. 为什么需要 Human Review？

因为 AI 或规则生成的内容不能默认可信，尤其是简历和求职表达场景。Human Review 用来确认内容是否有证据支撑、是否夸大、是否需要退回修改。只有 confirmed 之后才可能进入复制检查。

### 4. Copy Permission 怎么设计？

Copy Permission 是独立于生成和复核的最后一道门。系统会检查 review state、copy contract、risk state 和 audit context。confirmed 之前 `copyAllowed=false`，复制检查本身也会写入 audit history。

### 5. Provider SPI 怎么设计？

Provider SPI 把 provider mode、model、structuredJson、fallback reason、risk flags、traceId 等结果统一成 `ProviderResponse`。默认是 `local-rule`，OpenAI-compatible 和 DeepSeek 有 no-op/fallback adapter，真实调用只在 optional manual dry-run path 里显式开启。

### 6. DeepSeek / OpenAI-compatible relay 是怎么验证的？

用 sanitized input 手动执行 optional real Provider dry-run，记录的只是 metadata：external call attempted、schema validated、risk guard passed、humanReviewRequired、copyAllowed=false、rawResponseSaved=false 等状态。没有记录 API key，也没有保存 raw model response。

### 7. 为什么 raw response 不保存？

raw response 可能包含不可控文本、隐私或敏感信息。项目目标是验证 provider path、schema normalization 和 review/copy gate，不是沉淀模型原文。因此只保存结构化后的安全字段和 trace metadata，`rawResponseSaved=false` 是边界要求。

### 8. local-rule 和 real provider dry-run 区别是什么？

`local-rule` 是默认本地确定性规则路径，不需要网络和 API key，适合 demo、测试和 CI。real provider dry-run 是手动、可选、默认关闭的真实外部调用验证路径，只能在用户明确确认、PII Guard 通过、配置齐全时执行，而且输出仍然不能绕过 review 和 copy gate。

### 9. 如果真实上线还差什么？

还需要生产级身份认证、权限体系、租户隔离、密钥管理、日志脱敏、隐私合规、可靠的队列和重试、观测告警、完整安全评审、真实数据治理和人工审核运营机制。当前项目只定位为 portfolio-grade engineering demo。

### 10. 为什么不用爬虫？

因为这个项目的重点是 JD 输入后的证据工作流，不是采集招聘平台数据。爬虫会引入平台规则、登录、隐私和合规风险，也会让项目重点偏离 Java + AI workflow 工程能力。

### 11. 为什么不做自动投递？

自动投递涉及用户授权、平台协议、简历真实性和求职风险。OfferFlow 只做手动流程管理和 evidence-first 分析，不替用户投递，不自动联系 HR，也不承诺结果。

### 12. 这个项目里最能体现 Java 后端能力的点是什么？

主要是状态流转和审计链路：JD parse version、evidence lifecycle、match report review sync、copy permission audit、Provider validation/fallback 都在后端有明确边界。再加上 Flyway schema migration、H2/MySQL 兼容和 126 个 Maven tests，能体现后端工程化完整度。

### 13. Provider validation 失败时怎么处理？

不能把失败包装成成功。Provider response 如果 schemaVersion、required fields 或 risk policy 不通过，就要 fallback 到 local-rule，并保留 fallbackReason、traceId 和 Human Review 边界。

### 14. 为什么要做 Playwright screenshots？

因为作品集项目需要可视化证据。Playwright screenshots 证明页面是真实运行截图，并且有 1366 viewport 的 horizontal overflow 检查，不把设计参考图当成真实页面证据。

## 不可写表述清单

- 生产级招聘平台。
- 稳定接入 GPT / DeepSeek。
- 真实用户、真实客户、真实线上流量。
- 自动投递、自动联系 HR。
- Offer 预测、录取概率、保证通过、提升 Offer 率。
- 实时面试辅助或作弊功能。
- 招聘平台爬虫或平台自动化采集。
- 企业客户、商业指标或生产部署能力。

## 可写表述清单

- `portfolio-grade engineering demo`
- `optional manual real Provider dry-run`
- `DeepSeek / OpenAI-compatible relay manual dry-run verified`
- `rawResponseSaved=false`
- `output requires Human Review`
- `copyAllowed=false before confirmation`
- H2/MySQL/Flyway
- Provider SPI / Prompt Schema Contract / Risk Guard
- GitHub Actions CI
- Playwright screenshots / horizontal overflow validation
