<script setup lang="ts">
import { computed } from 'vue'
import {
  AlertTriangle,
  Check,
  ChevronRight,
  ClipboardList,
  MessageSquareText,
  RefreshCw,
  ShieldCheck,
  SlidersHorizontal,
  Sparkles,
  UserCheck,
} from 'lucide-vue-next'
import { useInterviewPrep } from '../composables/useInterviewPrep'
import CopyPermissionPanel from '../components/CopyPermissionPanel.vue'

const props = defineProps<{
  searchQuery: string
  selectedProvider: string
}>()

const {
  prep,
  copyPermission,
  copyAuditEvents,
  actionBusy,
  actionMessage,
  loading,
  source,
  reload,
  checkCopyPermission,
  copyConfirmedPrep,
} = useInterviewPrep()

const providerNotice = computed(() => props.selectedProvider === 'local-rule'
  ? '面试前准备 · local-rule mock'
  : `${props.selectedProvider} 未配置，当前仍使用 local-rule`)

const filteredQuestionGroups = computed(() => {
  const query = props.searchQuery.trim().toLowerCase()
  if (!query) return prep.value.questionGroups
  return prep.value.questionGroups
    .map((group) => ({
      ...group,
      questions: group.questions.filter((question) => [group.label, question].join(' ').toLowerCase().includes(query)),
    }))
    .filter((group) => group.questions.length > 0)
})
</script>

<template>
  <main class="main-content workflow-page interview-prep-page">
    <section class="page-intro">
      <div class="intro-copy">
        <div class="breadcrumbs"><span>面试准备</span><ChevronRight :size="13" /><strong>Pre Interview</strong></div>
        <h1>面试准备 <span class="title-mark workflow-title-mark interview-mark"><MessageSquareText :size="20" /></span></h1>
        <p>根据岗位要求、简历证据和项目经历生成面试前准备材料，用于准备与复盘。</p>
      </div>
      <div class="intro-actions">
        <div class="mode-note workflow-mode" :class="{ warning: props.selectedProvider !== 'local-rule' }">
          <span><Check v-if="props.selectedProvider === 'local-rule'" :size="13" /><SlidersHorizontal v-else :size="13" /></span>
          {{ providerNotice }}
        </div>
        <button type="button" class="secondary-button" :disabled="loading" @click="reload">
          <RefreshCw :size="15" :class="{ spinning: loading }" />刷新准备材料
        </button>
      </div>
    </section>

    <section class="workflow-context" aria-label="面试准备边界">
      <div class="workflow-context-main">
        <span class="workflow-ready-icon"><UserCheck :size="16" /></span>
        <strong>{{ prep.jobTitle }}</strong>
        <span>{{ prep.positioningNotice }}</span>
      </div>
      <div class="workflow-context-meta">
        <span :class="source">{{ source === 'api' ? 'LOCAL API LIVE' : 'DEMO SNAPSHOT' }}</span>
        <small>{{ prep.disclaimer }}</small>
      </div>
    </section>

    <section class="panel interview-copy-gate-panel">
      <CopyPermissionPanel
        title="Copy Gate"
        kicker="COPY PERMISSION"
        description="面试准备材料通过 Schema Validate 或 Risk Guard 后仍不可直接复制；只有 Human Review Confirmed 后才可作为正式准备摘要使用。"
        :result="copyPermission"
        :audit-events="copyAuditEvents"
        :busy="actionBusy || loading"
        copy-label="复制确认版准备摘要"
        @check="checkCopyPermission"
        @copy="copyConfirmedPrep"
      />
      <p class="copy-action-message">{{ actionMessage }}</p>
    </section>

    <section class="panel interview-focus-panel">
      <header class="panel-header workflow-panel-header">
        <div>
          <span class="panel-kicker">INTERVIEW FOCUS</span>
          <h2>岗位与面试重点</h2>
        </div>
        <span class="count-chip">{{ prep.focusAreas.length }} 项</span>
      </header>
      <div class="interview-focus-grid">
        <article v-for="area in prep.focusAreas" :key="area.label">
          <strong>{{ area.label }}</strong>
          <p>{{ area.detail }}</p>
          <small>{{ area.evidence }}</small>
        </article>
      </div>
    </section>

    <div class="interview-layout">
      <section class="panel question-groups-panel">
        <header class="panel-header workflow-panel-header">
          <div>
            <span class="panel-kicker">QUESTION GROUPS</span>
            <h2>面试问题分组</h2>
          </div>
          <ClipboardList :size="17" class="header-icon" />
        </header>
        <div class="question-group-list">
          <article v-for="group in filteredQuestionGroups" :key="group.key">
            <h3>{{ group.label }}</h3>
            <ol>
              <li v-for="question in group.questions" :key="question">{{ question }}</li>
            </ol>
          </article>
        </div>
      </section>

      <aside class="panel star-draft-panel">
        <header class="panel-header workflow-panel-header">
          <div>
            <span class="panel-kicker">STAR DRAFT</span>
            <h2>STAR 回答草稿</h2>
          </div>
          <Sparkles :size="17" class="header-icon" />
        </header>
        <div class="star-draft-stack">
          <p><strong>S</strong>{{ prep.starDraft.situation }}</p>
          <p><strong>T</strong>{{ prep.starDraft.task }}</p>
          <p><strong>A</strong>{{ prep.starDraft.action }}</p>
          <p><strong>R</strong>{{ prep.starDraft.result }}</p>
          <p class="risk-note"><strong>Risk</strong>{{ prep.starDraft.riskNote }}</p>
        </div>
      </aside>
    </div>

    <div class="interview-bottom-grid">
      <section class="panel interview-risk-panel">
        <header class="panel-header workflow-panel-header">
          <div>
            <span class="panel-kicker">RISK REMINDERS</span>
            <h2>风险提醒</h2>
          </div>
          <AlertTriangle :size="17" class="header-icon" />
        </header>
        <div class="risk-reminder-grid">
          <article v-for="reminder in prep.riskReminders" :key="reminder">
            <ShieldCheck :size="15" />
            <strong>{{ reminder }}</strong>
          </article>
        </div>
      </section>

      <section class="panel prep-timeline-panel">
        <header class="trace-header">
          <div>
            <ClipboardList :size="15" />
            <h2>面试复盘 Timeline</h2>
          </div>
          <span>待准备 -> 已确认答案 -> 已模拟练习 -> 已面试 -> 已复盘</span>
        </header>
        <div class="prep-timeline-track">
          <article v-for="(step, index) in prep.reviewTimeline" :key="step.key" :class="step.status">
            <span>{{ index + 1 }}</span>
            <strong>{{ step.label }}</strong>
            <small>{{ step.detail }}</small>
          </article>
        </div>
      </section>
    </div>

    <footer class="tag-footer">
      <span>Interview Prep</span>
      <span>STAR Draft</span>
      <span>Trace Evidence</span>
      <span>Human Review</span>
      <span>Pre Interview</span>
      <p>{{ prep.disclaimer }}</p>
    </footer>
  </main>
</template>
