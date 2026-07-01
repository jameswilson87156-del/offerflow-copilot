# Provider Sandbox

Provider Sandbox 用于本地演示 Provider SPI 的配置校验、fallback 和 Trace Evidence 写入。它不是生产模型网关。

## 接口

`POST /api/provider/sandbox-run`

Request body:

```json
{
  "taskType": "provider-sandbox",
  "inputText": "Java Spring Boot sandbox run.",
  "providerMode": "openai-compatible",
  "simulateFailure": false,
  "simulateTimeout": false,
  "actor": "provider-settings-user",
  "actorRole": "Human reviewer"
}
```

Response 是 `ProviderResponse`，包含 selected provider、final provider、fallback used、fallback reason、duration、traceId、risk flags、rawResponseSaved=false 和 humanReviewRequired=true。

## 行为

- 不发起真实外部 Provider 调用。
- `local-rule` 会直接返回 deterministic 本地结果。
- OpenAI-compatible 和 DeepSeek 当前是 no-op adapter。
- 未配置 provider、simulate failure、simulate timeout 都会 fallback 到 `local-rule`。
- fallback reason 会写入响应、`provider_trace_run` 和 Trace Evidence detail。
- 输出仍需 Human Review。

## Trace Evidence

每次 sandbox run 至少写入 8 条 `trace_step`：

1. Provider Config Check
2. Prompt Build
3. Provider Select
4. Provider No-op 或 Provider Call
5. Fallback Decision
6. Schema Validate
7. Risk Guard
8. Human Review Required

状态使用 `SUCCESS`、`FALLBACK`、`WARNING`、`BLOCKED`、`ERROR`。当前实现主要写入 `SUCCESS`、`FALLBACK` 和 `WARNING`。

## 前端页面

`/provider-settings` 新增：

- Provider Config Check：展示 local-rule 可用、OpenAI-compatible/DeepSeek configured 状态、realCallEnabled=false、rawResponseSave=false、API Key masked/not configured 和 boundary notice。
- Sandbox Run：选择 local-rule、OpenAI-compatible 或 DeepSeek，并可模拟 failure / timeout。
- Result Summary：展示 selected provider、final provider、fallback used、fallback reason、duration、trace id、risk flags 和 human review required。
- Trace Timeline：sandbox run 后自动刷新最新 trace。

页面文案必须保持清晰：当前未发起真实外部模型调用，未配置 Provider 必须 fallback，模型失败不伪装成功，所有输出仍需 Human Review。

## 安全边界

Sandbox 只用于本地演示。不要在请求、日志、文档或页面中写入真实 API Key、真实手机号、邮箱、身份证、聊天记录或招聘平台私信。不要把 sandbox 结果描述为真实模型质量、生产稳定性或录取概率。
