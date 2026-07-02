import { onMounted, shallowRef } from 'vue'
import { humanReviewDetailsFallback, humanReviewFallback } from '../data/reviews'
import { useLocalActor } from './useLocalActor'
import type { HumanReviewAuditEvent, HumanReviewCenterData, HumanReviewDetail } from '../types'

export function useHumanReviews() {
  const { actorContext, withActor } = useLocalActor()
  const center = shallowRef<HumanReviewCenterData>(humanReviewFallback)
  const details = shallowRef<Record<string, HumanReviewDetail>>({ ...humanReviewDetailsFallback })
  const loading = shallowRef(true)
  const source = shallowRef<'api' | 'fallback'>('fallback')

  async function load() {
    loading.value = true
    try {
      const response = await fetch('/api/reviews')
      if (!response.ok) throw new Error('Local review API unavailable')
      center.value = await response.json() as HumanReviewCenterData
      source.value = 'api'
    } catch {
      center.value = humanReviewFallback
      details.value = { ...humanReviewDetailsFallback }
      source.value = 'fallback'
    } finally {
      loading.value = false
    }
  }

  async function loadDetail(id: string) {
    if (details.value[id] && source.value === 'fallback') return details.value[id]
    try {
      const response = await fetch(`/api/reviews/${id}`)
      if (!response.ok) throw new Error('Local review detail unavailable')
      const detail = await response.json() as HumanReviewDetail
      details.value = { ...details.value, [id]: detail }
      return detail
    } catch {
      const fallback = humanReviewDetailsFallback[id] ?? Object.values(humanReviewDetailsFallback)[0]
      details.value = { ...details.value, [id]: fallback }
      return fallback
    }
  }

  async function reviewAction(id: string, action: 'confirm' | 'return' | 'flag-risk', note: string) {
    try {
      const response = await fetch(`/api/reviews/${id}/${action}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(withActor({ humanNote: note })),
      })
      if (!response.ok) throw new ActionRequestError(await errorMessage(response, 'Review action unavailable'))
      const detail = await response.json() as HumanReviewDetail
      details.value = { ...details.value, [id]: detail }
      await load()
      return detail
    } catch (error) {
      if (error instanceof ActionRequestError) throw error
      const current = details.value[id] ?? humanReviewDetailsFallback[id]
      const nextStatus = action === 'confirm' ? 'Confirmed' : action === 'return' ? 'Returned' : 'Risk Flagged'
      const nextRiskLevel = action === 'flag-risk' ? '高风险' : current.riskLevel
      const event: HumanReviewAuditEvent = {
        id: `fallback-action-${Date.now()}`,
        reviewId: id,
        action: action === 'confirm' ? 'CONFIRM' : action === 'return' ? 'RETURN' : 'FLAG_RISK',
        actionLabel: action === 'confirm' ? '确认可用' : action === 'return' ? '退回修改' : '标记风险',
        previousStatus: current.status,
        nextStatus,
        previousRiskLevel: current.riskLevel,
        nextRiskLevel,
        actor: actorContext.value.actor,
        actorRole: actorContext.value.actorRole,
        humanNote: note || current.humanNote,
        traceId: current.traceId,
        traceHash: `audit-${current.traceId.slice(-4).toLowerCase()}-${id.slice(-4)}`,
        createdAt: '2026-07-01 14:35',
      }
      const updated: HumanReviewDetail = {
        ...current,
        status: nextStatus,
        riskLevel: nextRiskLevel,
        copyAllowed: action === 'confirm',
        humanNote: note || current.humanNote,
        auditTrail: [...(current.auditTrail ?? []), event],
        lastAction: action === 'confirm'
          ? '人工已确认，可复制使用'
          : action === 'return'
            ? '已退回修改，复制仍被禁用'
            : '已标记风险，等待重新生成或人工改写',
      }
      details.value = { ...details.value, [id]: updated }
      return updated
    }
  }

  onMounted(load)

  return { center, details, loading, source, reload: load, loadDetail, reviewAction }
}

async function errorMessage(response: Response, fallback: string) {
  try {
    const body = await response.json() as { reason?: string }
    return body.reason ?? fallback
  } catch {
    return fallback
  }
}

class ActionRequestError extends Error {}
