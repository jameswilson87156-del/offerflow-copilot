import { onMounted, shallowRef } from 'vue'

export function useProviderStatus() {
  const source = shallowRef<'api' | 'fallback'>('fallback')

  onMounted(async () => {
    try {
      const response = await fetch('/api/provider/status')
      if (!response.ok) throw new Error('Local API unavailable')
      await response.json()
      source.value = 'api'
    } catch {
      source.value = 'fallback'
    }
  })

  return { source }
}
