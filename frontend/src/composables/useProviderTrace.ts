import { onMounted, shallowRef } from 'vue'
import { providerSettingsFallback, providerTraceIndexFallback, providerTraceRunFallback } from '../data/providerTrace'
import type { ProviderSettingsData, ProviderTraceIndex, ProviderTraceRun } from '../types'

export function useProviderTrace() {
  const settings = shallowRef<ProviderSettingsData>(providerSettingsFallback)
  const traceIndex = shallowRef<ProviderTraceIndex>(providerTraceIndexFallback)
  const traceRun = shallowRef<ProviderTraceRun>(providerTraceRunFallback)
  const loading = shallowRef(true)
  const source = shallowRef<'api' | 'fallback'>('fallback')

  async function load() {
    loading.value = true
    try {
      const [settingsResponse, tracesResponse] = await Promise.all([
        fetch('/api/provider/settings'),
        fetch('/api/provider/traces'),
      ])
      if (!settingsResponse.ok || !tracesResponse.ok) throw new Error('Local provider trace API unavailable')

      const nextSettings = await settingsResponse.json() as ProviderSettingsData
      const nextTraceIndex = await tracesResponse.json() as ProviderTraceIndex
      const runId = nextTraceIndex.items[0]?.runId ?? providerTraceRunFallback.runId
      const traceResponse = await fetch(`/api/provider/traces/${runId}`)
      if (!traceResponse.ok) throw new Error('Local provider run API unavailable')

      settings.value = nextSettings
      traceIndex.value = nextTraceIndex
      traceRun.value = await traceResponse.json() as ProviderTraceRun
      source.value = 'api'
    } catch {
      settings.value = providerSettingsFallback
      traceIndex.value = providerTraceIndexFallback
      traceRun.value = providerTraceRunFallback
      source.value = 'fallback'
    } finally {
      loading.value = false
    }
  }

  onMounted(load)

  return { settings, traceIndex, traceRun, loading, source, reload: load }
}
