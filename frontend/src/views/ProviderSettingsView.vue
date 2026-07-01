<script setup lang="ts">
import { computed, shallowRef, watch } from 'vue'
import {
  AlertTriangle,
  Check,
  CheckCircle2,
  ChevronRight,
  Code2,
  DatabaseZap,
  FileJson,
  GitBranch,
  KeyRound,
  RefreshCw,
  Route,
  ShieldCheck,
  SlidersHorizontal,
  Timer,
} from 'lucide-vue-next'
import { useProviderTrace } from '../composables/useProviderTrace'
import type { ProviderCard, ProviderPipelineStep, ProviderTraceStepStatus } from '../types'

const props = defineProps<{
  searchQuery: string
  selectedProvider: string
}>()

const { settings, traceIndex, traceRun, loading, source, reload } = useProviderTrace()
const selectedStepKey = shallowRef('provider-call')

const filteredProviders = computed(() => {
  const query = props.searchQuery.trim().toLowerCase()
  if (!query) return settings.value.providers
  return settings.value.providers.filter((provider) =>
    [
      provider.name,
      provider.status,
      provider.baseUrlStatus,
      provider.model,
      provider.fallbackPolicy,
      provider.boundaryNotice,
      provider.apiKeyStatus,
    ].join(' ').toLowerCase().includes(query),
  )
})

const filteredSteps = computed(() => {
  const query = props.searchQuery.trim().toLowerCase()
  if (!query) return traceRun.value.pipeline
  return traceRun.value.pipeline.filter((step) =>
    [
      step.label,
      step.status,
      step.inputSummary,
      step.outputSummary,
      step.linkedEvidence,
      traceRun.value.runId,
      traceRun.value.providerMode,
      traceRun.value.model,
      traceRun.value.jobTitle,
    ].join(' ').toLowerCase().includes(query),
  )
})

const selectedStep = computed<ProviderPipelineStep>(() =>
  traceRun.value.pipeline.find((step) => step.key === selectedStepKey.value)
  ?? traceRun.value.pipeline[0]!,
)

const runDetailRows = computed(() => [
  ['Run ID', traceRun.value.runId],
  ['Provider mode', traceRun.value.providerMode],
  ['Final Provider', traceRun.value.finalProvider],
  ['Model', traceRun.value.model],
  ['Fallback reason', traceRun.value.fallbackReason],
  ['Prompt version', traceRun.value.promptVersion],
  ['Schema version', traceRun.value.schemaVersion],
  ['Risk flags', traceRun.value.riskFlags.join(' / ')],
  ['Evidence count', `${traceRun.value.evidenceCount} 条`],
  ['Human review status', traceRun.value.humanReviewStatus],
  ['Duration', traceRun.value.duration],
  ['Trace hash', traceRun.value.traceHash],
])

const providerNotice = computed(() => props.selectedProvider === 'local-rule'
  ? 'local-rule fallback active · 无外部调用'
  : `${props.selectedProvider} 未配置，本页保持 local-rule fallback`)

watch(filteredSteps, (steps) => {
  if (steps.length && !steps.some((step) => step.key === selectedStepKey.value)) selectedStepKey.value = steps[0].key
})

function providerStatusClass(provider: ProviderCard) {
  if (provider.status === 'Active') return 'active'
  if (provider.status === 'Ready' || provider.status === 'Configured') return 'ready'
  return 'disabled'
}

function stepClass(status: ProviderTraceStepStatus) {
  return status
}
</script>

