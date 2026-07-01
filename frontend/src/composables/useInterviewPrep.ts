import { onMounted, shallowRef } from 'vue'
import { interviewPrepFallback } from '../data/workflow'
import type { CopyPermissionAuditEvent, CopyPermissionResult, InterviewPrepData } from '../types'

const COPY_BOUNDARY_NOTICE = 'Schema Validate 与 Risk Guard 通过后仍需 Human Review Confirmed；Draft/In Review/Returned/Risk Flagged/Archived 不可复制。当前为 demo/local-rule/no-op，不承诺 Offer 结果，也不是实时面试辅助工具。'

function normalizeStatus(value: string) {
  const normalized = value.replace(/\s+/g, '_').toUpperCase()
  if (normalized.includes('CONFIRMED')) return 'CONFIRMED'
  if (normalized.includes('IN_REVIEW')) return 'IN_REVIEW'
  if (normalized.includes('RETURN')) return 'RETURNED'
  if (normalized.includes('RISK')) return 'RISK_FLAGGED'
  if (normalized.includes('ARCHIVE')) return 'ARCHIVED'
  return 'DRAFT'
}

function displayStatus(status: string) {
  return ({
    CONFIRMED: 'Confirmed',
    IN_REVIEW: 'In Review',
    RETURNED: 'Returned',
    RISK_FLAGGED: 'Risk Flagged',
    ARCHIVED: 'Archived',
    DRAFT: 'Draft',
  } as Record<string, string>)[status] ?? 'Draft'
}

function deriveCopyPermission(prep: InterviewPrepData): CopyPermissionResult {
  const targetStatus = normalizeStatus(prep.reviewStatus)
  const confirmed = targetStatus === 'CONFIRMED'
  return {
    allowed: confirmed,
    reason: confirmed ? '已通过人工复核，可复制使用。' : '需要人工复核',
    targetType: 'INTERVIEW_PREP',
    targetId: prep.id,
    targetStatus,
    humanReviewStatus: displayStatus(targetStatus),
    schemaValidated: true,
    riskGuardPassed: targetStatus !== 'RISK_FLAGGED',
    confirmed,
    boundaryNotice: COPY_BOUNDARY_NOTICE,
    auditEventId: '',
    copyText: '',
  }
}

function prepCopyText(prep: InterviewPrepData) {
  return [
    `面试准备：${prep.jobTitle}`,
    `状态：${displayStatus(normalizeStatus(prep.reviewStatus))}`,
    `定位：${prep.positioningNotice}`,
    `STAR：${prep.starDraft.situation} / ${prep.starDraft.task} / ${prep.starDraft.action} / ${prep.starDraft.result}`,
    '说明：仅用于面试前准备与复盘，不用于实时面试辅助，不承诺 Offer 结果。',
  ].join('\n')
}

export function useInterviewPrep() {
  const prep = shallowRef<InterviewPrepData>(interviewPrepFallback)
  const copyPermission = shallowRef<CopyPermissionResult>(deriveCopyPermission(interviewPrepFallback))
  const copyAuditEvents = shallowRef<CopyPermissionAuditEvent[]>([])
  const actionBusy = shallowRef(false)
  const actionMessage = shallowRef('面试准备材料为 Draft，人工确认前不可复制为正式建议。')
  const loading = shallowRef(true)
  const source = shallowRef<'api' | 'fallback'>('fallback')

  async function load() {
    loading.value = true
    try {
      const response = await fetch('/api/interview-prep/demo')
      if (!response.ok) throw new Error('Local interview prep API unavailable')
      prep.value = await response.json() as InterviewPrepData
      copyPermission.value = deriveCopyPermission(prep.value)
      source.value = 'api'
      await loadCopyAudit(prep.value.id)
    } catch {
      prep.value = interviewPrepFallback
      copyPermission.value = deriveCopyPermission(interviewPrepFallback)
      copyAuditEvents.value = []
      source.value = 'fallback'
    } finally {
      loading.value = false
    }
  }

  async function loadCopyAudit(targetId = prep.value.id) {
    try {
      const response = await fetch(`/api/copy-permissions/audit-events?targetType=INTERVIEW_PREP&targetId=${encodeURIComponent(targetId)}`)
      if (!response.ok) throw new Error('Local copy permission audit unavailable')
      copyAuditEvents.value = await response.json() as CopyPermissionAuditEvent[]
    } catch {
      copyAuditEvents.value = []
    }
  }

  async function checkCopyPermission() {
    actionBusy.value = true
    try {
      const response = await fetch('/api/copy-permissions/check', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          targetType: 'INTERVIEW_PREP',
          targetId: prep.value.id,
          actor: 'demo-reviewer',
          actorRole: 'Human reviewer',
          requestedText: prepCopyText(prep.value),
          schemaVersion: 'interview-prep-copy-schema-v1',
          promptVersion: 'interview-prep-prompt-v1',
        }),
      })
      if (!response.ok) throw new Error('Copy permission API unavailable')
      copyPermission.value = await response.json() as CopyPermissionResult
      await loadCopyAudit(prep.value.id)
      actionMessage.value = copyPermission.value.allowed ? '复制门禁已通过。' : copyPermission.value.reason
      return copyPermission.value
    } catch {
      copyPermission.value = deriveCopyPermission(prep.value)
      actionMessage.value = '本地 Copy Permission API 不可用，页面保持 demo snapshot。'
      return copyPermission.value
    } finally {
      actionBusy.value = false
    }
  }

  async function copyConfirmedPrep() {
    if (!copyPermission.value.allowed) {
      actionMessage.value = '面试准备材料尚未 Human Review Confirmed，不能复制为正式建议。'
      return false
    }
    try {
      await navigator.clipboard.writeText(prepCopyText(prep.value))
      actionMessage.value = '已复制 Confirmed 面试准备摘要。'
      return true
    } catch {
      actionMessage.value = '浏览器未开放剪贴板权限，请先检查复制许可。'
      return false
    }
  }

  onMounted(load)

  return {
    prep,
    copyPermission,
    copyAuditEvents,
    actionBusy,
    actionMessage,
    loading,
    source,
    reload: load,
    checkCopyPermission,
    copyConfirmedPrep,
  }
}
