<script setup lang="ts">
import {
  Bot,
  Boxes,
  BrainCircuit,
  Code2,
  FolderCheck,
  GitBranch,
  Layers3,
  ServerCog,
  Workflow,
} from 'lucide-vue-next'
import type { EvidenceCategory } from '../../types'

const props = defineProps<{
  categories: EvidenceCategory[]
  activeKey: string
}>()

const emit = defineEmits<{ select: [key: string] }>()
const icons = [FolderCheck, ServerCog, BrainCircuit, Bot, Layers3, Code2, Boxes, Workflow, GitBranch]
</script>

<template>
  <section class="panel category-rail" aria-labelledby="category-title">
    <header class="panel-header evidence-panel-header">
      <div>
        <span class="panel-kicker">CAPABILITY INDEX</span>
        <h2 id="category-title">能力分类</h2>
      </div>
      <span class="count-chip">{{ props.categories.length }}</span>
    </header>
    <div class="category-list">
      <button
        v-for="(category, index) in props.categories"
        :key="category.key"
        type="button"
        :class="{ active: props.activeKey === category.key }"
        :aria-pressed="props.activeKey === category.key"
        @click="emit('select', category.key)"
      >
        <span class="category-icon"><component :is="icons[index]" :size="15" /></span>
        <strong>{{ category.label }}</strong>
        <em>{{ category.count }}</em>
      </button>
    </div>
    <footer class="category-footnote">
      <GitBranch :size="14" />
      <span>分类来自 local-rule 标签映射</span>
    </footer>
  </section>
</template>
