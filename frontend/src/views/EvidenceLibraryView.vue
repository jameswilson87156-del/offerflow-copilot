<script setup lang="ts">
import { computed, reactive, shallowRef, watch } from 'vue'
import { Check, ChevronRight, FolderCheck, Plus, RefreshCw, Save, ShieldCheck, SlidersHorizontal, X } from 'lucide-vue-next'
import EvidenceCardGrid from '../components/evidence/EvidenceCardGrid.vue'
import EvidenceCategories from '../components/evidence/EvidenceCategories.vue'
import EvidenceCoverageMap from '../components/evidence/EvidenceCoverageMap.vue'
import EvidenceDetailPanel from '../components/evidence/EvidenceDetailPanel.vue'
import { useEvidenceLibrary } from '../composables/useEvidenceLibrary'
import { useLocalActor } from '../composables/useLocalActor'
import type { EvidenceItem, EvidenceMutationPayload, PermissionAction } from '../types'

const props = defineProps<{
  searchQuery: string
  selectedProvider: string
}>()

const {
  library,
  coverage,
  detail,
  loading,
  detailLoading,
  actionLoading,
  source,
  reload,
  loadDetail,
  createEvidence,
  updateEvidence,
  confirmEvidence,
  returnToDraft,
  archiveEvidence,
  restoreEvidence,
} = useEvidenceLibrary()
const { can, permissionReason } = useLocalActor()

const activeCategory = shallowRef('all')
const selectedId = shallowRef('evidence-mcp')
const editOpen = shallowRef(false)

const editForm = reactive({
  id: '',
  projectName: '',
  summary: '',
  abilityTags: '',
  evidenceSources: '',
  credibility: '中',
  matchableRequirements: '',
  boundaryNote: '',
  relatedSkills: '',
  suitableRoles: '',
  interviewAnswers: '',
  riskBoundaries: '',
  humanNote: '需要保持作品集级表达，不保存真实隐私，不声称真实客户或生产级数据。',
})

const categoryTerms: Record<string, string[]> = {
  java: ['Java', 'Spring Boot'],
  'ai-app': ['AI 应用', 'RAG', 'AI Workflow'],
  agent: ['AI Agent', 'Tool'],
  rag: ['RAG', 'Knowledge'],
  'ai-coding': ['AI Coding'],
  frontend: ['前端', 'Vue 3'],
  ci: ['CI', '部署', 'GitHub Actions'],
  trace: ['Trace', 'Human Review'],
}

const filteredItems = computed(() => {
  const query = props.searchQuery.trim().toLowerCase()
  const terms = categoryTerms[activeCategory.value] ?? []
  return library.value.items.filter((item) => {
    const corpus = [
      item.projectName,
      item.summary,
      item.status,
      ...item.abilityTags,
      ...item.evidenceSources,
      ...item.matchableRequirements,
      ...item.detail.relatedSkills,
      ...item.detail.suitableRoles,
    ].join(' ').toLowerCase()
    const categoryMatch = activeCategory.value === 'all'
      || (activeCategory.value === 'archived' && item.status === 'Archived')
      || (activeCategory.value !== 'archived' && item.status !== 'Archived' && terms.some((term) => corpus.includes(term.toLowerCase())))
    return categoryMatch && (!query || corpus.includes(query))
  })
})

const selectedBaseItem = computed<EvidenceItem>(() =>
  filteredItems.value.find((item) => item.id === selectedId.value)
  ?? filteredItems.value[0]
  ?? library.value.items[0],
)

const selectedItem = computed<EvidenceItem>(() =>
  detail.value?.item.id === selectedBaseItem.value?.id ? detail.value.item : selectedBaseItem.value,
)

const auditTrail = computed(() => detail.value?.item.id === selectedItem.value?.id ? detail.value.auditTrail : [])
const boundaryNotice = computed(() => detail.value?.boundaryNotice ?? '证据默认为草稿，人工确认后才进入已确认。')
const confirmedCount = computed(() => library.value.items.filter((item) => item.status === 'Confirmed').length)
const draftCount = computed(() => library.value.items.filter((item) => item.status === 'Draft' || item.status === 'Returned').length)

const providerNotice = computed(() => props.selectedProvider === 'local-rule'
  ? '证据索引使用 local-rule · 无外部调用'
  : `${props.selectedProvider} 未配置，当前仍使用 local-rule`)

const evidencePermissions: Record<'confirm' | 'return' | 'archive' | 'restore' | 'create' | 'edit', PermissionAction> = {
  confirm: 'EVIDENCE_CONFIRM',
  return: 'EVIDENCE_UPDATE',
  archive: 'EVIDENCE_ARCHIVE',
  restore: 'EVIDENCE_RESTORE',
  create: 'EVIDENCE_CREATE',
  edit: 'EVIDENCE_UPDATE',
}

const createReason = computed(() => permissionReason('EVIDENCE_CREATE'))
const saveReason = computed(() => permissionReason(editForm.id ? 'EVIDENCE_UPDATE' : 'EVIDENCE_CREATE'))

watch(filteredItems, (items) => {
  if (items.length && !items.some((item) => item.id === selectedId.value)) selectedId.value = items[0].id
})

