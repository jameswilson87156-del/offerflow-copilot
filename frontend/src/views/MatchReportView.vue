<script setup lang="ts">
import { computed } from 'vue'
import {
  AlertTriangle,
  Archive,
  BarChart3,
  BadgeCheck,
  Check,
  ChevronRight,
  ClipboardCheck,
  Clock3,
  Database,
  FileText,
  Fingerprint,
  GitBranch,
  History,
  PlusCircle,
  RefreshCw,
  RotateCcw,
  Send,
  ShieldCheck,
  SlidersHorizontal,
  Target,
} from 'lucide-vue-next'
import { useMatchReport } from '../composables/useMatchReport'
import { useLocalActor } from '../composables/useLocalActor'
import AuditEventDisclosure from '../components/AuditEventDisclosure.vue'
import CopyPermissionPanel from '../components/CopyPermissionPanel.vue'
import { isReadonlyStatus, statusClass, statusLabel } from '../utils/status'

const props = defineProps<{
  searchQuery: string
  selectedProvider: string
}>()

const {
  report,
  versions,
  auditEvents,
  loading,
  actionBusy,
  actionMessage,
  copyCheck,
  copyPermission,
  copyAuditEvents,
  source,
  reload,
  loadVersion,
  generateVersion,
  sendToReview,
  archiveVersion,
  restoreVersion,
  checkCopyPermission,
  copyConfirmedSummary,
} = useMatchReport()
const { can, permissionReason } = useLocalActor()

const providerNotice = computed(() => props.selectedProvider === 'local-rule'
  ? 'local-rule 匹配报告 · 无外部调用'
  : `${props.selectedProvider} 未配置，当前仍使用 local-rule`)

const filteredEvidence = computed(() => {
  const query = props.searchQuery.trim().toLowerCase()
  if (!query) return report.value.evidenceSources
  return report.value.evidenceSources.filter((item) =>
    [item.requirement, item.project, item.rationale, ...item.evidenceTypes].join(' ').toLowerCase().includes(query),
  )
})

const versionHistory = computed(() =>
  [...versions.value].sort((a, b) => b.versionNo - a.versionNo),
)

const sourceStats = computed(() => [
  { label: 'JD parse version', value: `v${report.value.parseVersionNo}`, detail: report.value.parseVersionId, icon: Database },
  { label: 'Evidence bindings', value: `${report.value.evidenceBindingCount}`, detail: '绑定证据数量', icon: GitBranch },
  { label: 'Provider mode', value: report.value.providerMode, detail: report.value.generatedBy, icon: SlidersHorizontal },
  { label: 'Trace ID', value: report.value.traceId, detail: report.value.schemaVersion, icon: Fingerprint },
])

const copyAllowedLabel = computed(() => copyCheck.value.allowed ? '允许复制' : '禁止复制')

const canRestoreVersion = computed(() => ['RETURNED', 'RISK_FLAGGED', 'ARCHIVED'].includes(report.value.status))
const isReadonlyVersion = computed(() => isReadonlyStatus(report.value.status))
const canGenerateReport = computed(() => can('MATCH_REPORT_GENERATE'))
const canSendReview = computed(() => can('MATCH_REPORT_SEND_TO_REVIEW'))
const canArchiveReport = computed(() => can('MATCH_REPORT_ARCHIVE'))
const canRestoreReport = computed(() => can('MATCH_REPORT_RESTORE'))
const canCheckCopy = computed(() => can('COPY_CHECK'))
const copyPermissionReason = computed(() => permissionReason('COPY_CHECK'))

const currentStatusHint = computed(() => ({
  DRAFT: '草稿：需要送入 Human Review，确认前不可复制为正式建议。',
  IN_REVIEW: '等待人工复核：复核完成前不可复制。',
  CONFIRMED: '已确认：已通过 Human Review，可复制确认版摘要。',
  RETURNED: '已退回，需要修改或恢复为草稿后重新复核。',
  RISK_FLAGGED: '风险标记：不可作为正式建议，需要人工处理。',
  ARCHIVED: '只读归档：不可送审，不可复制，可恢复为草稿。',
} as Record<string, string>)[report.value.status] ?? '未知状态，需要人工复核。')

function scorePercent(value: number, maximum: number) {
  return `${Math.max(0, Math.round((Math.abs(value) / maximum) * 100))}%`
}

function severityClass(severity: string) {
  if (severity === '高') return 'high'
  if (severity === '中') return 'medium'
  return 'low'
}

