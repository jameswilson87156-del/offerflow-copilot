import { onMounted, shallowRef } from 'vue'
import {
  providerConfigCheckFallback,
  providerSettingsFallback,
  providerTraceIndexFallback,
  providerTraceRunFallback,
} from '../data/providerTrace'
import type {
  ProviderConfigCheck,
  ProviderResponse,
  ProviderSandboxRunPayload,
  ProviderSettingsData,
  ProviderTraceIndex,
  ProviderTraceRun,
} from '../types'

export function useProviderTrace() {
  const settings = shallowRef<ProviderSettingsData>(providerSettingsFallback)
  const configCheck = shallowRef<ProviderConfigCheck>(providerConfigCheckFallback)
  const traceIndex = shallowRef<ProviderTraceIndex>(providerTraceIndexFallback)
  const traceRun = shallowRef<ProviderTraceRun>(providerTraceRunFallback)
  const sandboxResult = shallowRef<ProviderResponse | null>(null)
  const sandboxError = shallowRef('')
  const loading = shallowRef(true)
  const running = shallowRef(false)
  const source = shallowRef<'api' | 'fallback'>('fallback')

  async function load(preferredRunId?: string) {
    loading.value = true
    try {
      const [settingsResponse, configCheckResponse, tracesResponse] = await Promise.all([
        fetch('/api/provider/settings'),
        fetch('/api/provider/config-check'),
        fetch('/api/provider/traces'),
      ])
      if (!settingsResponse.ok || !configCheckResponse.ok || !tracesResponse.ok) throw new Error('Local provider trace API unavailable')

      const nextSettings = await settingsResponse.json() as ProviderSettingsData
      const nextConfigCheck = await configCheckResponse.json() as ProviderConfigCheck
      const nextTraceIndex = await tracesResponse.json() as ProviderTraceIndex
      const runId = preferredRunId ?? nextTraceIndex.items[0]?.runId ?? providerTraceRunFallback.runId
      const traceResponse = await fetch(`/api/provider/traces/${runId}`)
      if (!traceResponse.ok) throw new Error('Local provider run API unavailable')

      settings.value = nextSettings
      configCheck.value = nextConfigCheck
      traceIndex.value = nextTraceIndex
      traceRun.value = await traceResponse.json() as ProviderTraceRun
      source.value = 'api'
    } catch {
      settings.value = providerSettingsFallback
      configCheck.value = providerConfigCheckFallback
      traceIndex.value = providerTraceIndexFallback
      traceRun.value = providerTraceRunFallback
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
      await load(sandboxResult.value.traceId)
      source.value = 'api'
    } catch {
      sandboxError.value = '本地 Provider sandbox API 不可用，页面保持 demo snapshot。'
      source.value = 'fallback'
    } finally {
      running.value = false
    }
  }

  onMounted(load)

  return {
    settings,
    configCheck,
    traceIndex,
    traceRun,
    sandboxResult,
    sandboxError,
    loading,
    running,
    source,
    reload: load,
    runSandbox,
  }
}
