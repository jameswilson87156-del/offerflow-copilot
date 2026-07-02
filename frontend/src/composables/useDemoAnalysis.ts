import { computed, onMounted, shallowRef } from 'vue'
import { demoFallback, jobDetailFallback, providerFallback } from '../data/demo'
import { useLocalActor } from './useLocalActor'
import type {
  DemoAnalysis,
  EvidenceMatch,
  JobDetailData,
  JobListData,
  JobMutationPayload,
  ProviderStatus,
  RequirementGroup,
} from '../types'

export function useDemoAnalysis() {
  const { withActor } = useLocalActor()
  const analysis = shallowRef<DemoAnalysis>(demoFallback)
  const jobDetail = shallowRef<JobDetailData>(jobDetailFallback)
  const provider = shallowRef<ProviderStatus>(providerFallback)
  const source = shallowRef<'api' | 'fallback'>('fallback')
  const loading = shallowRef(true)
  const actionLoading = shallowRef<'save' | 'parse' | 'bind' | null>(null)
  const error = shallowRef('')
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

  async function loadAnalysisOnly() {
    const response = await fetch('/api/jobs/demo-analysis')
    if (!response.ok) throw new Error('Demo analysis unavailable')
    analysis.value = await response.json() as DemoAnalysis
  }

  async function loadJobDetail() {
    const listResponse = await fetch('/api/jobs')
    if (!listResponse.ok) throw new Error('Job list unavailable')
    const list = await listResponse.json() as JobListData
    const firstJob = list.items[0]
    if (!firstJob) {
      jobDetail.value = jobDetailFallback
      return
    }
    const detailResponse = await fetch(`/api/jobs/${firstJob.id}`)
    if (!detailResponse.ok) throw new Error('Job detail unavailable')
    jobDetail.value = await detailResponse.json() as JobDetailData
  }

  async function load() {
    loading.value = true
    error.value = ''
    try {
      const providerResponse = await fetch('/api/provider/status')
      if (!providerResponse.ok) throw new Error('Provider unavailable')
      await Promise.all([loadAnalysisOnly(), loadJobDetail()])
      provider.value = await providerResponse.json() as ProviderStatus
      source.value = 'api'
    } catch {
      analysis.value = demoFallback
      jobDetail.value = jobDetailFallback
      provider.value = providerFallback
      source.value = 'fallback'
      error.value = '本地 API 暂不可用，当前展示 fallback demo。'
    } finally {
      loading.value = false
    }
  }

  async function runMutation<T>(kind: 'save' | 'parse' | 'bind', request: () => Promise<T>) {
    actionLoading.value = kind
    error.value = ''
    try {
      const result = await request()
      await loadAnalysisOnly()
      source.value = 'api'
      return result
    } catch {
      error.value = '操作失败，请确认本地 API 已启动；本轮不会调用外部 Provider。'
      throw new Error(error.value)
    } finally {
      actionLoading.value = null
    }
  }

  async function saveJob(payload: JobMutationPayload) {
    return runMutation('save', async () => {
      const jobId = jobDetail.value.job.id
      const response = await fetch(`/api/jobs/${jobId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(withActor({ sourceType: 'MANUAL_PASTE', ...payload })),
      })
      if (!response.ok) throw new Error('Save failed')
      jobDetail.value = await response.json() as JobDetailData
      return jobDetail.value
    })
  }

  async function parseJob(humanNote = '前端触发 local-rule 重新解析。') {
    return runMutation('parse', async () => {
      const response = await fetch(`/api/jobs/${jobDetail.value.job.id}/parse`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(withActor({ humanNote })),
      })
      if (!response.ok) throw new Error('Parse failed')
      jobDetail.value = await response.json() as JobDetailData
      return jobDetail.value
    })
  }

  async function bindEvidence(humanNote = '前端触发 local-rule evidence binding。') {
    return runMutation('bind', async () => {
      const response = await fetch(`/api/jobs/${jobDetail.value.job.id}/bind-evidence`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(withActor({ humanNote })),
      })
      if (!response.ok) throw new Error('Bind failed')
      jobDetail.value = await response.json() as JobDetailData
      return jobDetail.value
    })
  }

  onMounted(load)

  return {
    analysis,
    jobDetail,
    provider,
    source,
    loading,
    actionLoading,
    error,
    searchQuery,
    requirementGroups,
    evidenceMatches,
    reload: load,
    saveJob,
    parseJob,
    bindEvidence,
  }
}
