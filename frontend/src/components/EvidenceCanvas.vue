<script setup lang="ts">
import { ArrowRight, Boxes, GitCommitHorizontal, Info, Layers3 } from 'lucide-vue-next'
import type { DemoAnalysis, EvidenceMatch } from '../types'
import ScoreStrip from './ScoreStrip.vue'

const props = defineProps<{
  matches: EvidenceMatch[]
  score: DemoAnalysis['score']
}>()
</script>

<template>
  <section class="panel evidence-canvas" aria-labelledby="evidence-title">
    <header class="panel-header canvas-header">
      <div>
        <span class="panel-kicker">EVIDENCE MAP</span>
        <h2 id="evidence-title">简历证据匹配画布</h2>
      </div>
      <div class="strength-legend" aria-label="匹配强度图例">
        <span><i class="strong" />强匹配</span>
        <span><i class="medium" />中匹配</span>
        <span><i class="weak" />弱匹配</span>
      </div>
    </header>

    <div class="canvas-column-labels" aria-hidden="true">
      <span>JD 能力要求</span><span>关联</span><span>项目证明与可验证证据</span>
    </div>

    <div v-if="props.matches.length" class="evidence-rows">
      <article v-for="(match, index) in props.matches" :key="match.projectSlug" class="evidence-row">
        <div class="requirement-node">
          <span class="node-icon" :class="`tone-${index}`">
            <Layers3 v-if="index === 0" :size="18" />
            <Boxes v-else-if="index === 1" :size="18" />
            <GitCommitHorizontal v-else :size="18" />
          </span>
          <div>
            <strong>{{ match.requirement }}</strong>
            <small>{{ match.requirementDetail }}</small>
          </div>
        </div>

        <div class="evidence-connector" :class="match.strength">
          <span />
          <ArrowRight :size="15" />
        </div>

        <div class="project-node">
          <div class="project-heading">
            <div>
              <span class="project-index">0{{ index + 1 }}</span>
              <strong>{{ match.project }}</strong>
            </div>
            <span class="strength-badge" :class="match.strength">{{ match.strength }}</span>
          </div>
          <p>{{ match.rationale }}</p>
          <div class="evidence-tags">
            <span v-for="type in match.evidenceTypes" :key="type">{{ type }}</span>
          </div>
        </div>
      </article>
    </div>
    <div v-else class="empty-state evidence-empty">没有匹配的项目证据，请调整搜索词</div>

    <div class="score-wrap">
      <ScoreStrip :score="props.score" />
      <p class="score-note"><Info :size="13" />{{ props.score.note }}</p>
    </div>
  </section>
</template>
