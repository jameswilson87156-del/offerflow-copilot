import { onMounted, shallowRef } from 'vue'
import { evidenceCoverageFallback, evidenceLibraryFallback } from '../data/evidence'
import type { EvidenceAuditEvent, EvidenceCoverageData, EvidenceItem, EvidenceItemDetail, EvidenceLibraryData, EvidenceMutationPayload } from '../types'

const demoActor = {
  actor: 'demo-evidence-editor',
  actorRole: 'Evidence reviewer',
}

export function useEvidenceLibrary() {
  const library = shallowRef<EvidenceLibraryData>(evidenceLibraryFallback)
  const coverage = shallowRef<EvidenceCoverageData>(evidenceCoverageFallback)
  const detail = shallowRef<EvidenceItemDetail | null>(null)
  const loading = shallowRef(true)
  const detailLoading = shallowRef(false)
  const actionLoading = shallowRef(false)
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

  async function loadDetail(id: string) {
    detailLoading.value = true
    try {
      const response = await fetch(`/api/evidence/${id}`)
      if (!response.ok) throw new Error('Local evidence detail API unavailable')
      detail.value = await response.json() as EvidenceItemDetail
      source.value = 'api'
    } catch {
      const item = library.value.items.find((candidate) => candidate.id === id) ?? library.value.items[0]
      detail.value = fallbackDetail(item)
      source.value = 'fallback'
    } finally {
      detailLoading.value = false
    }
  }

  async function createEvidence(payload: EvidenceMutationPayload) {
    return mutate('/api/evidence', 'POST', payload)
  }

  async function updateEvidence(id: string, payload: EvidenceMutationPayload) {
    return mutate(`/api/evidence/${id}`, 'PUT', payload)
  }

  async function confirmEvidence(id: string, payload: EvidenceMutationPayload) {
    return mutate(`/api/evidence/${id}/confirm`, 'POST', payload)
  }

  async function returnToDraft(id: string, payload: EvidenceMutationPayload) {
    return mutate(`/api/evidence/${id}/return-to-draft`, 'POST', payload)
  }

  async function archiveEvidence(id: string, payload: EvidenceMutationPayload) {
    return mutate(`/api/evidence/${id}/archive`, 'POST', payload)
  }

  async function restoreEvidence(id: string, payload: EvidenceMutationPayload) {
    return mutate(`/api/evidence/${id}/restore`, 'POST', payload)
  }

  async function mutate(url: string, method: 'POST' | 'PUT', payload: EvidenceMutationPayload) {
    actionLoading.value = true
    const body = { ...demoActor, ...payload }
    try {
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
      if (!response.ok) throw new Error('Evidence mutation API unavailable')
      detail.value = await response.json() as EvidenceItemDetail
      await load()
      return detail.value
    } finally {
      actionLoading.value = false
    }
  }

  onMounted(load)

  return {
    library,
    coverage,
    detail,
    loading,
    detailLoading,
    actionLoading,
    source,
    reload: load,
    loadDetail,
    createEvidence,
    updateEvidence,
    confirmEvidence,
    returnToDraft,
    archiveEvidence,
    restoreEvidence,
  }
}

function fallbackDetail(item: EvidenceItem): EvidenceItemDetail {
  return {
    mode: 'mock/local-rule',
    item,
    auditTrail: [fallbackAudit(item)],
    boundaryNotice: 'Fallback snapshot 仅用于本地演示；请以 LOCAL API 返回的审计历史为准。',
  }
}

function fallbackAudit(item: EvidenceItem): EvidenceAuditEvent {
  return {
    id: `fallback-audit-${item.id}`,
    evidenceId: item.id,
    action: item.status === 'Confirmed' ? 'CONFIRM' : 'UPDATE_DRAFT',
    actionLabel: item.status === 'Confirmed' ? '确认证据' : '保存草稿',
    previousStatus: 'Draft',
    nextStatus: item.status,
    actor: 'demo-evidence-editor',
    actorRole: 'Evidence reviewer',
    changedFields: ['status'],
    beforeSnapshot: emptySnapshot(),
    afterSnapshot: {
      projectName: item.projectName,
      summary: item.summary,
      skills: item.detail.relatedSkills,
      abilityTags: item.abilityTags,
      evidenceSources: item.evidenceSources,
      strength: item.credibility,
      matchableRequirements: item.matchableRequirements,
      boundaryNote: item.detail.riskBoundaries.join(' / '),
      riskBoundaries: item.detail.riskBoundaries,
    },
    humanNote: 'Fallback demo audit event',
    createdAt: `${item.updatedAt} 12:00`,
  }
}

function emptySnapshot() {
  return {
    projectName: null,
    summary: null,
    skills: [],
    abilityTags: [],
    evidenceSources: [],
    strength: null,
    matchableRequirements: [],
    boundaryNote: null,
    riskBoundaries: [],
  }
}
