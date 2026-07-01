<script setup lang="ts">
import { AlertTriangle, BadgeCheck, BriefcaseBusiness, Check, CircleDot, Link2, MessageSquareText, ShieldCheck } from 'lucide-vue-next'
import type { EvidenceItem } from '../../types'

const props = defineProps<{ item: EvidenceItem }>()
</script>

<template>
  <aside class="panel evidence-detail" aria-labelledby="detail-title">
    <header class="panel-header evidence-panel-header">
      <div>
        <span class="panel-kicker">EVIDENCE DETAIL</span>
        <h2 id="detail-title">Evidence Detail</h2>
      </div>
      <span class="detail-status" :class="props.item.humanReviewStatus === 'Confirmed' ? 'confirmed' : 'review'">
        {{ props.item.humanReviewStatus === 'Confirmed' ? '已确认' : '待复核' }}
      </span>
    </header>

    <div class="detail-project">
      <span><ShieldCheck :size="20" /></span>
      <div><strong>{{ props.item.projectName }}</strong><small>{{ props.item.summary }}</small></div>
    </div>

    <section class="detail-block skill-block">
      <h3><CircleDot :size="14" />关联技能</h3>
      <div class="detail-chips">
        <span v-for="skill in props.item.detail.relatedSkills" :key="skill">{{ skill }}</span>
      </div>
    </section>

    <section class="detail-block">
      <h3><BriefcaseBusiness :size="14" />可用于哪些岗位</h3>
      <ul class="role-list">
        <li v-for="role in props.item.detail.suitableRoles" :key="role"><Check :size="12" />{{ role }}</li>
      </ul>
    </section>

    <section class="detail-block">
      <h3><MessageSquareText :size="14" />可支撑哪些面试回答</h3>
      <ol class="answer-list">
        <li v-for="answer in props.item.detail.interviewAnswers" :key="answer">{{ answer }}</li>
      </ol>
    </section>

    <section class="detail-block detail-risk">
      <h3><AlertTriangle :size="14" />风险边界</h3>
      <ul>
        <li v-for="risk in props.item.detail.riskBoundaries" :key="risk">{{ risk }}</li>
      </ul>
    </section>

    <section class="source-chain">
      <h3><Link2 :size="14" />证据来源链路</h3>
      <div class="source-steps">
        <div v-for="(step, index) in props.item.detail.sourceChain" :key="step.label" class="source-step" :class="step.status">
          <span class="step-dot"><BadgeCheck v-if="step.status === 'verified'" :size="12" /><span v-else /></span>
          <div><strong>{{ step.label }}</strong><small>{{ step.note }}</small></div>
          <i v-if="index < props.item.detail.sourceChain.length - 1" />
        </div>
      </div>
    </section>
  </aside>
</template>
