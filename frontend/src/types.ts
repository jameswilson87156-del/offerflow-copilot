export interface ProviderStatus {
  mode: string
  openaiCompatibleReady: boolean
  deepSeekReady: boolean
  realCallEnabled: boolean
  fallback: string
}

export interface Requirement {
  id: string
  title: string
  description: string
  level: string
  keywords: string[]
}

export interface RequirementGroup {
  key: 'core' | 'bonus' | 'risk'
  label: string
  tone: string
  items: Requirement[]
}

export interface EvidenceMatch {
  requirement: string
  requirementDetail: string
  project: string
  projectSlug: string
  strength: '强' | '中' | '弱'
  rationale: string
  evidenceTypes: string[]
}

export interface ScoreItem {
  key: string
  label: string
  value: number
  maximum: number
  description: string
  tone: string
}

export interface DemoAnalysis {
  job: { title: string; company: string; source: string; updatedAt: string }
  requirementGroups: RequirementGroup[]
  evidenceMatches: EvidenceMatch[]
  score: { total: number; maximum: number; items: ScoreItem[]; note: string }
  interviewPreparation: {
    followUpQuestions: string[]
    starDraft: { situation: string; task: string; action: string; result: string }
    riskReminders: string[]
  }
  humanReview: {
    aiOutputStatus: string
    humanStatus: string
    copyAllowed: boolean
    instruction: string
  }
  timeline: Array<{ key: string; label: string; status: string; detail: string; timestamp: string }>
  recommendedResumes: Array<{ name: string; focus: string; recommended: boolean }>
  technicalTags: string[]
  disclaimer: string
}

export interface EvidenceCategory {
  key: string
  label: string
  count: number
}

export interface EvidenceSourceStep {
  label: string
  status: 'verified' | 'partial' | 'missing'
  note: string
}

export interface EvidenceDetail {
  relatedSkills: string[]
  suitableRoles: string[]
  interviewAnswers: string[]
  riskBoundaries: string[]
  sourceChain: EvidenceSourceStep[]
}

export interface EvidenceItem {
  id: string
  projectName: string
  projectSlug: string
  summary: string
  abilityTags: string[]
  evidenceSources: string[]
  credibility: '强' | '中' | '弱'
  matchableRequirements: string[]
  humanReviewStatus: 'Confirmed' | 'Needs review'
  updatedAt: string
  detail: EvidenceDetail
}

export interface EvidenceLibraryData {
  mode: string
  total: number
  categories: EvidenceCategory[]
  items: EvidenceItem[]
  disclaimer: string
}

export interface CoverageItem {
  skill: string
  level: '强支撑' | '中支撑' | '弱支撑' | '暂无证据'
  score: number
  supportingProjects: string[]
  gap: string
}

export interface EvidenceCoverageData {
  mode: string
  items: CoverageItem[]
  note: string
}
