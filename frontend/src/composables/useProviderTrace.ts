import { onMounted, shallowRef } from 'vue'
import {
  providerConfigCheckFallback,
  providerContractDetailFallback,
  providerContractsFallback,
  providerSettingsFallback,
  providerTraceIndexFallback,
  providerTraceRunFallback,
  providerValidationFallback,
} from '../data/providerTrace'
import type {
  ProviderConfigCheck,
  ProviderContractSummary,
  ProviderPromptContract,
  PermissionAuditEvent,
  ProviderRealDryRunPayload,
  ProviderRealDryRunResult,
  ProviderResponse,
  ProviderSandboxRunPayload,
  ProviderSettingsData,
  ProviderTraceIndex,
  ProviderTraceRun,
  ProviderValidatedResult,
  ProviderValidationPayload,
} from '../types'

export function useProviderTrace() {
  const settings = shallowRef<ProviderSettingsData>(providerSettingsFallback)
  const configCheck = shallowRef<ProviderConfigCheck>(providerConfigCheckFallback)
  const contracts = shallowRef<ProviderContractSummary[]>(providerContractsFallback)
  const selectedContract = shallowRef<ProviderPromptContract>(providerContractDetailFallback)
  const traceIndex = shallowRef<ProviderTraceIndex>(providerTraceIndexFallback)
  const traceRun = shallowRef<ProviderTraceRun>(providerTraceRunFallback)
  const sandboxResult = shallowRef<ProviderResponse | null>(null)
  const realDryRunResult = shallowRef<ProviderRealDryRunResult | null>(null)
  const validationResult = shallowRef<ProviderValidatedResult>(providerValidationFallback)
  const permissionAuditEvents = shallowRef<PermissionAuditEvent[]>([])
  const sandboxError = shallowRef('')
  const realDryRunError = shallowRef('')
  const validationError = shallowRef('')
  const loading = shallowRef(true)
  const running = shallowRef(false)
  const realDryRunning = shallowRef(false)
  const validating = shallowRef(false)
  const source = shallowRef<'api' | 'fallback'>('fallback')

  async function load(preferredRunId?: string, preferredTaskType?: string) {
    loading.value = true
    try {
      const [settingsResponse, configCheckResponse, tracesResponse, contractsResponse, permissionAuditResponse] = await Promise.all([
        fetch('/api/provider/settings'),
        fetch('/api/provider/config-check'),
        fetch('/api/provider/traces'),
        fetch('/api/provider/contracts'),
        fetch('/api/permissions/audit-events'),
      ])
      if (!settingsResponse.ok || !configCheckResponse.ok || !tracesResponse.ok || !contractsResponse.ok) {
        throw new Error('Local provider trace API unavailable')
      }

      const nextSettings = await settingsResponse.json() as ProviderSettingsData
      const nextConfigCheck = await configCheckResponse.json() as ProviderConfigCheck
      const nextTraceIndex = await tracesResponse.json() as ProviderTraceIndex
      const nextContracts = await contractsResponse.json() as ProviderContractSummary[]
      const nextPermissionAudit = permissionAuditResponse.ok
        ? await permissionAuditResponse.json() as PermissionAuditEvent[]
        : []
      const runId = preferredRunId ?? nextTraceIndex.items[0]?.runId ?? providerTraceRunFallback.runId
      const taskType = preferredTaskType ?? providerContractDetailFallback.taskType ?? nextContracts[0]?.taskType
      const [traceResponse, contractResponse] = await Promise.all([
        fetch(`/api/provider/traces/${runId}`),
        fetch(`/api/provider/contracts/${taskType}`),
      ])
      if (!traceResponse.ok || !contractResponse.ok) throw new Error('Local provider run API unavailable')

      settings.value = nextSettings
      configCheck.value = nextConfigCheck
      contracts.value = nextContracts
      permissionAuditEvents.value = nextPermissionAudit
      traceIndex.value = nextTraceIndex
      traceRun.value = await traceResponse.json() as ProviderTraceRun
      selectedContract.value = await contractResponse.json() as ProviderPromptContract
      source.value = 'api'
    } catch {
      settings.value = providerSettingsFallback
      configCheck.value = providerConfigCheckFallback
      contracts.value = providerContractsFallback
      selectedContract.value = providerContractDetailFallback
      traceIndex.value = providerTraceIndexFallback
      traceRun.value = providerTraceRunFallback
      permissionAuditEvents.value = []
      source.value = 'fallback'
    } finally {
      loading.value = false
    }
  }

  async function runSandbox(payload: ProviderSandboxRunPayload) {
    running.value = true
    sandboxError.value = ''
    try {
      const response = await fetch('/api/provider/sandbox-run', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      if (!response.ok) throw new Error('Provider sandbox API unavailable')
      sandboxResult.value = await response.json() as ProviderResponse
      await load(sandboxResult.value.traceId, payload.taskType)
      source.value = 'api'
    } catch {
      sandboxError.value = '本地 Provider sandbox API 不可用，页面保持 demo snapshot。'
      source.value = 'fallback'
    } finally {
      running.value = false
    }
  }

  async function runRealDryRun(payload: ProviderRealDryRunPayload) {
    realDryRunning.value = true
    realDryRunError.value = ''
    try {
      const response = await fetch('/api/provider/real-dry-run', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      if (!response.ok) throw new Error('Provider real dry-run API unavailable')
      realDryRunResult.value = await response.json() as ProviderRealDryRunResult
      await load(realDryRunResult.value.runId, payload.taskType)
      source.value = 'api'
    } catch {
      realDryRunError.value = '本地 Real Provider dry-run API 不可用，或当前角色无权限。'
      source.value = 'fallback'
    } finally {
      realDryRunning.value = false
    }
  }

  async function loadContract(taskType: string) {
    try {
      const response = await fetch(`/api/provider/contracts/${taskType}`)
      if (!response.ok) throw new Error('Provider contract API unavailable')
      selectedContract.value = await response.json() as ProviderPromptContract
      source.value = 'api'
    } catch {
      selectedContract.value = providerContractDetailFallback
      source.value = 'fallback'
    }
  }

  async function validateResponse(payload: ProviderValidationPayload) {
    validating.value = true
    validationError.value = ''
    try {
      const response = await fetch('/api/provider/validate-response', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      if (!response.ok) throw new Error('Provider validation API unavailable')
      validationResult.value = await response.json() as ProviderValidatedResult
      source.value = 'api'
      await loadContract(payload.taskType)
    } catch {
      validationError.value = '本地 Provider validation API 不可用，页面保持 demo snapshot。'
      validationResult.value = providerValidationFallback
      source.value = 'fallback'
    } finally {
      validating.value = false
    }
  }

  onMounted(load)

  return {
    settings,
    configCheck,
    contracts,
    selectedContract,
    traceIndex,
    traceRun,
    sandboxResult,
    realDryRunResult,
    validationResult,
    permissionAuditEvents,
    sandboxError,
    realDryRunError,
    validationError,
    loading,
    running,
    realDryRunning,
    validating,
    source,
    reload: load,
    runSandbox,
    runRealDryRun,
    loadContract,
    validateResponse,
  }
}
