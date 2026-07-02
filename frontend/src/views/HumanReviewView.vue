<script setup lang="ts">
import { computed, shallowRef, watch } from 'vue'
import {
  AlertTriangle,
  Check,
  CheckCircle2,
  ChevronRight,
  ClipboardCheck,
  FileText,
  Flag,
  GitBranch,
  History,
  MessageSquarePlus,
  RefreshCw,
  RotateCcw,
  ShieldAlert,
  ShieldCheck,
  SlidersHorizontal,
  UserCheck,
} from 'lucide-vue-next'
import { useHumanReviews } from '../composables/useHumanReviews'
import { useLocalActor } from '../composables/useLocalActor'
import AuditEventDisclosure from '../components/AuditEventDisclosure.vue'
import { isClosedReviewStatus, statusClass, statusLabel } from '../utils/status'
import type { HumanReviewDetail, HumanReviewSummary, PermissionAction } from '../types'

const props = defineProps<{
  searchQuery: string
  selectedProvider: string
}>()

const { center, details, loading, source, reload, loadDetail, reviewAction } = useHumanReviews()
const { can, permissionReason } = useLocalActor()
const selectedId = shallowRef('review-star-mcp')
const manualNote = shallowRef('需要把“生产级”改成“作品集级”，不要声称真实用户。')
const actionBusy = shallowRef(false)
const actionMessage = shallowRef('所有 AI 输出保持草稿，人工确认前不可复制。')

const filteredItems = computed(() => {
  const query = props.searchQuery.trim().toLowerCase()
  return center.value.items.filter((item) => {
    const corpus = [
      item.title,
      item.sourcePage,
      item.riskLevel,
      item.providerMode,
      item.traceId,
      item.status,
    ].join(' ').toLowerCase()
    return !query || corpus.includes(query)
  })
})

const groupedItems = computed(() => center.value.groups
  .map((group) => ({
    ...group,
    items: filteredItems.value.filter((item) => item.group === group.key),
  }))
  .filter((group) => group.items.length > 0 || !props.searchQuery.trim()))

const selectedDetail = computed<HumanReviewDetail>(() => {
  const fallbackId = filteredItems.value[0]?.id ?? center.value.items[0]?.id ?? 'review-star-mcp'
  return details.value[selectedId.value] ?? details.value[fallbackId] ?? Object.values(details.value)[0]!
})

const highlightedSuggestion = computed(() =>
  splitRiskTerms(selectedDetail.value.aiSuggestion, selectedDetail.value.riskTerms),
)

const auditTrail = computed(() => selectedDetail.value.auditTrail ?? [])
const isReviewReadonly = computed(() => isClosedReviewStatus(selectedDetail.value.status))
const readonlyReason = computed(() => selectedDetail.value.status === 'Archived'
  ? '已归档复核项不可继续操作，请回到来源资产恢复。'
  : `${statusLabel(selectedDetail.value.status)}状态已结束；如需修改，请回到来源资产重新进入复核。`)
const auditFieldLabels: Record<string, string> = {
  status: '状态',
  riskLevel: '风险等级',
  humanNote: '人工备注',
  lastAction: '最后动作',
}

const providerNotice = computed(() => props.selectedProvider === 'local-rule'
  ? 'local-rule 审核流 · 无外部调用'
  : `${props.selectedProvider} 未配置，本页仍使用 local-rule 审核`)

const reviewActionMap: Record<'confirm' | 'return' | 'flag-risk', PermissionAction> = {
  confirm: 'REVIEW_CONFIRM',
  return: 'REVIEW_RETURN',
  'flag-risk': 'REVIEW_FLAG_RISK',
}

const reviewPermissionNote = computed(() => permissionReason('REVIEW_CONFIRM'))

watch(filteredItems, (items) => {
  if (items.length && !items.some((item) => item.id === selectedId.value)) selectedId.value = items[0].id
})

watch(selectedId, async (id) => {
  const detail = await loadDetail(id)
  manualNote.value = detail.humanNote
}, { immediate: true })

