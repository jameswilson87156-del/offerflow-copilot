<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import {
  Check,
  ChevronRight,
  ClipboardList,
  FileDown,
  GitBranch,
  History,
  Link2,
  PlayCircle,
  RefreshCw,
  Save,
  ShieldCheck,
  SlidersHorizontal,
} from 'lucide-vue-next'
import EvidenceCanvas from '../components/EvidenceCanvas.vue'
import RequirementPanel from '../components/RequirementPanel.vue'
import ReviewPanel from '../components/ReviewPanel.vue'
import TimelinePanel from '../components/TimelinePanel.vue'
import { useDemoAnalysis } from '../composables/useDemoAnalysis'
import { useLocalActor } from '../composables/useLocalActor'
import type { JobIntakeStatus } from '../types'

const props = defineProps<{
  searchQuery: string
  selectedProvider: string
}>()

const {
  analysis,
  jobDetail,
  loading,
  actionLoading,
  error,
  source,
  searchQuery: localSearch,
  requirementGroups,
  evidenceMatches,
  reload,
  saveJob,
  parseJob,
  bindEvidence,
} = useDemoAnalysis()
const { can, permissionReason } = useLocalActor()

const form = reactive({
  title: '',
  company: '',
  city: '',
  jdText: '',
  sourceNote: '',
  humanNote: '手动更新 JD，继续使用 local-rule 解析和证据绑定。',
})

watch(() => props.searchQuery, (value) => {
  localSearch.value = value
}, { immediate: true })

watch(() => jobDetail.value.job, (job) => {
  form.title = job.title
  form.company = job.company
  form.city = job.city
  form.jdText = job.jdText
  form.sourceNote = job.sourceNote
}, { immediate: true })

const providerNotice = computed(() => props.selectedProvider === 'local-rule'
  ? '确定性规则模式 · 无外部调用'
  : `${props.selectedProvider} 尚未配置，本轮不会发起真实调用`)

const currentJob = computed(() => jobDetail.value.job)
const currentVersion = computed(() => jobDetail.value.currentParseVersion)
const parseVersions = computed(() => [...jobDetail.value.parseVersions].reverse())
const auditTrail = computed(() => [...jobDetail.value.auditTrail].reverse().slice(0, 5))
const bindingPreview = computed(() => jobDetail.value.evidenceBindings.slice(0, 4))
const apiSourceLabel = computed(() => source.value === 'api' ? 'H2 demo persistence' : 'fallback demo')

function statusClass(status: JobIntakeStatus | string) {
  return status.toLowerCase().replace(/\s+/g, '-')
}

async function handleSave() {
  if (!can('JD_UPDATE')) return
  await saveJob({
    title: form.title,
    company: form.company,
    city: form.city,
    jdText: form.jdText,
    sourceNote: form.sourceNote,
    humanNote: form.humanNote,
  })
}
</script>