function auditTone(action: string) {
  if (action === 'ARCHIVE') return 'archive'
  if (action === 'RESTORE_VERSION') return 'restore'
  if (action.startsWith('HUMAN_REVIEW')) return 'review'
  if (action.startsWith('COPY')) return 'copy'
  if (action === 'SEND_TO_REVIEW') return 'review'
  if (action === 'CREATE_DRAFT') return 'draft'
  return 'generate'
}
</script>

<template>
  <main class="main-content workflow-page match-report-page" :class="{ 'readonly-state': isReadonlyVersion }">
    <section class="page-intro">
      <div class="intro-copy">
        <div class="breadcrumbs"><span>匹配报告</span><ChevronRight :size="13" /><strong>Evidence Match</strong></div>
        <h1>匹配报告 <span class="title-mark workflow-title-mark"><BarChart3 :size="20" /></span></h1>
        <p>基于岗位 JD、简历证据和项目证明，生成可版本化、可解释、可复核的岗位匹配分析。</p>
      </div>
      <div class="intro-actions">
        <div class="mode-note workflow-mode" :class="{ warning: props.selectedProvider !== 'local-rule' }">
          <span><Check v-if="props.selectedProvider === 'local-rule'" :size="13" /><SlidersHorizontal v-else :size="13" /></span>
          {{ providerNotice }}
        </div>
        <button type="button" class="secondary-button match-generate-button" :disabled="actionBusy || loading || !canGenerateReport" :title="canGenerateReport ? '' : permissionReason('MATCH_REPORT_GENERATE')" @click="generateVersion">
          <PlusCircle :size="15" />生成新匹配报告
        </button>
        <button type="button" class="secondary-button" :disabled="loading" @click="reload">
          <RefreshCw :size="15" :class="{ spinning: loading }" />刷新报告
        </button>
      </div>
    </section>

    <section v-if="isReadonlyVersion" class="readonly-state-banner" role="status">
      <Archive :size="15" />
      <strong>{{ statusLabel(report.status) }}只读态</strong>
      <span>{{ report.status === 'ARCHIVED' ? '归档版本不可送审或复制；如需继续处理，请先恢复为草稿。' : '当前版本不可作为正式建议；请恢复为草稿后重新进入复核。' }}</span>
    </section>

    <section class="workflow-context" aria-label="匹配报告摘要">
      <div class="workflow-context-main">
        <span class="workflow-ready-icon"><ShieldCheck :size="16" /></span>
        <strong>{{ report.summary.jobTitle }}</strong>
        <span>v{{ report.versionNo }} · {{ statusLabel(report.status) }} · Human Review: {{ statusLabel(report.humanReviewStatus) }}</span>
      </div>
      <div class="workflow-context-meta">
        <span :class="source">{{ source === 'api' ? 'LOCAL API LIVE' : 'DEMO SNAPSHOT' }}</span>
        <small>{{ report.disclaimer }}</small>
      </div>
    </section>

    <section class="panel match-copy-panel" aria-label="状态与复制许可">
      <header class="match-copy-header">
        <div>
          <span class="panel-kicker">REVIEW SYNC</span>
          <h2>状态与复制许可</h2>
        </div>
        <span :class="['status-chip', statusClass(report.status)]">{{ statusLabel(report.status) }}</span>
      </header>
      <div class="copy-permission-grid">
        <article>
          <BadgeCheck :size="15" />
          <span>当前版本状态</span>
          <strong>{{ statusLabel(report.status) }}</strong>
          <small>{{ currentStatusHint }}</small>
        </article>
        <article>
          <ShieldCheck :size="15" />
          <span>Human Review 状态</span>
          <strong>{{ statusLabel(report.humanReviewStatus) }}</strong>
          <small>{{ report.humanReviewId }}</small>
        </article>
        <article :class="copyCheck.allowed ? 'allowed' : 'blocked'">
          <ClipboardCheck :size="15" />
          <span>复制许可</span>
          <strong>{{ copyAllowedLabel }}</strong>
          <small>{{ copyCheck.reason }}</small>
        </article>
        <article class="notice">
          <AlertTriangle :size="15" />
          <span>Boundary Notice</span>
          <strong>local-rule / demo user</strong>
          <small>{{ copyCheck.boundaryNotice }}</small>
        </article>
      </div>
      <div class="copy-action-row">
        <button type="button" class="copy-action-button restore" :disabled="actionBusy || loading || !canRestoreVersion || !canRestoreReport" :title="canRestoreReport ? '' : permissionReason('MATCH_REPORT_RESTORE')" @click="restoreVersion">
          <RotateCcw :size="15" />恢复版本
        </button>
        <button type="button" class="copy-action-button review" :disabled="actionBusy || loading || report.status !== 'DRAFT' || !canSendReview" :title="canSendReview ? '只有草稿可以送入人工复核；归档版本需先恢复' : permissionReason('MATCH_REPORT_SEND_TO_REVIEW')" @click="sendToReview">
          <Send :size="15" />送入人工复核
        </button>
        <button type="button" class="copy-action-button archive" :disabled="actionBusy || loading || report.status === 'ARCHIVED' || !canArchiveReport" :title="canArchiveReport ? '' : permissionReason('MATCH_REPORT_ARCHIVE')" @click="archiveVersion">
          <Archive :size="15" />归档版本
        </button>
      </div>
      <p v-if="!canSendReview || !canArchiveReport || !canCheckCopy" class="permission-inline-note">
        {{ !canCheckCopy ? copyPermissionReason : !canSendReview ? permissionReason('MATCH_REPORT_SEND_TO_REVIEW') : permissionReason('MATCH_REPORT_ARCHIVE') }}
      </p>
      <CopyPermissionPanel
        title="Copy Permission Contract"
        kicker="COPY GATE"
        description="Schema Validate 和 Risk Guard 通过不等于可复制；只有 Human Review Confirmed 后才显示复制确认版摘要。"
        :result="copyPermission"
        :audit-events="copyAuditEvents"
        :busy="actionBusy || loading"
        :check-disabled="!canCheckCopy"
        :copy-disabled="!canCheckCopy"
        :disabled-reason="copyPermissionReason"
        copy-label="复制确认版摘要"
        @check="checkCopyPermission"
        @copy="copyConfirmedSummary"
      />
      <p class="copy-action-message">{{ actionMessage }}</p>
    </section>

    <section class="match-source-grid" aria-label="报告来源">
      <article v-for="item in sourceStats" :key="item.label">
        <component :is="item.icon" :size="15" />
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.detail }}</small>
      </article>
    </section>

    <section class="panel match-version-panel">
      <header class="panel-header workflow-panel-header">
        <div>
          <span class="panel-kicker">VERSION HISTORY</span>
          <h2>版本历史</h2>
        </div>
        <span class="count-chip">{{ versions.length }} 个版本</span>
      </header>
      <div class="match-version-layout">
        <div class="match-version-list" aria-label="版本列表">
          <button
            v-for="version in versionHistory"
            :key="version.id"
            type="button"
            class="match-version-item"
            :class="{ active: version.id === report.versionId }"
            @click="loadVersion(version.id)"
          >
            <strong>v{{ version.versionNo }} {{ version.providerMode }}</strong>
            <span>{{ version.createdAt }} · {{ statusLabel(version.status) }}</span>
            <small>JD parse v{{ version.parseVersionNo }} · {{ version.evidenceBindingCount }} bindings</small>
            <em>{{ statusLabel(version.humanReviewStatus) }}</em>
          </button>
        </div>

        <div class="match-handoff-box">
          <div class="handoff-state">
            <Clock3 :size="16" />
            <div>
              <span>当前版本</span>
              <strong>v{{ report.versionNo }} · {{ statusLabel(report.status) }}</strong>
              <small>{{ report.humanReviewId }}</small>
            </div>
          </div>
          <p>{{ currentStatusHint }}</p>
        </div>

        <div class="match-audit-mini">
          <header>
            <History :size="15" />
            <strong>审计历史</strong>
            <em>{{ auditEvents.length }} 条</em>
          </header>
          <AuditEventDisclosure
            v-for="event in auditEvents"
            :key="event.id"
            :event="event"
            :tone="auditTone(event.action)"
          />
        </div>
      </div>
    </section>

    <section class="panel match-summary-panel">
      <div class="match-summary-score">
        <span>综合匹配得分</span>
        <strong>{{ report.summary.totalScore }}<small>/{{ report.summary.maximumScore }}</small></strong>
        <p>{{ report.summary.note }}</p>
      </div>
      <div class="resume-version-list">
        <span>推荐简历版本</span>
        <strong v-for="version in report.summary.recommendedResumeVersions" :key="version">{{ version }}</strong>
      </div>
      <div class="report-status-box">
        <span>当前状态</span>
        <strong>{{ statusLabel(report.status) }}</strong>
        <p>{{ currentStatusHint }}</p>
      </div>
    </section>

    <div class="workflow-two-column">
      <section class="panel score-breakdown-panel">
        <header class="panel-header workflow-panel-header">
          <div>
            <span class="panel-kicker">SCORE BREAKDOWN</span>
            <h2>评分拆解</h2>
          </div>
          <Target :size="17" class="header-icon" />
        </header>
        <div class="score-breakdown-list">
          <article v-for="item in report.score.items" :key="item.key" :class="['score-breakdown-item', item.tone]">
            <div>
              <strong>{{ item.label }}</strong>
              <span>{{ item.value }}/{{ item.maximum }}</span>
            </div>
            <i><b :style="{ width: scorePercent(item.value, item.maximum) }" /></i>
            <p>{{ item.detail }}</p>
          </article>
        </div>
        <p class="workflow-note">{{ report.score.note }}</p>
      </section>

      <section class="panel evidence-source-panel">
        <header class="panel-header workflow-panel-header">
          <div>
            <span class="panel-kicker">EVIDENCE SOURCES</span>
            <h2>证据来源</h2>
          </div>
          <span class="count-chip">{{ filteredEvidence.length }} 项</span>
        </header>
        <div class="match-evidence-list">
          <article v-for="item in filteredEvidence" :key="`${item.requirement}-${item.project}`">
            <div>
              <strong>{{ item.requirement }}</strong>
              <ChevronRight :size="12" />
              <strong>{{ item.project }}</strong>
              <em>{{ item.strength }}</em>
            </div>
            <p>{{ item.rationale }}</p>
            <span><b v-for="type in item.evidenceTypes" :key="type">{{ type }}</b></span>
          </article>
        </div>
      </section>
    </div>

    <div class="workflow-two-column lower">
      <section class="panel skill-gap-panel">
        <header class="panel-header workflow-panel-header">
          <div>
            <span class="panel-kicker">SKILL GAPS</span>
            <h2>技能差距</h2>
          </div>
          <AlertTriangle :size="17" class="header-icon" />
        </header>
        <div class="skill-gap-grid">
          <article v-for="gap in report.skillGaps" :key="gap.skill" :class="['skill-gap-card', severityClass(gap.severity)]">
            <div><strong>{{ gap.skill }}</strong><em>{{ gap.severity }}</em></div>
            <p>{{ gap.reason }}</p>
            <small>{{ gap.nextAction }}</small>
          </article>
        </div>
      </section>

      <section class="panel recommended-action-panel">
        <header class="panel-header workflow-panel-header">
          <div>
            <span class="panel-kicker">NEXT ACTIONS</span>
            <h2>推荐行动</h2>
          </div>
          <FileText :size="17" class="header-icon" />
        </header>
        <div class="recommended-action-list">
          <article v-for="action in report.recommendedActions" :key="action.title" :class="action.priority">
            <strong>{{ action.title }}</strong>
            <p>{{ action.detail }}</p>
          </article>
        </div>
      </section>
    </div>

    <section class="panel match-risk-note-panel">
      <header class="panel-header workflow-panel-header">
        <div>
          <span class="panel-kicker">RISK NOTES</span>
          <h2>风险提醒</h2>
        </div>
        <span :class="['status-chip', statusClass(report.status)]">{{ statusLabel(report.status) }}</span>
      </header>
      <div class="risk-note-list">
        <article v-for="note in report.riskNotes" :key="note">
          <AlertTriangle :size="14" />
          <span>{{ note }}</span>
        </article>
      </div>
    </section>

    <section class="panel workflow-trace-panel">
      <header class="trace-header">
        <div>
          <GitBranch :size="15" />
          <h2>Trace Evidence</h2>
        </div>
        <span>JD parse version -> Evidence bindings -> local-rule scoring -> Versioned asset -> Human Review</span>
      </header>
      <div class="workflow-trace-steps">
        <article v-for="(step, index) in report.traceEvidence" :key="step.label" :class="step.status">
          <span>{{ index + 1 }}</span>
          <strong>{{ step.label }}</strong>
          <small>{{ step.detail }}</small>
        </article>
      </div>
    </section>

    <footer class="tag-footer">
      <span>Match Report Versioning</span>
      <span>Trace Evidence</span>
      <span>Risk Guard</span>
      <span>Human Review</span>
      <span>local-rule fallback</span>
      <p>{{ report.disclaimer }}</p>
    </footer>
  </main>
</template>
