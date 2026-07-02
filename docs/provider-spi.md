# Provider SPI

## Manual Dry-run Verification

P4F/P4G manual real Provider dry-run was verified with sanitized input for DeepSeek and an OpenAI-compatible relay. Only status metadata is recorded: both runs used `externalCallAttempted=true`, `externalCallBlocked=false`, `fallbackUsed=false`, `schemaValidated=true`, `riskGuardPassed=true`, `humanReviewRequired=true`, `copyAllowed=false`, and `rawResponseSaved=false`.

- DeepSeek: `finalProvider=deepseek`, `model=deepseek-v4-pro`, `traceId=P4F-96549DC4`.
- OpenAI-compatible relay: `finalProvider=openai-compatible`, `model=gpt-5.5`, `traceId=P4F-2D3FF22E`.

This does not change the Provider SPI boundary: the path is optional, manual, review-gated, and not stable production provider integration. API keys and raw model responses are not recorded.

P4B 为后续模型接入建立 Provider SPI，P4C 在 SPI 上增加 Prompt Contract、Risk Policy、Response Schema 和 Response Validator，P4F 增加手动 real dry-run 路径。默认仍是 `local-rule` / no-op；真实调用必须手动开启并通过多重门禁。

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
- `RealProviderDryRunService`：手动 dry-run 编排，只支持 DeepSeek 和 OpenAI-compatible，先检查权限、PII、配置、PromptContract 和 RiskPolicy，再决定是否通过 `HttpRealProviderGateway` 尝试外部调用。
- `HttpRealProviderGateway`：OpenAI-compatible chat-completions HTTP gateway；只在 dry-run 门禁全部通过时使用环境变量中的 API Key，不日志打印、不返回、不保存 key 或 raw response。

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

API Key 不落库、不展示、不写日志。接口只返回 `masked`、`not configured` 或 `disabled`。`rawResponseSave` 默认 false，`ProviderResponse.rawResponseSaved` 在 P4F 中固定为 false。

## Fallback 策略

- `local-rule`：直接执行本地规则，不需要 fallback。
- OpenAI-compatible 未配置：fallback 到 `local-rule`，记录 fallback reason。
- DeepSeek 未配置：fallback 到 `local-rule`，记录 fallback reason。
- simulate failure：fallback 到 `local-rule`，记录 simulated failure。
- simulate timeout：fallback 到 `local-rule`，记录 simulated timeout 和 timeoutMs。
- 未知 provider mode：fallback 到 `local-rule`，记录 unknown mode。
- real dry-run 外部调用失败：fallback 到 `local-rule`，记录通用 failure reason。
- real dry-run 响应 Schema Validate 或 Risk Guard 不通过：fallback 到 `local-rule`，保留 violation code。
- real dry-run PII 命中、未勾选 `confirmNoPii` 或 `allowExternalCall=false`：网络前 blocked，不尝试外部请求。

模型失败不能伪装成成功。响应会明确 `fallbackUsed=true`、`finalProvider=local-rule` 和 `fallbackReason`。

## Contract Validation

- `GET /api/provider/contracts` 返回所有 taskType 的 prompt/schema/risk policy 摘要。
- `GET /api/provider/contracts/{taskType}` 返回单个 PromptContract detail。
- `POST /api/provider/validate-response` 只验证本地模拟响应，不发外部请求，不保存 raw model response。
- Validator 检查 required fields、schemaVersion、禁用表述、rawResponseSaved=false，以及 `realCallEnabled=false` 时外部 provider 不能被标记为真实成功。
- validation 失败时必须 `fallbackRequired=true`、`humanReviewRequired=true`，并保留 violation code/message。

## 当前边界

P4F 不是生产级模型网关，也不证明 DeepSeek 或 OpenAI-compatible 生产稳定可用。所有输出仍是 Draft，需要 Human Review；验证通过不代表可复制，P4D 还要求 Copy Permission Contract 通过；不输出 Offer 概率、录取概率或保证通过。详见 [real-provider-dry-run.md](real-provider-dry-run.md)。
