import { computed, onMounted, shallowRef } from 'vue'
import { demoFallback, providerFallback } from '../data/demo'
import type { DemoAnalysis, EvidenceMatch, ProviderStatus, RequirementGroup } from '../types'

export function useDemoAnalysis() {
  const analysis = shallowRef<DemoAnalysis>(demoFallback)
  const provider = shallowRef<ProviderStatus>(providerFallback)
  const source = shallowRef<'api' | 'fallback'>('fallback')
  const loading = shallowRef(true)
  const searchQuery = shallowRef('')

  const normalizedQuery = computed(() => searchQuery.value.trim().toLowerCase())

  const requirementGroups = computed<RequirementGroup[]>(() => {
    if (!normalizedQuery.value) return analysis.value.requirementGroups
    return analysis.value.requirementGroups
      .map((group) => ({
        ...group,
        items: group.items.filter((item) =>
          [item.title, item.description, ...item.keywords].join(' ').toLowerCase().includes(normalizedQuery.value),
        ),
      }))
      .filter((group) => group.items.length > 0)
  })

  const evidenceMatches = computed<EvidenceMatch[]>(() => {
    if (!normalizedQuery.value) return analysis.value.evidenceMatches
    return analysis.value.evidenceMatches.filter((match) =>
      [match.requirement, match.requirementDetail, match.project, match.rationale, ...match.evidenceTypes]
        .join(' ')
        .toLowerCase()
        .includes(normalizedQuery.value),
    )
  })

  async function load() {
    loading.value = true
    try {
      const [analysisResponse, providerResponse] = await Promise.all([
        fetch('/api/jobs/demo-analysis'),
        fetch('/api/provider/status'),
      ])
      if (!analysisResponse.ok || !providerResponse.ok) throw new Error('Local API unavailable')
      analysis.value = await analysisResponse.json() as DemoAnalysis
      provider.value = await providerResponse.json() as ProviderStatus
      source.value = 'api'
    } catch {
      analysis.value = demoFallback
      provider.value = providerFallback
      source.value = 'fallback'
    } finally {
      loading.value = false
    }
  }

  onMounted(load)

  return { analysis, provider, source, loading, searchQuery, requirementGroups, evidenceMatches, reload: load }
}
