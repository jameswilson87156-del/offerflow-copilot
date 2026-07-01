<script setup lang="ts">
import { computed } from 'vue'
import {
  BriefcaseBusiness,
  Check,
  ChevronRight,
  Clock3,
  MessageSquareText,
  RefreshCw,
  ShieldCheck,
  SlidersHorizontal,
  StickyNote,
} from 'lucide-vue-next'
import { useApplicationTracker } from '../composables/useApplicationTracker'

const props = defineProps<{
  searchQuery: string
  selectedProvider: string
}>()

const { tracker, loading, source, reload } = useApplicationTracker()

const providerNotice = computed(() => props.selectedProvider === 'local-rule'
  ? '手动投递跟踪 · 不接招聘平台 API'
  : `${props.selectedProvider} 未配置，投递跟踪仍为手动 mock`)

const filteredApplications = computed(() => {
  const query = props.searchQuery.trim().toLowerCase()
  if (!query) return tracker.value.applications
  return tracker.value.applications.filter((item) =>
    [
      item.company,
      item.role,
      item.city,
      item.resumeVersion,
      item.sourceNote,
      item.status,
      item.nextAction,
    ].join(' ').toLowerCase().includes(query),
  )
})

function applicationsByStatus(label: string) {
  return filteredApplications.value.filter((item) => item.status === label)
}
</script>

<template>
  <main class="main-content workflow-page application-tracker-page">
    <section class="page-intro">
      <div class="intro-copy">
        <div class="breadcrumbs"><span>投递跟踪</span><ChevronRight :size="13" /><strong>Manual Tracker</strong></div>
        <h1>投递跟踪 <span class="title-mark workflow-title-mark tracker-mark"><BriefcaseBusiness :size="20" /></span></h1>
        <p>手动记录投递状态、沟通记录、简历版本、面试安排和复盘结果。</p>
      </div>
      <div class="intro-actions">
        <div class="mode-note workflow-mode" :class="{ warning: props.selectedProvider !== 'local-rule' }">
          <span><Check v-if="props.selectedProvider === 'local-rule'" :size="13" /><SlidersHorizontal v-else :size="13" /></span>
          {{ providerNotice }}
        </div>
        <button type="button" class="secondary-button" :disabled="loading" @click="reload">
          <RefreshCw :size="15" :class="{ spinning: loading }" />刷新记录
        </button>
      </div>
    </section>

    <section class="workflow-context" aria-label="投递跟踪边界">
      <div class="workflow-context-main">
        <span class="workflow-ready-icon"><StickyNote :size="16" /></span>
        <strong>{{ filteredApplications.length }} 条手动投递记录</strong>
        <span>不自动投递，不抓取平台聊天，不保存真实 HR 隐私</span>
      </div>
      <div class="workflow-context-meta">
        <span :class="source">{{ source === 'api' ? 'LOCAL API LIVE' : 'DEMO SNAPSHOT' }}</span>
        <small>{{ tracker.disclaimer }}</small>
      </div>
    </section>

    <section class="panel application-board-panel">
      <header class="panel-header workflow-panel-header">
        <div>
          <span class="panel-kicker">APPLICATION BOARD</span>
          <h2>投递看板</h2>
        </div>
        <span class="count-chip">{{ tracker.boardColumns.length }} 列</span>
      </header>
      <div class="application-board-grid">
        <article v-for="column in tracker.boardColumns" :key="column.key" :class="['application-column', column.tone]">
          <header>
            <strong>{{ column.label }}</strong>
            <em>{{ column.count }}</em>
          </header>
          <div>
            <span
              v-for="app in applicationsByStatus(column.label)"
              :key="app.id"
            >{{ app.company }} · {{ app.role }}</span>
            <small v-if="applicationsByStatus(column.label).length === 0">暂无手动记录</small>
          </div>
        </article>
      </div>
    </section>

    <div class="application-layout">
      <section class="panel application-record-panel">
        <header class="panel-header workflow-panel-header">
          <div>
            <span class="panel-kicker">APPLICATION RECORDS</span>
            <h2>投递记录卡片</h2>
          </div>
          <BriefcaseBusiness :size="17" class="header-icon" />
        </header>
        <div class="application-card-grid">
          <article v-for="item in filteredApplications" :key="item.id" class="application-card">
            <div class="application-card-title">
              <strong>{{ item.company }}</strong>
              <em>{{ item.status }}</em>
            </div>
            <h3>{{ item.role }}</h3>
            <dl>
              <div><dt>城市</dt><dd>{{ item.city }}</dd></div>
              <div><dt>简历版本</dt><dd>{{ item.resumeVersion }}</dd></div>
              <div><dt>来源备注</dt><dd>{{ item.sourceNote }}</dd></div>
              <div><dt>最近更新</dt><dd>{{ item.updatedAt }}</dd></div>
            </dl>
            <p>{{ item.nextAction }}</p>
          </article>
        </div>
      </section>

      <aside class="panel communication-log-panel">
        <header class="panel-header workflow-panel-header">
          <div>
            <span class="panel-kicker">COMMUNICATION LOG</span>
            <h2>沟通记录</h2>
          </div>
          <MessageSquareText :size="17" class="header-icon" />
        </header>
        <div class="communication-log-list">
          <article v-for="log in tracker.communicationLogs" :key="`${log.applicationId}-${log.stage}-${log.timestamp}`">
            <span><Clock3 :size="12" />{{ log.timestamp }}</span>
            <strong>{{ log.stage }}</strong>
            <p>{{ log.note }}</p>
          </article>
        </div>
      </aside>
    </div>

    <section class="panel tracker-boundary-panel">
      <header class="panel-header workflow-panel-header">
        <div>
          <span class="panel-kicker">BOUNDARY</span>
          <h2>风险边界</h2>
        </div>
        <ShieldCheck :size="17" class="header-icon" />
      </header>
      <div class="tracker-boundary-grid">
        <article v-for="boundary in tracker.riskBoundaries" :key="boundary">
          <ShieldCheck :size="15" />
          <strong>{{ boundary }}</strong>
        </article>
      </div>
    </section>

    <footer class="tag-footer">
      <span>Application Tracker</span>
      <span>Manual Records</span>
      <span>Resume Version</span>
      <span>Interview Review</span>
      <span>mock/local-rule</span>
      <p>{{ tracker.disclaimer }}</p>
    </footer>
  </main>
</template>