<template>
  <main class="main-content provider-trace-page">
    <section class="page-intro provider-trace-intro">
      <div class="intro-copy">
        <div class="breadcrumbs"><span>Provider 设置</span><ChevronRight :size="13" /><strong>Trace Evidence</strong></div>
        <h1>Provider 设置与证据链 <span class="title-mark provider-title-mark"><ShieldCheck :size="20" /></span></h1>
        <p>管理 AI Provider 状态，追踪每次分析运行的输入、输出、Schema 校验、fallback 和人工复核结果。</p>
      </div>
      <div class="intro-actions">
        <div class="mode-note provider-trace-mode" :class="{ warning: props.selectedProvider !== 'local-rule' }">
          <span><Check v-if="props.selectedProvider === 'local-rule'" :size="13" /><SlidersHorizontal v-else :size="13" /></span>
          {{ providerNotice }}
        </div>
        <button type="button" class="secondary-button" :disabled="loading" @click="reload">
          <RefreshCw :size="15" :class="{ spinning: loading }" />刷新 Trace
        </button>
      </div>
    </section>

    <section class="provider-trace-context" aria-label="Provider Trace 状态">
      <div class="provider-context-main">
        <span class="provider-ready-icon"><Route :size="16" /></span>
        <strong>{{ settings.currentStatus }}</strong>
        <span>Run: {{ traceRun.runId }} · {{ traceRun.humanReviewStatus }}</span>
      </div>
      <div class="provider-context-meta">
        <span :class="source">{{ source === 'api' ? 'LOCAL API LIVE' : 'DEMO SNAPSHOT' }}</span>
        <small>mock/local-rule · 不保存 API Key · 不调用真实 Provider</small>
      </div>
    </section>

    <div class="provider-top-grid">
      <section class="panel provider-status-panel">
        <header class="panel-header provider-panel-header">
          <div>
            <span class="panel-kicker">PROVIDER STATUS</span>
            <h2>Provider 状态</h2>
          </div>
          <span class="count-chip">{{ filteredProviders.length }} 个</span>
        </header>
        <div class="provider-card-grid">
          <article
            v-for="provider in filteredProviders"
            :key="provider.id"
            :class="['provider-card', providerStatusClass(provider)]"
          >
            <div class="provider-card-top">
              <span class="provider-symbol"><Code2 v-if="provider.id === 'local-rule'" :size="17" /><DatabaseZap v-else :size="17" /></span>
              <div>
                <strong>{{ provider.name }}</strong>
                <small>{{ provider.apiKeyStatus }}</small>
              </div>
              <em>{{ provider.status }}</em>
            </div>
            <dl class="provider-card-meta">
              <div><dt>Base URL</dt><dd>{{ provider.baseUrlStatus }}</dd></div>
              <div><dt>Model</dt><dd>{{ provider.model }}</dd></div>
              <div><dt>Timeout</dt><dd>{{ provider.timeout }}</dd></div>
              <div><dt>Last Run</dt><dd>{{ provider.lastRun }}</dd></div>
              <div><dt>Fallback policy</dt><dd>{{ provider.fallbackPolicy }}</dd></div>
              <div><dt>Boundary notice</dt><dd>{{ provider.boundaryNotice }}</dd></div>
            </dl>
            <footer>
              <span><Timer :size="12" />Real call enabled <b>{{ provider.realCallEnabled }}</b></span>
              <span><FileJson :size="12" />Raw response save <b>{{ provider.rawResponseSave }}</b></span>
            </footer>
          </article>
        </div>
      </section>

      <section class="panel safety-boundary-panel">
        <header class="panel-header provider-panel-header">
          <div>
            <span class="panel-kicker">SAFETY BOUNDARY</span>
            <h2>安全边界</h2>
          </div>
          <KeyRound :size="17" class="header-icon" />
        </header>
        <div class="safety-boundary-list">
          <article
            v-for="boundary in settings.safetyBoundaries"
            :key="boundary.title"
            :class="['safety-boundary-item', boundary.tone]"
          >
            <span><ShieldCheck v-if="boundary.tone === 'safe'" :size="14" /><AlertTriangle v-else :size="14" /></span>
            <div>
              <strong>{{ boundary.title }}</strong>
              <p>{{ boundary.description }}</p>
            </div>
          </article>
        </div>
      </section>
    </div>

    <section class="panel run-pipeline-panel">
      <header class="trace-header provider-trace-header">
        <div>
          <GitBranch :size="15" />
          <h2>Trace Timeline / Run Pipeline</h2>
        </div>
        <span>{{ traceIndex.items[0]?.startedAt }} · {{ traceIndex.items[0]?.duration }}</span>
      </header>
      <div class="run-pipeline-track">
        <button
          v-for="(step, index) in filteredSteps"
          :key="step.key"
          type="button"
          :class="['pipeline-step', stepClass(step.status), { selected: step.key === selectedStep.key }]"
          @click="selectedStepKey = step.key"
        >
          <span>{{ index + 1 }}</span>
          <strong>{{ step.label }}</strong>
          <small>{{ step.duration }}</small>
          <p>{{ step.outputSummary }}</p>
          <em>{{ step.linkedEvidence }}</em>
        </button>
      </div>
    </section>

    <div class="provider-bottom-grid">
      <section class="panel run-detail-panel">
        <header class="panel-header provider-panel-header">
          <div>
            <span class="panel-kicker">RUN DETAILS</span>
            <h2>Run Details</h2>
          </div>
          <span class="status-chip draft">{{ traceRun.humanReviewStatus }}</span>
        </header>
        <dl class="run-detail-grid">
          <div v-for="[label, value] in runDetailRows" :key="label">
            <dt>{{ label }}</dt>
            <dd>{{ value }}</dd>
          </div>
        </dl>
      </section>

      <section class="panel trace-detail-panel">
        <header class="panel-header provider-panel-header">
          <div>
            <span class="panel-kicker">TRACE EVIDENCE DETAIL</span>
            <h2>当前步骤：{{ selectedStep.label }}</h2>
          </div>
          <span :class="['pipeline-status', selectedStep.status]">{{ selectedStep.status }}</span>
        </header>

        <div class="trace-detail-layout">
          <section class="trace-detail-block jd-snippet">
            <h3>引用 JD 片段</h3>
            <blockquote>{{ traceRun.evidenceDetail.jdSnippet }}</blockquote>
            <p>{{ selectedStep.inputSummary }} -> {{ selectedStep.outputSummary }}</p>
          </section>

          <section class="trace-detail-block resume-evidence">
            <h3>引用简历证据</h3>
            <article v-for="item in traceRun.evidenceDetail.resumeEvidence" :key="item.id">
              <strong>{{ item.id }} · {{ item.title }}</strong>
              <p>{{ item.excerpt }}</p>
              <span><em v-for="sourceType in item.sources" :key="sourceType">{{ sourceType }}</em></span>
            </article>
          </section>

          <section class="trace-detail-block json-summary">
            <h3>结构化 JSON 摘要</h3>
            <pre>{{ traceRun.evidenceDetail.jsonSummary }}</pre>
          </section>

          <section class="trace-detail-block fallback-reason">
            <h3>错误 / 降级原因</h3>
            <p>{{ traceRun.evidenceDetail.fallbackReason }}</p>
            <h3>人工备注</h3>
            <p>{{ traceRun.evidenceDetail.humanNote }}</p>
          </section>
        </div>
      </section>
    </div>

    <footer class="tag-footer">
      <span v-for="tag in traceRun.technicalTags" :key="tag">{{ tag }}</span>
      <p>所有 Provider 与 Trace 数据均为 mock/local-rule；API Key 不展示、不保存、不发送。</p>
    </footer>
  </main>
</template>