function splitRiskTerms(text: string, terms: string[]) {
  const sortedTerms = [...terms].sort((a, b) => b.length - a.length)
  const segments: Array<{ text: string; risk: boolean }> = []
  let cursor = 0

  while (cursor < text.length) {
    let nextIndex = -1
    let nextTerm = ''

    for (const term of sortedTerms) {
      const index = text.indexOf(term, cursor)
      if (index !== -1 && (nextIndex === -1 || index < nextIndex || (index === nextIndex && term.length > nextTerm.length))) {
        nextIndex = index
        nextTerm = term
      }
    }

    if (nextIndex === -1) {
      segments.push({ text: text.slice(cursor), risk: false })
      break
    }

    if (nextIndex > cursor) segments.push({ text: text.slice(cursor, nextIndex), risk: false })
    segments.push({ text: text.slice(nextIndex, nextIndex + nextTerm.length), risk: true })
    cursor = nextIndex + nextTerm.length
  }

  return segments
}

function riskClass(riskLevel: string) {
  if (riskLevel.includes('高')) return 'high'
  if (riskLevel.includes('中')) return 'medium'
  return 'low'
}

function formatRiskLevel(riskLevel: string) {
  return riskLevel.endsWith('风险') ? riskLevel : `${riskLevel}风险`
}

function sourceLabel(item: HumanReviewSummary) {
  return `${item.sourcePage} · ${item.providerMode}`
}

function auditClass(action: string) {
  if (action === 'CONFIRM') return 'confirm'
  if (action === 'RETURN') return 'return'
  if (action === 'FLAG_RISK') return 'flag'
  if (action === 'AUTO_RISK_GUARD') return 'guard'
  return 'note'
}

async function applyAction(action: 'confirm' | 'return' | 'flag-risk') {
  const permission = reviewActionMap[action]
  if (!can(permission)) {
    actionMessage.value = permissionReason(permission)
    return
  }
  actionBusy.value = true
  try {
    const detail = await reviewAction(selectedDetail.value.id, action, manualNote.value)
    actionMessage.value = detail.lastAction
  } catch (error) {
    actionMessage.value = error instanceof Error ? error.message : permissionReason(permission)
  } finally {
    actionBusy.value = false
  }
}

function rememberNote() {
  if (!can('REVIEW_RETURN')) {
    actionMessage.value = reviewPermissionNote.value
    return
  }
  actionMessage.value = '人工备注已保留在当前复核草稿中，等待确认、退回或标记风险。'
}

function actionDisabled(action: 'confirm' | 'return' | 'flag-risk') {
  return actionBusy.value || isReviewReadonly.value || !can(reviewActionMap[action])
}

function actionTitle(action: 'confirm' | 'return' | 'flag-risk') {
  return can(reviewActionMap[action]) ? '' : permissionReason(reviewActionMap[action])
}
</script>

