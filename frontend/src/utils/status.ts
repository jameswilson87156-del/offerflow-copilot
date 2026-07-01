const STATUS_LABELS: Record<string, string> = {
  DRAFT: '草稿',
  IN_REVIEW: '复核中',
  CONFIRMED: '已确认',
  RETURNED: '已退回',
  RISK_FLAGGED: '风险标记',
  ARCHIVED: '已归档',
}

export function statusKey(status: string) {
  return status.trim().replace(/([a-z])([A-Z])/g, '$1_$2').replace(/[\s-]+/g, '_').toUpperCase()
}

export function statusLabel(status: string) {
  return STATUS_LABELS[statusKey(status)] ?? status
}

export function statusClass(status: string) {
  return statusKey(status).toLowerCase().replace(/_/g, '-')
}

export function isReadonlyStatus(status: string) {
  return ['RETURNED', 'RISK_FLAGGED', 'ARCHIVED'].includes(statusKey(status))
}

export function isClosedReviewStatus(status: string) {
  return ['CONFIRMED', 'RETURNED', 'RISK_FLAGGED', 'ARCHIVED'].includes(statusKey(status))
}
