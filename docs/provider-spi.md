# Provider SPI

P4B 为后续模型接入建立 Provider SPI，P4C 在 SPI 上增加 Prompt Contract、Risk Policy、Response Schema 和 Response Validator。当前仍是 `local-rule` / no-op。OpenAI-compatible、DeepSeek 或中转站都没有真实网络调用。

## 结构

- `AiProviderClient`：统一 provider contract，包含 `analyze`、`health`、`descriptor`。
- `ProviderRequest`：记录 runId、providerMode、taskType、promptVersion、schemaVersion、inputText、evidenceRefs、riskPolicy、timeoutMs 和 metadata。
- `ProviderResponse`：记录 success、providerMode、finalProvider、model、outputText、structuredJson、fallbackUsed、fallbackReason、errorCode、durationMs、traceId、riskFlags、rawResponseSaved=false 和 humanReviewRequired=true。
- `ProviderDescriptor`：面向 `/api/provider/settings`，只展示配置状态、模型名、timeout、fallback policy 和 boundary notice，不展示 API Key 明文。
- `ProviderHealth`：面向内部健康状态，说明 configured、realCallEnabled 和 reason。
- `com.offerflow.copilot.provider.contract`：保存 `ProviderTaskType`、`PromptContract`、`RiskPolicy`、`ProviderResponseSchema`、`ProviderResponseValidator` 和 validation result。

## Provider 实现

- `LocalRuleProviderClient`：默认可用，deterministic 输出，不访问外部服务。
- `NoOpOpenAiCompatibleProviderClient`：读取 OpenAI-compatible 配置，未配置或真实调用禁用时返回 fallback-required，不发网络请求。
- `NoOpDeepSeekProviderClient`：读取 DeepSeek 配置，未配置或真实调用禁用时返回 fallback-required，不发网络请求。

`ProviderRouter` 根据 provider mode、配置状态、simulate failure 和 simulate timeout 决定是否 fallback 到 `local-rule`。`ProviderExecutionService` 负责创建 runId/traceId、加载 PromptContract/RiskPolicy、执行 sandbox run、调用 ProviderResponseValidator，并写入 `provider_trace_run` 和 `trace_step`。

## 配置

默认值：

```properties
offerflow.ai.provider.mode=local-rule
offerflow.ai.provider.real-call-enabled=false
offerflow.ai.provider.raw-response-save=false
offerflow.ai.provider.timeout-ms=8000
```

外部 provider 配置通过环境变量占位读取：

```properties
OFFERFLOW_OPENAI_COMPATIBLE_BASE_URL
OFFERFLOW_OPENAI_COMPATIBLE_API_KEY
OFFERFLOW_OPENAI_COMPATIBLE_MODEL
OFFERFLOW_DEEPSEEK_BASE_URL
OFFERFLOW_DEEPSEEK_API_KEY
OFFERFLOW_DEEPSEEK_MODEL
```

API Key 不落库、不展示、不写日志。接口只返回 `masked`、`not configured` 或 `disabled`。`rawResponseSave` 默认 false，`ProviderResponse.rawResponseSaved` 在 P4C 中固定为 false。

## Fallback 策略

- `local-rule`：直接执行本地规则，不需要 fallback。
- OpenAI-compatible 未配置：fallback 到 `local-rule`，记录 fallback reason。
- DeepSeek 未配置：fallback 到 `local-rule`，记录 fallback reason。
- simulate failure：fallback 到 `local-rule`，记录 simulated failure。
- simulate timeout：fallback 到 `local-rule`，记录 simulated timeout 和 timeoutMs。
- 未知 provider mode：fallback 到 `local-rule`，记录 unknown mode。

模型失败不能伪装成成功。响应会明确 `fallbackUsed=true`、`finalProvider=local-rule` 和 `fallbackReason`。

## Contract Validation

- `GET /api/provider/contracts` 返回所有 taskType 的 prompt/schema/risk policy 摘要。
- `GET /api/provider/contracts/{taskType}` 返回单个 PromptContract detail。
- `POST /api/provider/validate-response` 只验证本地模拟响应，不发外部请求，不保存 raw model response。
- Validator 检查 required fields、schemaVersion、禁用表述、rawResponseSaved=false，以及 `realCallEnabled=false` 时外部 provider 不能被标记为真实成功。
- validation 失败时必须 `fallbackRequired=true`、`humanReviewRequired=true`，并保留 violation code/message。

## 当前边界

P4C 不是生产级模型网关，不支持真实 OpenAI、DeepSeek 或中转站调用。所有输出仍是 Draft，需要 Human Review；不输出 Offer 概率、录取概率或保证通过。
