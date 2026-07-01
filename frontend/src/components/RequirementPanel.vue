<script setup lang="ts">
import { Braces, CircleAlert, CircleCheck, Sparkles } from 'lucide-vue-next'
import type { RequirementGroup } from '../types'

const props = defineProps<{ groups: RequirementGroup[] }>()

function groupIcon(key: RequirementGroup['key']) {
  if (key === 'core') return Braces
  if (key === 'bonus') return Sparkles
  return CircleAlert
}
</script>

<template>
  <section class="panel requirements-panel" aria-labelledby="requirements-title">
    <header class="panel-header">
      <div>
        <span class="panel-kicker">JD STRUCTURE</span>
        <h2 id="requirements-title">岗位要求拆解</h2>
      </div>
      <span class="count-chip">{{ props.groups.reduce((sum, group) => sum + group.items.length, 0) }} 项</span>
    </header>

    <div v-if="props.groups.length" class="requirement-groups">
      <section v-for="group in props.groups" :key="group.key" class="requirement-group" :class="group.key">
        <div class="requirement-group-title">
          <component :is="groupIcon(group.key)" :size="15" />
          <strong>{{ group.label }}</strong>
          <span>{{ group.items.length }}</span>
        </div>
        <article v-for="item in group.items" :key="item.id" class="requirement-item">
          <div class="requirement-main">
            <CircleCheck v-if="group.key !== 'risk'" :size="15" />
            <CircleAlert v-else :size="15" />
            <strong>{{ item.title }}</strong>
            <span class="level-tag">{{ item.level }}</span>
          </div>
          <p>{{ item.description }}</p>
        </article>
      </section>
    </div>
    <div v-else class="empty-state">没有匹配的岗位要求</div>

    <footer class="panel-hint">
      <span>JD-042</span>
      <p>示例内容 · 由 local-rule 拆解</p>
    </footer>
  </section>
</template>
