import { onMounted, shallowRef } from 'vue'
import { matchReportFallback } from '../data/workflow'
import type { MatchReportAuditEvent, MatchReportData, MatchReportVersionSummary } from '../types'

const fallbackVersions: MatchReportVersionSummary[] = [
  {
    id: matchReportFallback.versionId,
    reportId: matchReportFallback.reportId,
    jobId: matchReportFallback.jobId,
    parseVersionId: matchReportFallback.parseVersionId,
    parseVersionNo: matchReportFallback.parseVersionNo,
    versionNo: matchReportFallback.versionNo,
    score: matchReportFallback.summary.totalScore,
    status: matchReportFallback.status,
    providerMode: matchReportFallback.providerMode,
    promptVersion: matchReportFallback.promptVersion,
    traceId: matchReportFallback.traceId,
    humanReviewId: matchReportFallback.humanReviewId,
    humanReviewStatus: matchReportFallback.humanReviewStatus,
    evidenceBindingCount: matchReportFallback.evidenceBindingCount,
    createdAt: matchReportFallback.createdAt,
  },
]

const fallbackAuditEvents: MatchReportAuditEvent[] = [
  {
    id: 'match-audit-seed-generate',
    reportVersionId: matchReportFallback.versionId,
    action: 'GENERATE_LOCAL_RULE',
    actionLabel: 'local-rule 生成',
    previousStatus: 'NONE',
    nextStatus: 'DRAFT',
    actor: 'local-rule report generator',
    actorRole: 'System',
    changedFields: ['score', 'summary', 'evidenceRefs'],
    humanNote: '使用 local-rule scoring 生成 seed 匹配报告版本。',
    traceId: matchReportFallback.traceId,
    createdAt: matchReportFallback.createdAt,
  },
  {
    id: 'match-audit-seed-draft',
    reportVersionId: matchReportFallback.versionId,
    action: 'CREATE_DRAFT',
    actionLabel: '创建 Draft',
    previousStatus: 'NONE',
    nextStatus: 'DRAFT',
    actor: 'local-rule report generator',
    actorRole: 'System',
    changedFields: ['status', 'humanReviewId'],
    humanNote: '创建 Draft 报告版本并关联 Human Review seed item。',
    traceId: matchReportFallback.traceId,
    createdAt: matchReportFallback.createdAt,
  },
]

export function useMatchReport() {
  const report = shallowRef<MatchReportData>(matchReportFallback)
  const versions = shallowRef<MatchReportVersionSummary[]>(fallbackVersions)
  const auditEvents = shallowRef<MatchReportAuditEvent[]>(fallbackAuditEvents)
  const loading = shallowRef(true)
  const actionBusy = shallowRef(false)
  const actionMessage = shallowRef('匹配报告为 Draft，人工复核前不可复制或外发。')
  const source = shallowRef<'api' | 'fallback'>('fallback')

  async function load() {
    loading.value = true
    try {
      const response = await fetch('/api/match-report/demo')
      if (!response.ok) throw new Error('Local match report API unavailable')
      const detail = await response.json() as MatchReportData
      report.value = detail
      source.value = 'api'
      await Promise.all([loadVersions(detail.jobId), loadAudit(detail.versionId)])
    } catch {
      report.value = matchReportFallback
      versions.value = fallbackVersions
      auditEvents.value = fallbackAuditEvents
      source.value = 'fallback'
    } finally {
      loading.value = false
    }
  }

  async function loadVersion(versionId: string) {
    loading.value = true
    try {
      const response = await fetch(`/api/match-reports/${versionId}`)
      if (!response.ok) throw new Error('Local match report version unavailable')
      const detail = await response.json() as MatchReportData
      report.value = detail
      source.value = 'api'
      await Promise.all([loadVersions(detail.jobId), loadAudit(detail.versionId)])
      return detail
    } finally {
      loading.value = false
    }
  }

  async function loadVersions(jobId = report.value.jobId) {
    try {
      const response = await fetch(`/api/jobs/${jobId}/match-reports`)
      if (!response.ok) throw new Error('Local match report versions unavailable')
      versions.value = await response.json() as MatchReportVersionSummary[]
    } catch {
      versions.value = fallbackVersions
    }
  }

  async function loadAudit(versionId = report.value.versionId) {
    try {
      const response = await fetch(`/api/match-reports/${versionId}/audit-events`)
      if (!response.ok) throw new Error('Local match report audit unavailable')
      auditEvents.value = await response.json() as MatchReportAuditEvent[]
    } catch {
      auditEvents.value = fallbackAuditEvents
    }
  }

  async function generateVersion() {
    actionBusy.value = true
    try {
      const response = await fetch(`/api/jobs/${report.value.jobId}/match-reports/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          actor: 'demo-report-generator',
          actorRole: 'System',
          humanNote: '从 Match Report 页面生成新版本。',
        }),
      })
      if (!response.ok) throw new Error('Generate match report version unavailable')
      const detail = await response.json() as MatchReportData
      report.value = detail
      source.value = 'api'
      await Promise.all([loadVersions(detail.jobId), loadAudit(detail.versionId)])
      actionMessage.value = `已生成 v${detail.versionNo}，并创建 Human Review item。`
      return detail
    } catch {
      actionMessage.value = '当前未连接本地 API，无法生成新版本。'
      return report.value
    } finally {
      actionBusy.value = false
    }
  }

  async function sendToReview() {
    actionBusy.value = true
    try {
      const detail = await mutateCurrentVersion('send-to-review', '已送入人工复核，确认前不可复制或外发。')
      return detail
    } finally {
      actionBusy.value = false
    }
  }

  async function archiveVersion() {
    actionBusy.value = true
    try {
      const detail = await mutateCurrentVersion('archive', '当前版本已归档，不作为可用建议。')
      return detail
    } finally {
      actionBusy.value = false
    }
  }

  async function mutateCurrentVersion(action: 'send-to-review' | 'archive', successMessage: string) {
    try {
      const response = await fetch(`/api/match-reports/${report.value.versionId}/${action}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          actor: 'demo-reviewer',
          actorRole: 'Human reviewer',
          humanNote: successMessage,
        }),
      })
      if (!response.ok) throw new Error('Match report action unavailable')
      const detail = await response.json() as MatchReportData
      report.value = detail
      source.value = 'api'
      await Promise.all([loadVersions(detail.jobId), loadAudit(detail.versionId)])
      actionMessage.value = successMessage
      return detail
    } catch {
      actionMessage.value = '当前未连接本地 API，状态变更未写入。'
      return report.value
    }
  }

  onMounted(load)

  return {
    report,
    versions,
    auditEvents,
    loading,
    actionBusy,
    actionMessage,
    source,
    reload: load,
    loadVersion,
    generateVersion,
    sendToReview,
    archiveVersion,
  }
}
