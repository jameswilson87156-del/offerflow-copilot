# Provider Sandbox

Provider Sandbox 用于本地演示 Provider SPI 的配置校验、contract validation、fallback 和 Trace Evidence 写入。它不是生产模型网关。

P4F 新增的 `POST /api/provider/real-dry-run` 是另一个手动 dry-run 入口；它不改变 sandbox-run 的 no-op/local-rule 行为。真实外呼仍默认关闭，且必须经过 Local Permission、PII Guard、显式勾选和 Provider 配置校验。

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
  "actor": "demo.owner",
  "actorRole": "OWNER"
}
```

Response 是 `ProviderResponse`，包含 selected provider、final provider、fallback used、fallback reason、duration、traceId、risk flags、rawResponseSaved=false 和 humanReviewRequired=true。

P4E 后，sandbox run 会先经过 Local Permission Model。`OWNER` 和 `SYSTEM` 可以执行 `PROVIDER_SANDBOX_RUN`；`REVIEWER`、`EDITOR` 和 `VIEWER` 会被拒绝并写入 `permission_audit_event`。

## 行为

- 不发起真实外部 Provider 调用。
- `local-rule` 会直接返回 deterministic 本地结果。
- OpenAI-compatible 和 DeepSeek 当前是 no-op adapter。
- 未配置 provider、simulate failure、simulate timeout 都会 fallback 到 `local-rule`。
- fallback reason 和 validation result 会写入响应、`provider_trace_run` 和 Trace Evidence detail。
- 输出仍需 Human Review。

## Trace Evidence

每次 sandbox run 至少写入 12 条 `trace_step`：

1. Provider Config Check
2. Prompt Contract Load
3. Risk Policy Load
4. Prompt Build
5. Provider Select
6. Provider No-op 或 Provider Call
7. Fallback Decision
8. Provider Response Validate
9. Schema Contract Validate
10. Risk Policy Guard
11. Contract Violation Check
12. Human Review Required

状态使用 `SUCCESS`、`FALLBACK`、`WARNING`、`BLOCKED`、`ERROR`。当前实现主要写入 `SUCCESS`、`FALLBACK` 和 `WARNING`。

## 前端页面

`/provider-settings` 新增：

- Provider Config Check：展示 local-rule 可用、OpenAI-compatible/DeepSeek configured 状态、realCallEnabled=false、rawResponseSave=false、API Key masked/not configured 和 boundary notice。
- Sandbox Run：选择 local-rule、OpenAI-compatible 或 DeepSeek，并可模拟 failure / timeout。
- Result Summary：展示 selected provider、final provider、fallback used、fallback reason、duration、trace id、risk flags 和 human review required。
- Provider Contract：展示 taskType、promptVersion、schemaVersion、riskPolicyVersion、required inputs、forbidden claims 和 output schema。
- Response Validation Sandbox：模拟 missing field、unsafe claim、schema mismatch，并展示 violations、fallbackRequired、humanReviewRequired、riskFlags 和 sanitizedOutput。
- Trace Timeline：sandbox run 后自动刷新最新 trace。
- Permission Audit：展示最近本地权限决策，包括 actor、role、action、target、allowed 和 reason。
- Real Provider Dry-run：选择 DeepSeek / OpenAI-compatible、taskType、脱敏输入，勾选 `confirmNoPii` 和 `allowExternalCall` 后手动运行，并展示 blocked/fallback/schema/risk/human-review/copy 状态、Trace ID 和 Run ID。

页面文案必须保持清晰：sandbox-run 未发起真实外部模型调用；real dry-run 是手动、默认关闭、非生产级稳定接入。真实 Provider 输出必须通过 schema validate 和 risk guard，所有输出仍需 Human Review。P4D 后，validation 通过也不代表可复制；Copy Permission Contract 是 Human Review Confirmed 后的最后一道门禁。

## 安全边界

Sandbox 只用于本地演示。不要在请求、日志、文档或页面中写入真实 API Key、真实手机号、邮箱、身份证、聊天记录或招聘平台私信。不要把 sandbox 结果描述为真实模型质量、生产稳定性或录取概率。

Provider Sandbox 的 Local Permission Model 不是生产鉴权。P4E 只提供本地 role switch、disabled action 和 permission audit，用于演示写操作边界。

Real Provider dry-run 也使用同一套 Local Permission Model：`OWNER` / `EDITOR` 可运行，`VIEWER` 禁止，`SYSTEM` 禁止作为人工 dry-run actor。Denied 必须写入 `permission_audit_event`。
