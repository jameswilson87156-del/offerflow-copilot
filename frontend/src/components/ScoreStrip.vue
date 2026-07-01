<script setup lang="ts">
import { CheckCircle2, CircleGauge, ShieldAlert, Sparkles } from 'lucide-vue-next'
import type { DemoAnalysis } from '../types'

const props = defineProps<{ score: DemoAnalysis['score'] }>()

const icons = [CheckCircle2, Sparkles, ShieldAlert, CircleGauge]
</script>

<template>
  <section class="score-strip" aria-label="匹配依据">
    <div class="score-label">
      <span>匹配依据</span>
      <small>Evidence score</small>
    </div>
    <div v-for="(item, index) in props.score.items" :key="item.key" class="score-item" :class="item.tone">
      <component :is="icons[index]" :size="15" />
      <div>
        <span>{{ item.label }}</span>
        <small>{{ item.description }}</small>
      </div>
      <strong>{{ item.value > 0 ? item.value : item.value }}/{{ item.maximum }}</strong>
    </div>
    <div class="total-score">
      <strong>{{ props.score.total }}</strong><span>/{{ props.score.maximum }}</span>
      <small>综合匹配得分</small>
    </div>
  </section>
</template>
