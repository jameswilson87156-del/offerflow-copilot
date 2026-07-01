<script setup lang="ts">
import { computed, shallowRef, watch } from 'vue'
import { Check, ChevronRight, FolderCheck, Plus, RefreshCw, ShieldCheck, SlidersHorizontal, Upload } from 'lucide-vue-next'
import EvidenceCardGrid from '../components/evidence/EvidenceCardGrid.vue'
import EvidenceCategories from '../components/evidence/EvidenceCategories.vue'
import EvidenceCoverageMap from '../components/evidence/EvidenceCoverageMap.vue'
import EvidenceDetailPanel from '../components/evidence/EvidenceDetailPanel.vue'
import { useEvidenceLibrary } from '../composables/useEvidenceLibrary'
import type { EvidenceItem } from '../types'

const props = defineProps<{
  searchQuery: string
  selectedProvider: string
}>()

const { library, coverage, loading, source, reload } = useEvidenceLibrary()
const activeCategory = shallowRef('all')
const selectedId = shallowRef('evidence-mcp')

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
      ...item.abilityTags,
      ...item.evidenceSources,
      ...item.matchableRequirements,
      ...item.detail.relatedSkills,
      ...item.detail.suitableRoles,
    ].join(' ').toLowerCase()
    const categoryMatch = activeCategory.value === 'all' || terms.some((term) => corpus.includes(term.toLowerCase()))
    return categoryMatch && (!query || corpus.includes(query))
  })
})

const selectedItem = computed<EvidenceItem>(() =>
  filteredItems.value.find((item) => item.id === selectedId.value)
  ?? filteredItems.value[0]
  ?? library.value.items[0],
)

watch(filteredItems, (items) => {
  if (items.length && !items.some((item) => item.id === selectedId.value)) selectedId.value = items[0].id
})

const providerNotice = computed(() => props.selectedProvider === 'local-rule'
  ? '证据索引由 local-rule 生成 · 无外部调用'
  : `${props.selectedProvider} 未配置，当前仍使用 local-rule`)

const confirmedCount = computed(() => library.value.items.filter((item) => item.humanReviewStatus === 'Confirmed').length)
</script>

<template>
  <main class="main-content evidence-page">
    <section class="page-intro evidence-intro">
      <div class="intro-copy">
        <div class="breadcrumbs"><span>简历证据库</span><ChevronRight :size="13" /><strong>可验证项目资产</strong></div>
        <h1>简历证据库 <span class="title-mark evidence-title-mark"><FolderCheck :size="20" /></span></h1>
        <p>把项目经历、技术栈、截图、README、测试与部署记录沉淀为可检索、可匹配、可复核的求职证据。</p>
      </div>
      <div class="intro-actions">
        <div class="mode-note evidence-mode" :class="{ warning: props.selectedProvider !== 'local-rule' }">
          <span><Check v-if="props.selectedProvider === 'local-rule'" :size="13" /><SlidersHorizontal v-else :size="13" /></span>
          {{ providerNotice }}
        </div>
        <button type="button" class="secondary-button" :disabled="loading" @click="reload">
          <RefreshCw :size="15" :class="{ spinning: loading }" />刷新证据
        </button>
        <button type="button" class="secondary-button" title="P2A 不保存上传内容" disabled><Upload :size="15" />导入证据</button>
        <button type="button" class="primary-evidence-button" title="P2A 不提供持久化" disabled><Plus :size="15" />新建证据</button>
      </div>
    </section>

    <section class="evidence-context" aria-label="证据库状态">
      <div class="evidence-context-main">
        <span class="evidence-ready-icon"><ShieldCheck :size="16" /></span>
        <strong>{{ library.total }} 个匿名化项目证据</strong>
        <span>{{ confirmedCount }} 个已人工确认</span>
      </div>
      <div class="evidence-context-meta">
        <span :class="source">{{ source === 'api' ? 'LOCAL API LIVE' : 'DEMO SNAPSHOT' }}</span>
        <small>仅 mock/local-rule · 无数据库</small>
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
      <EvidenceDetailPanel :item="selectedItem" />
    </div>

    <EvidenceCoverageMap :coverage="coverage" />

    <footer class="evidence-disclaimer">
      <ShieldCheck :size="13" />
      <span>{{ library.disclaimer }}</span>
    </footer>
  </main>
</template>
