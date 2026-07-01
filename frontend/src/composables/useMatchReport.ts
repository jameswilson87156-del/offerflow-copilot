import { onMounted, shallowRef } from 'vue'
import { matchReportFallback } from '../data/workflow'
import type { MatchReportData } from '../types'

export function useMatchReport() {
  const report = shallowRef<MatchReportData>(matchReportFallback)
  const loading = shallowRef(true)
  const source = shallowRef<'api' | 'fallback'>('fallback')

  async function load() {
    loading.value = true
    try {
      const response = await fetch('/api/match-report/demo')
      if (!response.ok) throw new Error('Local match report API unavailable')
      report.value = await response.json() as MatchReportData
      source.value = 'api'
    } catch {
      report.value = matchReportFallback
      source.value = 'fallback'
    } finally {
      loading.value = false
    }
  }

  onMounted(load)

  return { report, loading, source, reload: load }
}
