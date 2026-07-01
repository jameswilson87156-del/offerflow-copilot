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

const {
  settings,
  configCheck,
  traceIndex,
  traceRun,
  sandboxResult,
  sandboxError,
  loading,
  running,
  source,
  reload,
  runSandbox,
} = useProviderTrace()

const providerOptions = [
  { id: 'local-rule', label: 'local-rule' },
  { id: 'openai-compatible', label: 'OpenAI-compatible' },
  { id: 'deepseek', label: 'DeepSeek' },
]

const EMPTY_STEP: ProviderPipelineStep = {
  key: 'empty',
  label: 'Trace Pending',
  status: 'warning',
  duration: '0ms',
  inputSummary: '等待 sandbox run 或已有 trace。',
  outputSummary: '暂无步骤。',
  linkedEvidence: 'pending',
}

const selectedStepKey = shallowRef('provider-noop')
const selectedSandboxProvider = shallowRef(props.selectedProvider || 'local-rule')
const simulateFailure = shallowRef(false)
const simulateTimeout = shallowRef(false)
const sandboxInput = shallowRef('Java Spring Boot role with AI tooling evidence. Keep output local and require Human Review.')

const filteredProviders = computed(() => {
  const query = props.searchQuery.trim().toLowerCase()
  if (!query) return settings.value.providers
  return settings.value.providers.filter((provider) =>
    [
      provider.displayName,
      provider.providerMode,
      provider.configured ? 'configured' : 'not configured',
      provider.baseUrlConfigured ? 'base url configured' : 'base url not configured',
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
  ?? traceRun.value.pipeline[0]
  ?? EMPTY_STEP,
)

const configRows = computed(() => [
  ['local-rule', configCheck.value.localRuleAvailable ? 'available' : 'blocked'],
  ['OpenAI-compatible', configCheck.value.openAiCompatibleConfigured ? 'configured' : 'not configured'],
  ['DeepSeek', configCheck.value.deepSeekConfigured ? 'configured' : 'not configured'],
  ['real call enabled', String(configCheck.value.realCallEnabled)],
  ['raw response save', configCheck.value.rawResponseSave ? 'enabled' : 'disabled'],
  ['OpenAI key', configCheck.value.apiKeyStatus['openai-compatible'] ?? 'not configured'],
  ['DeepSeek key', configCheck.value.apiKeyStatus.deepseek ?? 'not configured'],
])

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

const sandboxRows = computed(() => sandboxResult.value
  ? [
      ['Selected provider', sandboxResult.value.providerMode],
      ['Final provider', sandboxResult.value.finalProvider],
      ['Fallback used', String(sandboxResult.value.fallbackUsed)],
      ['Fallback reason', sandboxResult.value.fallbackReason || 'local-rule selected'],
      ['Duration', `${sandboxResult.value.durationMs}ms`],
      ['Trace ID', sandboxResult.value.traceId],
      ['Risk flags', sandboxResult.value.riskFlags.join(' / ')],
      ['Human review required', String(sandboxResult.value.humanReviewRequired)],
    ]
  : [
      ['Selected provider', selectedSandboxProvider.value],
      ['Final provider', 'pending'],
      ['Fallback used', 'pending'],
      ['Fallback reason', '运行后显示'],
    ])

const providerNotice = computed(() => props.selectedProvider === 'local-rule'
  ? 'local-rule active · no external calls'
  : `${props.selectedProvider} stays no-op and falls back when sandboxed`)

watch(() => props.selectedProvider, (nextProvider) => {
  if (providerOptions.some((option) => option.id === nextProvider)) selectedSandboxProvider.value = nextProvider
})

watch(filteredSteps, (steps) => {
  if (steps.length && !steps.some((step) => step.key === selectedStepKey.value)) selectedStepKey.value = steps[0].key
})

async function submitSandboxRun() {
  await runSandbox({
    taskType: 'provider-sandbox',
    inputText: sandboxInput.value,
    providerMode: selectedSandboxProvider.value,
    simulateFailure: simulateFailure.value,
    simulateTimeout: simulateTimeout.value,
    actor: 'provider-settings-user',
    actorRole: 'Human reviewer',
  })
  selectedStepKey.value = 'fallback-decision'
}

function providerStatusClass(provider: ProviderCard) {
  if (provider.active) return 'active'
  if (provider.configured) return 'ready'
  return 'disabled'
}

function providerStatusLabel(provider: ProviderCard) {
  if (provider.active) return 'Active'
  if (provider.configured) return 'Configured'
  return 'Not configured'
}

function stepClass(status: ProviderTraceStepStatus) {
  return status.toLowerCase()
}
</script>

<template>
  <main class="main-content provider-trace-page">
    <section class="page-intro provider-trace-intro">
      <div class="intro-copy">
        <div class="breadcrumbs"><span>Provider 设置</span><ChevronRight :size="13" /><strong>Sandbox Resilience</strong></div>
        <h1>Provider 设置与证据链 <span class="title-mark provider-title-mark"><ShieldCheck :size="20" /></span></h1>
        <p>管理 Provider SPI、配置校验、沙箱降级、Trace Evidence 和 Human Review 状态；当前未发起真实外部模型调用。</p>
      </div>
      <div class="intro-actions">
        <div class="mode-note provider-trace-mode" :class="{ warning: props.selectedProvider !== 'local-rule' }">
          <span><Check v-if="props.selectedProvider === 'local-rule'" :size="13" /><SlidersHorizontal v-else :size="13" /></span>
          {{ providerNotice }}
        </div>
        <button type="button" class="secondary-button" :disabled="loading || running" @click="reload()">
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
        <small>local-rule/no-op · API Key masked · rawResponseSaved=false</small>
      </div>
    </section>

    <div class="provider-sandbox-grid">
      <section class="panel provider-config-panel">
        <header class="panel-header provider-panel-header">
          <div>
            <span class="panel-kicker">CONFIG CHECK</span>
            <h2>Provider Config Check</h2>
          </div>
          <CheckCircle2 :size="17" class="header-icon" />
        </header>
        <dl class="provider-config-grid">
          <div v-for="[label, value] in configRows" :key="label">
            <dt>{{ label }}</dt>
            <dd :class="{ safe: value === 'available' || value === 'false' || value === 'disabled', warning: value === 'not configured' }">{{ value }}</dd>
          </div>
        </dl>
        <div class="provider-boundary-note">
          <ShieldCheck :size="14" />
          <span>{{ configCheck.boundaryNotice }}</span>
        </div>
        <div class="provider-warning-list">
          <p v-for="warning in configCheck.warnings" :key="warning"><AlertTriangle :size="12" />{{ warning }}</p>
        </div>
      </section>

      <section class="panel provider-sandbox-panel">
        <header class="panel-header provider-panel-header">
          <div>
            <span class="panel-kicker">SANDBOX RUN</span>
            <h2>Provider 沙箱测试</h2>
          </div>
          <Route :size="17" class="header-icon" />
        </header>
        <div class="sandbox-form">
          <div class="provider-mode-picker" aria-label="Sandbox provider mode">
            <button
              v-for="option in providerOptions"
              :key="option.id"
              type="button"
              :class="{ selected: selectedSandboxProvider === option.id }"
              @click="selectedSandboxProvider = option.id"
            >
              {{ option.label }}
            </button>
          </div>
          <textarea v-model="sandboxInput" aria-label="Sandbox input" />
          <div class="sandbox-toggles">
            <label><input v-model="simulateFailure" type="checkbox" />simulate failure</label>
            <label><input v-model="simulateTimeout" type="checkbox" />simulate timeout</label>
          </div>
          <button type="button" class="primary-button sandbox-run-button" :disabled="running" @click="submitSandboxRun">
            <RefreshCw :size="14" :class="{ spinning: running }" />运行沙箱测试
          </button>
        </div>
        <dl class="sandbox-result-grid">
          <div v-for="[label, value] in sandboxRows" :key="label">
            <dt>{{ label }}</dt>
            <dd>{{ value }}</dd>
          </div>
        </dl>
        <p v-if="sandboxError" class="sandbox-error"><AlertTriangle :size="12" />{{ sandboxError }}</p>
      </section>
    </div>

    <div class="provider-top-grid">
      <section class="panel provider-status-panel">
        <header class="panel-header provider-panel-header">
          <div>
            <span class="panel-kicker">PROVIDER DESCRIPTORS</span>
            <h2>Provider 状态</h2>
          </div>
          <span class="count-chip">{{ filteredProviders.length }} 个</span>
        </header>
        <div class="provider-card-grid">
          <article
            v-for="provider in filteredProviders"
            :key="provider.providerMode"
            :class="['provider-card', providerStatusClass(provider)]"
          >
            <div class="provider-card-top">
              <span class="provider-symbol"><Code2 v-if="provider.providerMode === 'local-rule'" :size="17" /><DatabaseZap v-else :size="17" /></span>
              <div>
                <strong>{{ provider.displayName }}</strong>
                <small>API Key {{ provider.apiKeyStatus }}</small>
              </div>
              <em>{{ providerStatusLabel(provider) }}</em>
            </div>
            <dl class="provider-card-meta">
              <div><dt>Base URL</dt><dd>{{ provider.baseUrlConfigured ? 'configured' : 'not configured' }}</dd></div>
              <div><dt>Model</dt><dd>{{ provider.model }}</dd></div>
              <div><dt>Timeout</dt><dd>{{ provider.timeoutMs }}ms</dd></div>
              <div><dt>Fallback policy</dt><dd>{{ provider.fallbackPolicy }}</dd></div>
              <div><dt>Boundary notice</dt><dd>{{ provider.boundaryNotice }}</dd></div>
            </dl>
            <footer>
              <span><Timer :size="12" />Real call <b>{{ provider.realCallEnabled }}</b></span>
              <span><FileJson :size="12" />Raw save <b>{{ provider.rawResponseSave }}</b></span>
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
          <span :class="['pipeline-status', stepClass(selectedStep.status)]">{{ selectedStep.status }}</span>
        </header>

        <div class="trace-detail-layout">
          <section class="trace-detail-block jd-snippet">
            <h3>引用 JD / Sandbox 输入</h3>
            <blockquote>{{ traceRun.evidenceDetail.jdSnippet }}</blockquote>
            <p>{{ selectedStep.inputSummary }} -> {{ selectedStep.outputSummary }}</p>
          </section>

          <section class="trace-detail-block resume-evidence">
            <h3>引用证据</h3>
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
      <p>当前未发起真实外部模型调用；未配置 Provider 时必须 fallback；模型失败不伪装成功；所有输出仍需 Human Review。</p>
    </footer>
  </main>
</template>
