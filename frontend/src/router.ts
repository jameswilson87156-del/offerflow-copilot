import { createRouter, createWebHistory } from 'vue-router'
import EvidenceLibraryView from './views/EvidenceLibraryView.vue'
import JdAnalyzerView from './views/JdAnalyzerView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/jd-analyzer' },
    { path: '/jd-analyzer', name: 'jd-analyzer', component: JdAnalyzerView },
    { path: '/evidence-library', name: 'evidence-library', component: EvidenceLibraryView },
  ],
})

router.afterEach((to) => {
  document.title = to.name === 'evidence-library'
    ? 'OfferFlow Copilot · 简历证据库'
    : 'OfferFlow Copilot · JD 证据匹配工作台'
})
