import { onMounted, shallowRef } from 'vue'
import { applicationTrackerFallback } from '../data/workflow'
import type { ApplicationTrackerData } from '../types'

export function useApplicationTracker() {
  const tracker = shallowRef<ApplicationTrackerData>(applicationTrackerFallback)
  const loading = shallowRef(true)
  const source = shallowRef<'api' | 'fallback'>('fallback')

  async function load() {
    loading.value = true
    try {
      const response = await fetch('/api/applications')
      if (!response.ok) throw new Error('Local application tracker API unavailable')
      tracker.value = await response.json() as ApplicationTrackerData
      source.value = 'api'
    } catch {
      tracker.value = applicationTrackerFallback
      source.value = 'fallback'
    } finally {
      loading.value = false
    }
  }

  onMounted(load)

  return { tracker, loading, source, reload: load }
}