watch(selectedId, (id) => {
  editOpen.value = false
  if (id) void loadDetail(id)
}, { immediate: true })

watch(() => library.value.items.map((item) => `${item.id}:${item.updatedAt}:${item.status}`).join('|'), () => {
  if (selectedId.value) void loadDetail(selectedId.value)
})

function openCreate() {
  if (!can('EVIDENCE_CREATE')) return
  editForm.id = ''
  editForm.projectName = 'New Portfolio Evidence'
  editForm.summary = '待补充的作品集级项目证据草稿。'
  editForm.abilityTags = 'Java 后端, Trace / Human Review'
  editForm.evidenceSources = 'README, 截图'
  editForm.credibility = '中'
  editForm.matchableRequirements = 'Spring Boot, Evidence Binding'
  editForm.boundaryNote = 'Draft 阶段不得声称真实客户、真实用户或生产级数据。'
  editForm.relatedSkills = 'Java, Spring Boot, Trace Evidence'
  editForm.suitableRoles = 'Java 后端实习生, AI 应用开发实习生'
  editForm.interviewAnswers = '如何验证项目证据来源？'
  editForm.riskBoundaries = '不保存真实隐私, 不虚构真实客户, 不声称生产级能力'
  editForm.humanNote = '创建 Draft 证据，等待人工确认。'
  editOpen.value = true
}

function openEdit() {
  const item = selectedItem.value
  if (item.status === 'Archived' || !can('EVIDENCE_UPDATE')) return
  editForm.id = item.id
  editForm.projectName = item.projectName
  editForm.summary = item.summary
  editForm.abilityTags = item.abilityTags.join(', ')
  editForm.evidenceSources = item.evidenceSources.join(', ')
  editForm.credibility = item.credibility
  editForm.matchableRequirements = item.matchableRequirements.join(', ')
  editForm.boundaryNote = item.detail.riskBoundaries.join('；')
  editForm.relatedSkills = item.detail.relatedSkills.join(', ')
  editForm.suitableRoles = item.detail.suitableRoles.join(', ')
  editForm.interviewAnswers = item.detail.interviewAnswers.join('；')
  editForm.riskBoundaries = item.detail.riskBoundaries.join('；')
  editForm.humanNote = '保存为 Draft，重新走人工确认。'
  editOpen.value = true
}

async function saveDraft() {
  const permission = editForm.id ? 'EVIDENCE_UPDATE' : 'EVIDENCE_CREATE'
  if (!can(permission)) return
  const payload = formPayload()
  const result = editForm.id
    ? await updateEvidence(editForm.id, payload)
    : await createEvidence(payload)
  selectedId.value = result.item.id
  editOpen.value = false
  await loadDetail(result.item.id)
}

async function runAction(action: 'confirm' | 'return' | 'archive' | 'restore') {
  const permission = evidencePermissions[action]
  if (!can(permission)) return
  const payload: EvidenceMutationPayload = {
    humanNote: editForm.humanNote,
    targetStatus: action === 'restore' ? 'Draft' : undefined,
  }
  if (action === 'confirm') await confirmEvidence(selectedItem.value.id, payload)
  if (action === 'return') await returnToDraft(selectedItem.value.id, payload)
  if (action === 'archive') await archiveEvidence(selectedItem.value.id, payload)
  if (action === 'restore') await restoreEvidence(selectedItem.value.id, payload)
  await loadDetail(selectedItem.value.id)
}

async function reloadAll() {
  await reload()
  if (selectedId.value) await loadDetail(selectedId.value)
}

function formPayload(): EvidenceMutationPayload {
  return {
    humanNote: editForm.humanNote,
    projectName: editForm.projectName,
    summary: editForm.summary,
    abilityTags: csv(editForm.abilityTags),
    evidenceSources: csv(editForm.evidenceSources),
    credibility: editForm.credibility,
    matchableRequirements: csv(editForm.matchableRequirements),
    boundaryNote: editForm.boundaryNote,
    relatedSkills: csv(editForm.relatedSkills),
    suitableRoles: csv(editForm.suitableRoles),
    interviewAnswers: semi(editForm.interviewAnswers),
    riskBoundaries: semi(editForm.riskBoundaries),
  }
}

function csv(value: string) {
  return value.split(',').map((item) => item.trim()).filter(Boolean)
}

function semi(value: string) {
  return value.split(/[；;]/).map((item) => item.trim()).filter(Boolean)
}
</script>

