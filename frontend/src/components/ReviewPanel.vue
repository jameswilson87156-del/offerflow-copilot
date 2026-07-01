<script setup lang="ts">
import { AlertTriangle, ArrowRight, BadgeCheck, HelpCircle, LockKeyhole, MessageSquareQuote } from 'lucide-vue-next'
import type { DemoAnalysis } from '../types'

const props = defineProps<{
  preparation: DemoAnalysis['interviewPreparation']
  review: DemoAnalysis['humanReview']
}>()
</script>

<template>
  <section class="panel review-panel" aria-labelledby="review-title">
    <header class="panel-header">
      <div>
        <span class="panel-kicker">REVIEW GATE</span>
        <h2 id="review-title">面试准备与人工复核</h2>
      </div>
      <LockKeyhole :size="17" class="header-icon" />
    </header>

    <section class="review-block questions-block">
      <h3><HelpCircle :size="16" />可能追问 <span>{{ props.preparation.followUpQuestions.length }}</span></h3>
      <ol>
        <li v-for="question in props.preparation.followUpQuestions" :key="question">{{ question }}</li>
      </ol>
    </section>

    <section class="review-block star-block">
      <h3><MessageSquareQuote :size="16" />STAR 回答草稿</h3>
      <p><strong>S</strong>{{ props.preparation.starDraft.situation }}</p>
      <p><strong>A</strong>{{ props.preparation.starDraft.action }}</p>
      <button type="button">查看完整草稿 <ArrowRight :size="13" /></button>
    </section>

    <section class="review-block risk-block">
      <h3><AlertTriangle :size="16" />风险提醒</h3>
      <ul>
        <li v-for="risk in props.preparation.riskReminders.slice(0, 2)" :key="risk">{{ risk }}</li>
      </ul>
    </section>

    <section class="review-gate">
      <div class="review-flow">
        <div class="state-card draft">
          <small>规则输出</small>
          <strong>{{ props.review.aiOutputStatus }}</strong>
        </div>
        <ArrowRight :size="16" />
        <div class="state-card pending">
          <small>人工确认</small>
          <strong>待核验</strong>
        </div>
        <ArrowRight :size="16" />
        <div class="state-card confirmed">
          <BadgeCheck :size="16" />
          <strong>Confirmed</strong>
        </div>
      </div>
      <p><LockKeyhole :size="13" />{{ props.review.instruction }}</p>
      <button type="button" class="review-button">进入人工复核</button>
    </section>
  </section>
</template>
