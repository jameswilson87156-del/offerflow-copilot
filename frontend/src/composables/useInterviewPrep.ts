import { onMounted, shallowRef } from 'vue'
import { interviewPrepFallback } from '../data/workflow'
import type { InterviewPrepData } from '../types'

export function useInterviewPrep() {
  const prep = shallowRef<InterviewPrepData>(interviewPrepFallback)
  const loading = shallowRef(true)
  const source = shallowRef<'api' | 'fallback'>('fallback')

  async function load() {
    loading.value = true
    try {
      const response = await fetch('/api/interview-prep/demo')
      if (!response.ok) throw new Error('Local interview prep API unavailable')
      prep.value = await response.json() as InterviewPrepData
      source.value = 'api'
    } catch {
      prep.value = interviewPrepFallback
      source.value = 'fallback'
    } finally {
      loading.value = false
    }
  }

  onMounted(load)

  return { prep, loading, source, reload: load }
}
