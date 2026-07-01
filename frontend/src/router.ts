import { createRouter, createWebHistory } from 'vue-router'
import EvidenceLibraryView from './views/EvidenceLibraryView.vue'
import HumanReviewView from './views/HumanReviewView.vue'
import JdAnalyzerView from './views/JdAnalyzerView.vue'
import ProviderSettingsView from './views/ProviderSettingsView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/jd-analyzer' },
    { path: '/jd-analyzer', name: 'jd-analyzer', component: JdAnalyzerView },
    { path: '/evidence-library', name: 'evidence-library', component: EvidenceLibraryView },
    { path: '/human-review', name: 'human-review', component: HumanReviewView },
    { path: '/provider-settings', name: 'provider-settings', component: ProviderSettingsView },
  ],
})

router.afterEach((to) => {
  const titles: Record<string, string> = {
    'jd-analyzer': 'OfferFlow Copilot · JD 证据匹配工作台',
    'evidence-library': 'OfferFlow Copilot · 简历证据库',
    'human-review': 'OfferFlow Copilot · 人工复核中心',
    'provider-settings': 'OfferFlow Copilot · Provider 设置与证据链',
  }
  document.title = titles[String(to.name)] ?? 'OfferFlow Copilot'
})
