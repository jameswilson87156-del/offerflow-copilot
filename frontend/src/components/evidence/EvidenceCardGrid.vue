<script setup lang="ts">
import { BadgeCheck, Box, BrainCircuit, FolderGit2, Layers3, ShieldCheck } from 'lucide-vue-next'
import type { EvidenceItem } from '../../types'

const props = defineProps<{
  items: EvidenceItem[]
  selectedId: string
}>()

const emit = defineEmits<{ select: [id: string] }>()
const projectIcons = [Box, BrainCircuit, Layers3, FolderGit2]
</script>

<template>
  <section class="panel evidence-gallery" aria-labelledby="gallery-title">
    <header class="panel-header evidence-panel-header gallery-header">
      <div>
        <span class="panel-kicker">VERIFIABLE PROJECTS</span>
        <h2 id="gallery-title">项目证据</h2>
      </div>
      <div class="gallery-legend">
        <span><i class="verified" />人工确认</span>
        <span><i class="review" />待补强</span>
      </div>
    </header>

    <div v-if="props.items.length" class="evidence-card-grid">
      <button
        v-for="(item, index) in props.items"
        :key="item.id"
        type="button"
        class="library-card"
        :class="{ selected: props.selectedId === item.id }"
        :aria-pressed="props.selectedId === item.id"
        @click="emit('select', item.id)"
      >
        <div class="library-card-top">
          <span class="project-symbol" :class="`project-tone-${index}`">
            <component :is="projectIcons[index]" :size="19" />
          </span>
          <div class="library-card-title">
            <small>PROJECT 0{{ index + 1 }}</small>
            <strong>{{ item.projectName }}</strong>
          </div>
          <span class="credibility" :class="item.credibility">{{ item.credibility }}支撑</span>
        </div>

        <p class="library-summary">{{ item.summary }}</p>

        <div class="ability-tags">
          <span v-for="tag in item.abilityTags" :key="tag">{{ tag }}</span>
        </div>

        <div class="library-divider" />

        <dl class="card-evidence-meta">
          <div>
            <dt>证据来源</dt>
            <dd>{{ item.evidenceSources.slice(0, 3).join(' · ') }}</dd>
          </div>
          <div>
            <dt>可匹配要求</dt>
            <dd>{{ item.matchableRequirements.slice(0, 2).join(' / ') }}</dd>
          </div>
        </dl>

        <footer class="library-card-footer">
          <span :class="item.humanReviewStatus === 'Confirmed' ? 'confirmed' : 'needs-review'">
            <BadgeCheck v-if="item.humanReviewStatus === 'Confirmed'" :size="13" />
            <ShieldCheck v-else :size="13" />
            {{ item.humanReviewStatus === 'Confirmed' ? '已人工确认' : '需要人工复核' }}
          </span>
          <small>更新 {{ item.updatedAt }}</small>
        </footer>
      </button>
    </div>
    <div v-else class="empty-state gallery-empty">没有匹配的项目证据</div>
  </section>
</template>