<template>
  <main class="main-content">
    <section class="page-intro">
      <div class="intro-copy">
        <div class="breadcrumbs"><span>JD 分析台</span><ChevronRight :size="13" /><strong>结构化入库</strong></div>
        <h1>JD 证据匹配工作台 <span class="title-mark"><ShieldCheck :size="20" /></span></h1>
        <p>手动粘贴 JD，保存解析版本，绑定简历证据，并保留可追溯的审计链路。</p>
      </div>
      <div class="intro-actions">
        <div class="mode-note" :class="{ warning: props.selectedProvider !== 'local-rule' }">
          <span><Check v-if="props.selectedProvider === 'local-rule'" :size="13" /><SlidersHorizontal v-else :size="13" /></span>
          {{ providerNotice }}
        </div>
        <button type="button" class="secondary-button" :disabled="loading" @click="reload">
          <RefreshCw :size="15" :class="{ spinning: loading }" />刷新匹配
        </button>
        <button type="button" class="secondary-button" title="本轮不提供真实导出" disabled>
          <FileDown :size="15" />导出报告
        </button>
      </div>
    </section>

    <section class="job-context" aria-label="当前岗位上下文">
      <div>
        <span class="job-status">手动粘贴</span>
        <strong>{{ currentJob.title }}</strong>
        <span>{{ currentJob.company }} · {{ currentJob.city }}</span>
      </div>
      <div class="job-meta">
        <span>sourceType: {{ currentJob.sourceType }}</span>
        <span>v{{ currentVersion?.versionNo ?? 0 }} {{ currentVersion?.parserMode ?? 'not parsed' }}</span>
        <span class="jd-status-pill" :class="statusClass(currentJob.status)">{{ currentJob.status }}</span>
        <span>更新于 {{ currentJob.updatedAt }}</span>
      </div>
    </section>

    <section class="jd-intake-workflow" aria-label="结构化 JD Intake 工作流">
      <article class="panel jd-intake-panel">
        <header class="panel-header jd-panel-header">
          <div>
            <span class="panel-kicker">STRUCTURED JD INTAKE</span>
            <h2>手动粘贴 JD</h2>
          </div>
          <span class="source-pill">{{ apiSourceLabel }}</span>
        </header>
        <div class="jd-form-grid">
          <label>
            <span>岗位标题</span>
            <input v-model="form.title" type="text" autocomplete="off" />
          </label>
          <label>
            <span>公司</span>
            <input v-model="form.company" type="text" autocomplete="off" />
          </label>
          <label>
            <span>城市</span>
            <input v-model="form.city" type="text" autocomplete="off" />
          </label>
          <label class="wide">
            <span>来源备注</span>
            <input v-model="form.sourceNote" type="text" autocomplete="off" />
          </label>
          <label class="wide jd-textarea-label">
            <span>JD 文本</span>
            <textarea v-model="form.jdText" rows="7" />
          </label>
          <label class="wide">
            <span>人工备注</span>
            <input v-model="form.humanNote" type="text" autocomplete="off" />
          </label>
        </div>
        <div class="jd-action-row">
          <button type="button" class="primary-action" :disabled="!!actionLoading || !can('JD_UPDATE')" :title="can('JD_UPDATE') ? '' : permissionReason('JD_UPDATE')" @click="handleSave">
            <Save :size="14" />保存 JD
          </button>
          <button type="button" class="secondary-button" :disabled="!!actionLoading || !can('JD_PARSE')" :title="can('JD_PARSE') ? '' : permissionReason('JD_PARSE')" @click="parseJob()">
            <PlayCircle :size="14" />重新解析
          </button>
          <button type="button" class="secondary-button" :disabled="!!actionLoading || !can('JD_BIND_EVIDENCE')" :title="can('JD_BIND_EVIDENCE') ? '' : permissionReason('JD_BIND_EVIDENCE')" @click="bindEvidence()">
            <Link2 :size="14" />生成证据绑定
          </button>
        </div>
        <p v-if="!can('JD_UPDATE')" class="permission-inline-note">{{ permissionReason('JD_UPDATE') }}</p>
        <p v-if="error" class="jd-error">{{ error }}</p>
        <p class="jd-boundary">{{ jobDetail.boundaryNotice }}</p>
      </article>

      <div class="jd-side-panels">
        <article class="panel parse-version-panel">
          <header class="panel-header jd-panel-header">
            <div>
              <span class="panel-kicker">PARSE HISTORY</span>
              <h2>解析版本历史</h2>
            </div>
            <GitBranch :size="16" />
          </header>
          <div class="parse-version-list">
            <article v-for="version in parseVersions" :key="version.id" class="parse-version-item">
              <div>
                <strong>v{{ version.versionNo }} {{ version.parserMode }}</strong>
                <span>{{ version.createdAt }} · {{ version.schemaVersion }}</span>
              </div>
              <small>{{ version.parseStatus }}</small>
              <p>{{ version.keywords.join(' / ') }}</p>
            </article>
          </div>
        </article>

        <article class="panel binding-summary-panel">
          <header class="panel-header jd-panel-header">
            <div>
              <span class="panel-kicker">EVIDENCE BINDING</span>
              <h2>证据绑定摘要</h2>
            </div>
            <span class="count-chip">{{ jobDetail.evidenceBindings.length }} 条</span>
          </header>
          <div class="binding-mini-list">
            <article v-for="binding in bindingPreview" :key="binding.id" class="binding-mini-card">
              <span class="strength-badge" :class="binding.evidenceStrength">{{ binding.evidenceStrength }}</span>
              <div>
                <strong>{{ binding.requirementLabel }}</strong>
                <p>{{ binding.evidenceProject }} · {{ binding.evidenceSource }}</p>
              </div>
            </article>
          </div>
        </article>

        <article class="panel jd-audit-trail-panel">
          <header class="panel-header jd-panel-header">
            <div>
              <span class="panel-kicker">JD AUDIT TRAIL</span>
              <h2>JD Audit Trail</h2>
            </div>
            <History :size="16" />
          </header>
          <div class="jd-audit-list">
            <article v-for="event in auditTrail" :key="event.id" class="jd-audit-event">
              <span class="audit-dot" />
              <div>
                <strong>{{ event.actionLabel }}</strong>
                <small>{{ event.previousStatus }} -> {{ event.nextStatus }} · {{ event.actor }}</small>
                <p>{{ event.humanNote }}</p>
                <em>{{ event.createdAt }}</em>
              </div>
            </article>
          </div>
        </article>
      </div>
    </section>

    <div class="jd-local-rule-note">
      <ClipboardList :size="14" />
      <span>当前只支持用户手动粘贴 JD；解析与绑定均为 local-rule，不接招聘平台 API，不爬取网页。</span>
    </div>

    <div class="workbench-grid">
      <RequirementPanel :groups="requirementGroups" />
      <EvidenceCanvas :matches="evidenceMatches" :score="analysis.score" />
      <ReviewPanel :preparation="analysis.interviewPreparation" :review="analysis.humanReview" />
    </div>

    <TimelinePanel :timeline="analysis.timeline" :resumes="analysis.recommendedResumes" />

    <footer class="tag-footer">
      <span v-for="tag in analysis.technicalTags" :key="tag">{{ tag }}</span>
      <p>{{ analysis.disclaimer }}</p>
    </footer>
  </main>
</template>
