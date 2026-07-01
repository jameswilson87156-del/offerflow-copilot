import { onMounted, shallowRef } from 'vue'
import { evidenceCoverageFallback, evidenceLibraryFallback } from '../data/evidence'
import type { EvidenceCoverageData, EvidenceLibraryData } from '../types'

export function useEvidenceLibrary() {
  const library = shallowRef<EvidenceLibraryData>(evidenceLibraryFallback)
  const coverage = shallowRef<EvidenceCoverageData>(evidenceCoverageFallback)
  const loading = shallowRef(true)
  const source = shallowRef<'api' | 'fallback'>('fallback')

  async function load() {
    loading.value = true
    try {
      const [libraryResponse, coverageResponse] = await Promise.all([
        fetch('/api/evidence/library'),
        fetch('/api/evidence/coverage'),
      ])
      if (!libraryResponse.ok || !coverageResponse.ok) throw new Error('Local evidence API unavailable')
      library.value = await libraryResponse.json() as EvidenceLibraryData
      coverage.value = await coverageResponse.json() as EvidenceCoverageData
      source.value = 'api'
    } catch {
      library.value = evidenceLibraryFallback
      coverage.value = evidenceCoverageFallback
      source.value = 'fallback'
    } finally {
      loading.value = false
    }
  }

  onMounted(load)

  return { library, coverage, loading, source, reload: load }
}
