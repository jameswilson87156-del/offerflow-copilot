<script setup lang="ts">
import { Clock3, FileText, Sparkles } from 'lucide-vue-next'
import type { DemoAnalysis } from '../types'

const props = defineProps<{
  timeline: DemoAnalysis['timeline']
  resumes: DemoAnalysis['recommendedResumes']
}>()
</script>

<template>
  <section class="panel timeline-panel" aria-labelledby="timeline-title">
    <div class="timeline-area">
      <header class="timeline-heading">
        <div>
          <Clock3 :size="17" />
          <h2 id="timeline-title">投递流程时间线</h2>
        </div>
        <span>尚未投递 · 仅本地记录</span>
      </header>
      <div class="timeline-track">
        <div v-for="(step, index) in props.timeline" :key="step.key" class="timeline-step" :class="step.status">
          <div class="step-top">
            <span>{{ index + 1 }}</span>
            <i v-if="index < props.timeline.length - 1" />
          </div>
          <strong>{{ step.label }}</strong>
          <small>{{ step.detail }}</small>
        </div>
      </div>
    </div>

    <aside class="resume-area">
      <div class="resume-heading"><Sparkles :size="16" /><strong>推荐简历版本</strong></div>
      <article v-for="resume in props.resumes" :key="resume.name" class="resume-card">
        <span><FileText :size="19" /></span>
        <div><strong>{{ resume.name }}</strong><small>{{ resume.focus }}</small></div>
      </article>
    </aside>
  </section>
</template>