<template>
  <main class="main-content evidence-page">
    <section class="page-intro evidence-intro">
      <div class="intro-copy">
        <div class="breadcrumbs"><span>简历证据库</span><ChevronRight :size="13" /><strong>可编辑证据资产</strong></div>
        <h1>简历证据库 <span class="title-mark evidence-title-mark"><FolderCheck :size="20" /></span></h1>
        <p>把项目经历、技术栈、截图、README、测试与部署记录维护为可确认、可追溯、可回滚的求职证据。</p>
      </div>
      <div class="intro-actions">
        <div class="mode-note evidence-mode" :class="{ warning: props.selectedProvider !== 'local-rule' }">
          <span><Check v-if="props.selectedProvider === 'local-rule'" :size="13" /><SlidersHorizontal v-else :size="13" /></span>
          {{ providerNotice }}
        </div>
        <button type="button" class="secondary-button" :disabled="loading || detailLoading" @click="reloadAll">
          <RefreshCw :size="15" :class="{ spinning: loading || detailLoading }" />刷新证据
        </button>
        <button type="button" class="primary-evidence-button" :disabled="actionLoading || !can('EVIDENCE_CREATE')" :title="can('EVIDENCE_CREATE') ? '' : createReason" @click="openCreate"><Plus :size="15" />新建草稿</button>
      </div>
    </section>

    <section class="evidence-context" aria-label="证据库状态">
      <div class="evidence-context-main">
        <span class="evidence-ready-icon"><ShieldCheck :size="16" /></span>
        <strong>{{ library.total }} 个匿名化项目证据</strong>
        <span>{{ confirmedCount }} 个已确认</span>
        <span>{{ draftCount }} 个草稿 / 已退回</span>
      </div>
      <div class="evidence-context-meta">
        <span :class="source">{{ source === 'api' ? 'LOCAL API LIVE' : 'DEMO SNAPSHOT' }}</span>
        <small>H2 demo persistence · mock/local-rule</small>
      </div>
    </section>

    <div class="evidence-library-layout">
      <EvidenceCategories
        :categories="library.categories"
        :active-key="activeCategory"
        @select="activeCategory = $event"
      />
      <EvidenceCardGrid
        :items="filteredItems"
        :selected-id="selectedItem.id"
        @select="selectedId = $event"
      />
      <div class="evidence-side-stack">
        <EvidenceDetailPanel
          :item="selectedItem"
          :audit-trail="auditTrail"
          :boundary-notice="boundaryNotice"
          :busy="actionLoading || detailLoading"
          :can-edit="can('EVIDENCE_UPDATE')"
          :can-confirm="can('EVIDENCE_CONFIRM')"
          :can-return-to-draft="can('EVIDENCE_UPDATE')"
          :can-archive="can('EVIDENCE_ARCHIVE')"
          :can-restore="can('EVIDENCE_RESTORE')"
          :edit-reason="permissionReason('EVIDENCE_UPDATE')"
          :confirm-reason="permissionReason('EVIDENCE_CONFIRM')"
          :return-reason="permissionReason('EVIDENCE_UPDATE')"
          :archive-reason="permissionReason('EVIDENCE_ARCHIVE')"
          :restore-reason="permissionReason('EVIDENCE_RESTORE')"
          @edit="openEdit"
          @confirm="runAction('confirm')"
          @return-to-draft="runAction('return')"
          @archive="runAction('archive')"
          @restore="runAction('restore')"
        />

        <aside v-if="editOpen" class="panel evidence-edit-panel" aria-label="Evidence Edit Drawer">
          <header class="panel-header evidence-panel-header">
            <div>
              <span class="panel-kicker">EDIT DRAFT</span>
              <h2>{{ editForm.id ? '编辑证据草稿' : '新建证据草稿' }}</h2>
            </div>
            <button type="button" class="icon-only-button" @click="editOpen = false"><X :size="15" /></button>
          </header>
          <div class="evidence-edit-form">
            <label>项目名称<input v-model="editForm.projectName" type="text" /></label>
            <label>项目摘要<textarea v-model="editForm.summary" rows="3" /></label>
            <label>能力标签<input v-model="editForm.abilityTags" type="text" /></label>
            <label>证据来源<input v-model="editForm.evidenceSources" type="text" /></label>
            <label>可信度
              <select v-model="editForm.credibility">
                <option>强</option>
                <option>中</option>
                <option>弱</option>
              </select>
            </label>
            <label>可匹配岗位要求<input v-model="editForm.matchableRequirements" type="text" /></label>
            <label>关联技能<input v-model="editForm.relatedSkills" type="text" /></label>
            <label>适合岗位<input v-model="editForm.suitableRoles" type="text" /></label>
            <label>可支撑面试回答<textarea v-model="editForm.interviewAnswers" rows="2" /></label>
            <label>风险边界<textarea v-model="editForm.riskBoundaries" rows="2" /></label>
            <label>人工备注<textarea v-model="editForm.humanNote" rows="2" /></label>
          </div>
          <footer class="evidence-edit-actions">
            <button type="button" class="secondary-button" @click="editOpen = false">取消</button>
            <button type="button" class="primary-evidence-button" :disabled="actionLoading || !can(editForm.id ? 'EVIDENCE_UPDATE' : 'EVIDENCE_CREATE')" :title="can(editForm.id ? 'EVIDENCE_UPDATE' : 'EVIDENCE_CREATE') ? '' : saveReason" @click="saveDraft"><Save :size="15" />保存草稿</button>
          </footer>
        </aside>
      </div>
    </div>

    <EvidenceCoverageMap :coverage="coverage" />

    <footer class="evidence-disclaimer">
      <ShieldCheck :size="13" />
      <span>{{ library.disclaimer }}</span>
    </footer>
  </main>
</template>
