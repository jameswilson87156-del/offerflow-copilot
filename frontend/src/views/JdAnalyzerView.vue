<script setup lang="ts">
import { computed, watch } from 'vue'
import { Check, ChevronRight, FileDown, RefreshCw, ShieldCheck, SlidersHorizontal } from 'lucide-vue-next'
import EvidenceCanvas from '../components/EvidenceCanvas.vue'
import RequirementPanel from '../components/RequirementPanel.vue'
import ReviewPanel from '../components/ReviewPanel.vue'
import TimelinePanel from '../components/TimelinePanel.vue'
import { useDemoAnalysis } from '../composables/useDemoAnalysis'

const props = defineProps<{
  searchQuery: string
  selectedProvider: string
}>()

const {
  analysis,
  loading,
  searchQuery: localSearch,
  requirementGroups,
  evidenceMatches,
  reload,
} = useDemoAnalysis()

watch(() => props.searchQuery, (value) => {
  localSearch.value = value
}, { immediate: true })

const providerNotice = computed(() => props.selectedProvider === 'local-rule'
  ? '确定性规则模式 · 无外部调用'
  : `${props.selectedProvider} 尚未配置，本轮不会发起真实调用`)
</script>

<template>
  <main class="main-content">
    <section class="page-intro">
      <div class="intro-copy">
        <div class="breadcrumbs"><span>JD 分析台</span><ChevronRight :size="13" /><strong>证据匹配</strong></div>
        <h1>JD 证据匹配工作台 <span class="title-mark"><ShieldCheck :size="20" /></span></h1>
        <p>解析岗位要求，匹配简历证据与项目证明，生成可复核的面试准备材料。</p>
      </div>
      <div class="intro-actions">
        <div class="mode-note" :class="{ warning: props.selectedProvider !== 'local-rule' }">
          <span><Check v-if="props.selectedProvider === 'local-rule'" :size="13" /><SlidersHorizontal v-else :size="13" /></span>
          {{ providerNotice }}
        </div>
        <button type="button" class="secondary-button" :disabled="loading" @click="reload">
          <RefreshCw :size="15" :class="{ spinning: loading }" />刷新匹配
        </button>
        <button type="button" class="secondary-button" title="P1 暂不提供真实导出" disabled>
          <FileDown :size="15" />导出报告
        </button>
      </div>
    </section>

    <section class="job-context" aria-label="当前岗位上下文">
      <div>
        <span class="job-status">DEMO JD</span>
        <strong>{{ analysis.job.title }}</strong>
        <span>{{ analysis.job.company }}</span>
      </div>
      <div class="job-meta">
        <span>来源：{{ analysis.job.source }}</span>
        <span>更新于 {{ analysis.job.updatedAt }}</span>
      </div>
    </section>

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