<template>
  <main class="main-content human-review-page" :class="{ 'readonly-state': isReviewReadonly }">
    <section class="page-intro human-review-intro">
      <div class="intro-copy">
        <div class="breadcrumbs"><span>人工复核</span><ChevronRight :size="13" /><strong>Human Review Center</strong></div>
        <h1>人工复核中心 <span class="title-mark human-title-mark"><ClipboardCheck :size="20" /></span></h1>
        <p>所有 AI 输出在复制或投递前必须经过人工确认，避免夸大、误导和无证据表述。</p>
      </div>
      <div class="intro-actions">
        <div class="mode-note human-mode" :class="{ warning: props.selectedProvider !== 'local-rule' }">
          <span><Check v-if="props.selectedProvider === 'local-rule'" :size="13" /><SlidersHorizontal v-else :size="13" /></span>
          {{ providerNotice }}
        </div>
        <button type="button" class="secondary-button" :disabled="loading" @click="reload">
          <RefreshCw :size="15" :class="{ spinning: loading }" />刷新队列
        </button>
      </div>
    </section>

    <section class="human-review-context" aria-label="人工复核状态">
      <div class="human-context-main">
        <span class="human-ready-icon"><ShieldAlert :size="16" /></span>
        <strong>{{ center.pendingReviewCount }} 个待复核</strong>
        <span>当前状态：草稿，需要人工确认</span>
      </div>
      <div class="human-context-meta">
        <span :class="source">{{ source === 'api' ? 'LOCAL API LIVE' : 'DEMO SNAPSHOT' }}</span>
        <small>mock/local-rule · 不接真实 LLM · 不接招聘平台 API</small>
      </div>
    </section>

    <div class="human-review-layout">
      <section class="panel review-queue-panel">
        <header class="panel-header human-panel-header">
          <div>
            <span class="panel-kicker">REVIEW QUEUE</span>
            <h2>审核队列</h2>
          </div>
          <span class="count-chip">{{ filteredItems.length }} 项</span>
        </header>

        <div class="review-queue-groups">
          <section v-for="group in groupedItems" :key="group.key" class="queue-group">
            <h3>
              <span :class="['queue-group-dot', group.key]" />
              {{ group.label }}
              <em>{{ group.items.length || group.count }}</em>
            </h3>
            <button
              v-for="item in group.items"
              :key="item.id"
              type="button"
              class="queue-item"
              :class="{ selected: item.id === selectedDetail.id }"
              @click="selectedId = item.id"
            >
              <span class="queue-title">{{ item.title }}</span>
              <span class="queue-meta">
                <em :class="['risk-pill', riskClass(item.riskLevel)]">{{ formatRiskLevel(item.riskLevel) }}</em>
                <small>{{ sourceLabel(item) }}</small>
              </span>
              <span class="queue-trace">Trace: {{ item.traceId }}</span>
              <span :class="['status-chip', statusClass(item.status)]">{{ statusLabel(item.status) }}</span>
            </button>
          </section>
        </div>
      </section>

      <section class="panel review-detail-panel">
        <header class="panel-header human-panel-header">
          <div>
            <span class="panel-kicker">REVIEW DETAIL</span>
            <h2>{{ selectedDetail.title }}</h2>
          </div>
          <span :class="['status-chip', statusClass(selectedDetail.status)]">{{ statusLabel(selectedDetail.status) }}</span>
        </header>

        <div class="detail-section ai-original">
          <h3><FileText :size="14" />AI 原始建议</h3>
          <p>
            <span
              v-for="(segment, index) in highlightedSuggestion"
              :key="`${segment.text}-${index}`"
              :class="{ 'risk-highlight': segment.risk }"
            >{{ segment.text }}</span>
          </p>
        </div>

        <div class="detail-grid">
          <section class="detail-section evidence-box">
            <h3><ShieldCheck :size="14" />引用的 JD 片段</h3>
            <blockquote>{{ selectedDetail.evidence.jdSnippet }}</blockquote>
            <p>{{ selectedDetail.evidence.evidenceNote }}</p>
          </section>

          <section class="detail-section evidence-box">
            <h3><GitBranch :size="14" />引用的简历项目</h3>
            <article v-for="project in selectedDetail.evidence.resumeProjects" :key="project.name" class="review-project">
              <strong>{{ project.name }}</strong>
              <p>{{ project.excerpt }}</p>
              <span>
                <em v-for="sourceType in project.sourceTypes" :key="sourceType">{{ sourceType }}</em>
              </span>
            </article>
          </section>
        </div>

        <div class="detail-section risk-keywords">
          <h3><AlertTriangle :size="14" />风险词高亮</h3>
          <div class="risk-term-list">
            <span v-for="term in selectedDetail.riskTerms" :key="term">{{ term }}</span>
          </div>
        </div>
      </section>

      <aside class="panel review-action-panel">
        <header class="panel-header human-panel-header">
          <div>
            <span class="panel-kicker">HUMAN ACTION</span>
            <h2>人工复核操作</h2>
          </div>
          <UserCheck :size="17" class="header-icon" />
        </header>

        <div class="action-button-grid">
          <button type="button" class="review-action confirm" :disabled="actionDisabled('confirm')" :title="actionTitle('confirm')" @click="applyAction('confirm')">
            <CheckCircle2 :size="16" />确认可用
          </button>
          <button type="button" class="review-action return" :disabled="actionDisabled('return')" :title="actionTitle('return')" @click="applyAction('return')">
            <RotateCcw :size="16" />退回修改
          </button>
          <button type="button" class="review-action flag" :disabled="actionDisabled('flag-risk')" :title="actionTitle('flag-risk')" @click="applyAction('flag-risk')">
            <Flag :size="16" />标记风险
          </button>
          <button type="button" class="review-action note" :disabled="actionBusy || isReviewReadonly || !can('REVIEW_RETURN')" :title="can('REVIEW_RETURN') ? '' : reviewPermissionNote" @click="rememberNote">
            <MessageSquarePlus :size="16" />添加人工备注
          </button>
        </div>
        <p v-if="!isReviewReadonly && !can('REVIEW_CONFIRM')" class="permission-inline-note">{{ reviewPermissionNote }}</p>

        <label class="manual-note">
          <span>人工备注</span>
          <textarea v-model="manualNote" maxlength="500" :disabled="isReviewReadonly" />
          <small>{{ manualNote.length }}/500</small>
        </label>

        <dl class="review-meta-list">
          <div><dt>审核人</dt><dd>{{ selectedDetail.reviewer }}</dd></div>
          <div><dt>更新时间</dt><dd>{{ selectedDetail.updatedAt }}</dd></div>
          <div><dt>Risk Guard</dt><dd :class="riskClass(selectedDetail.riskLevel)">{{ formatRiskLevel(selectedDetail.riskLevel) }}</dd></div>
          <div><dt>Human Review</dt><dd>{{ selectedDetail.copyAllowed ? '已人工确认' : '待人工确认' }}</dd></div>
        </dl>

        <p class="action-message" :class="{ readonly: isReviewReadonly }">{{ isReviewReadonly ? readonlyReason : actionMessage }}</p>

        <section class="review-audit-card" aria-label="Review History">
          <header>
            <div>
              <span class="panel-kicker">REVIEW HISTORY</span>
              <h3><History :size="14" />审计记录</h3>
            </div>
            <em>{{ auditTrail.length }} 条</em>
          </header>

          <div class="audit-timeline">
            <AuditEventDisclosure
              v-for="event in auditTrail"
              :key="event.id"
              :event="event"
              :tone="auditClass(event.action)"
              :field-labels="auditFieldLabels"
            />
            <p v-if="!auditTrail.length" class="empty-audit">暂无审计记录，状态变更后会自动写入。</p>
          </div>
        </section>
      </aside>
    </div>

    <section class="panel trace-evidence-panel">
      <header class="trace-header">
        <div>
          <GitBranch :size="15" />
          <h2>Trace Evidence</h2>
        </div>
        <span>生成链路 · 仅 mock/local-rule</span>
      </header>
      <div class="trace-steps">
        <article
          v-for="(step, index) in selectedDetail.traceEvidence"
          :key="step.label"
          :class="['trace-step-card', step.status]"
        >
          <span class="trace-step-index">{{ index + 1 }}</span>
          <strong>{{ step.label }}</strong>
          <small>{{ step.detail }}</small>
        </article>
      </div>
    </section>

    <section class="compliance-principles" aria-label="合规原则">
      <h2>合规原则</h2>
      <div>
        <article v-for="principle in center.compliancePrinciples" :key="principle">
          <ShieldCheck :size="16" />
          <strong>{{ principle }}</strong>
        </article>
      </div>
    </section>

    <footer class="tag-footer">
      <span>Human Review</span>
      <span>Trace Evidence</span>
      <span>Risk Guard</span>
      <span>Schema Validate</span>
      <span>local-rule fallback</span>
      <p>所有内容均为匿名化 seed demo 数据；确认按钮只改变 H2 demo persistence 状态。</p>
    </footer>
  </main>
</template>
