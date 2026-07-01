<script setup lang="ts">
import { CircleHelp, Network } from 'lucide-vue-next'
import type { EvidenceCoverageData } from '../../types'

const props = defineProps<{ coverage: EvidenceCoverageData }>()
</script>

<template>
  <section class="panel coverage-map" aria-labelledby="coverage-title">
    <header class="coverage-header">
      <div><Network :size="17" /><h2 id="coverage-title">Evidence Coverage Map</h2><span>证据覆盖地图</span></div>
      <div class="coverage-legend">
        <span><i class="strong" />强支撑</span>
        <span><i class="medium" />中支撑</span>
        <span><i class="weak" />弱支撑</span>
        <span><i class="none" />暂无证据</span>
      </div>
    </header>

    <div class="coverage-grid">
      <article v-for="item in props.coverage.items" :key="item.skill" class="coverage-item" :class="item.level">
        <div class="coverage-name">
          <strong>{{ item.skill }}</strong>
          <span>{{ item.level }}</span>
        </div>
        <div class="coverage-bar" :aria-label="`${item.skill} 覆盖度 ${item.score}%`">
          <i :style="{ width: `${item.score}%` }" />
        </div>
        <div class="coverage-projects">
          <span v-for="project in item.supportingProjects" :key="project" :title="project">{{ project.charAt(0) }}</span>
          <small>{{ item.supportingProjects.length }} 个项目</small>
        </div>
        <p>{{ item.gap }}</p>
      </article>
    </div>

    <footer class="coverage-note"><CircleHelp :size="13" />{{ props.coverage.note }}</footer>
  </section>
</template>
