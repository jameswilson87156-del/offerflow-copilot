<script setup lang="ts">
import { computed } from 'vue'
import {
  AlertTriangle,
  BarChart3,
  Check,
  ChevronRight,
  FileText,
  GitBranch,
  RefreshCw,
  ShieldCheck,
  SlidersHorizontal,
  Target,
} from 'lucide-vue-next'
import { useMatchReport } from '../composables/useMatchReport'

const props = defineProps<{
  searchQuery: string
  selectedProvider: string
}>()

const { report, loading, source, reload } = useMatchReport()

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

function scorePercent(value: number, maximum: number) {
  return `${Math.max(0, Math.round((Math.abs(value) / maximum) * 100))}%`
}

function severityClass(severity: string) {
  if (severity === '高') return 'high'
  if (severity === '中') return 'medium'
  return 'low'
}
</script>

<template>
  <main class="main-content workflow-page match-report-page">
    <section class="page-intro">
      <div class="intro-copy">
        <div class="breadcrumbs"><span>匹配报告</span><ChevronRight :size="13" /><strong>Evidence Match</strong></div>
        <h1>匹配报告 <span class="title-mark workflow-title-mark"><BarChart3 :size="20" /></span></h1>
        <p>基于岗位 JD、简历证据和项目证明，展示可解释、可复核的岗位匹配分析。</p>
      </div>
      <div class="intro-actions">
        <div class="mode-note workflow-mode" :class="{ warning: props.selectedProvider !== 'local-rule' }">
          <span><Check v-if="props.selectedProvider === 'local-rule'" :size="13" /><SlidersHorizontal v-else :size="13" /></span>
          {{ providerNotice }}
        </div>
        <button type="button" class="secondary-button" :disabled="loading" @click="reload">
          <RefreshCw :size="15" :class="{ spinning: loading }" />刷新报告
        </button>
      </div>
    </section>

    <section class="workflow-context" aria-label="匹配报告摘要">
      <div class="workflow-context-main">
        <span class="workflow-ready-icon"><ShieldCheck :size="16" /></span>
        <strong>{{ report.summary.jobTitle }}</strong>
        <span>{{ report.summary.status }}</span>
      </div>
      <div class="workflow-context-meta">
        <span :class="source">{{ source === 'api' ? 'LOCAL API LIVE' : 'DEMO SNAPSHOT' }}</span>
        <small>{{ report.disclaimer }}</small>
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
        <strong>{{ report.summary.status }}</strong>
        <p>复制、投递或外发前必须经过人工确认。</p>
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

    <section class="panel workflow-trace-panel">
      <header class="trace-header">
        <div>
          <GitBranch :size="15" />
          <h2>Trace Evidence</h2>
        </div>
        <span>JD 输入 -> 关键词解析 -> 证据检索 -> 评分拆解 -> 风险校验 -> Human Review</span>
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
      <span>Match Report</span>
      <span>Trace Evidence</span>
      <span>Risk Guard</span>
      <span>Human Review</span>
      <span>local-rule fallback</span>
      <p>{{ report.disclaimer }}</p>
    </footer>
  </main>
</template>
