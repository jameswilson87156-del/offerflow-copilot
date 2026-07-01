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

export type HumanReviewStatus = 'Draft' | 'Returned' | 'Confirmed' | 'Risk Flagged'

export interface HumanReviewGroup {
  key: string
  label: string
  count: number
}

export interface HumanReviewSummary {
  id: string
  group: string
  title: string
  sourcePage: string
  riskLevel: string
  providerMode: string
  traceId: string
  status: HumanReviewStatus
  updatedAt: string
}

export interface HumanReviewProject {
  name: string
  excerpt: string
  sourceTypes: string[]
}

export interface HumanReviewEvidence {
  jdSnippet: string
  resumeProjects: HumanReviewProject[]
  evidenceNote: string
}

export interface HumanReviewTraceStep {
  label: string
  status: 'done' | 'warning' | 'current'
  detail: string
}

export interface HumanReviewAuditEvent {
  id: string
  reviewId: string
  action: 'CONFIRM' | 'RETURN' | 'FLAG_RISK' | 'ADD_NOTE' | 'AUTO_RISK_GUARD' | string
  actionLabel: string
  previousStatus: string
  nextStatus: string
  previousRiskLevel: string
  nextRiskLevel: string
  actor: string
  actorRole: string
  humanNote: string
  traceId: string
  traceHash: string
  createdAt: string
}

export interface HumanReviewDetail extends HumanReviewSummary {
  reviewer: string
  humanNote: string
  aiSuggestion: string
  evidence: HumanReviewEvidence
  riskTerms: string[]
  traceEvidence: HumanReviewTraceStep[]
  compliancePrinciples: string[]
  copyAllowed: boolean
  lastAction: string
  auditTrail: HumanReviewAuditEvent[]
}

export interface HumanReviewCenterData {
  mode: string
  pendingReviewCount: number
  groups: HumanReviewGroup[]
  items: HumanReviewSummary[]
  compliancePrinciples: string[]
}

export type ProviderTraceStepStatus = 'success' | 'fallback' | 'warning'

export interface ProviderCard {
  id: string
  name: string
  status: string
  baseUrlStatus: string
  model: string
  timeout: string
  lastRun: string
  fallbackPolicy: string
  boundaryNotice: string
  realCallEnabled: boolean
  rawResponseSave: string
  apiKeyStatus: string
}

export interface SafetyBoundary {
  title: string
  description: string
  tone: string
}

export interface ProviderSettingsData {
  mode: string
  currentStatus: string
  providers: ProviderCard[]
  safetyBoundaries: SafetyBoundary[]
}

export interface ProviderTraceSummary {
  runId: string
  jobTitle: string
  providerMode: string
  finalProvider: string
  status: string
  startedAt: string
  duration: string
  evidenceCount: number
  humanReviewStatus: string
}

export interface ProviderTraceIndex {
  mode: string
  currentStatus: string
  items: ProviderTraceSummary[]
}

export interface ProviderPipelineStep {
  key: string
  label: string
  status: ProviderTraceStepStatus
  duration: string
  inputSummary: string
  outputSummary: string
  linkedEvidence: string
}

export interface ProviderResumeEvidenceRef {
  id: string
  title: string
  excerpt: string
  sources: string[]
}

export interface ProviderTraceEvidenceDetail {
  jdSnippet: string
  resumeEvidence: ProviderResumeEvidenceRef[]
  jsonSummary: string
  fallbackReason: string
  humanNote: string
}

export interface ProviderTraceRun {
  runId: string
  jobTitle: string
  providerMode: string
  finalProvider: string
  model: string
  fallbackReason: string
  promptVersion: string
  schemaVersion: string
  riskFlags: string[]
  evidenceCount: number
  humanReviewStatus: string
  duration: string
  traceHash: string
  pipeline: ProviderPipelineStep[]
  evidenceDetail: ProviderTraceEvidenceDetail
  technicalTags: string[]
}

export interface MatchReportSummary {
  jobTitle: string
  recommendedResumeVersions: string[]
  totalScore: number
  maximumScore: number
  status: string
  note: string
}

export interface MatchReportScoreItem {
  key: string
  label: string
  value: number
  maximum: number
  detail: string
  tone: string
}

export interface MatchReportEvidenceSource {
  requirement: string
  project: string
  strength: '强' | '中' | '弱'
  evidenceTypes: string[]
  rationale: string
}

export interface MatchReportSkillGap {
  skill: string
  severity: string
  reason: string
  nextAction: string
}

export interface MatchReportAction {
  title: string
  detail: string
  priority: string
}

export interface WorkflowTraceStep {
  label: string
  status: 'success' | 'warning' | 'current' | 'upcoming'
  detail: string
}

export interface MatchReportData {
  mode: string
  summary: MatchReportSummary
  score: {
    items: MatchReportScoreItem[]
    note: string
  }
  evidenceSources: MatchReportEvidenceSource[]
  skillGaps: MatchReportSkillGap[]
  recommendedActions: MatchReportAction[]
  traceEvidence: WorkflowTraceStep[]
  disclaimer: string
}

export interface InterviewFocusArea {
  label: string
  detail: string
  evidence: string
}

export interface InterviewQuestionGroup {
  key: string
  label: string
  questions: string[]
}

export interface InterviewStarDraft {
  situation: string
  task: string
  action: string
  result: string
  riskNote: string
}

export interface InterviewTimelineStep {
  key: string
  label: string
  status: 'current' | 'upcoming' | 'done'
  detail: string
}

export interface InterviewPrepData {
  mode: string
  jobTitle: string
  positioningNotice: string
  focusAreas: InterviewFocusArea[]
  questionGroups: InterviewQuestionGroup[]
  starDraft: InterviewStarDraft
  riskReminders: string[]
  reviewTimeline: InterviewTimelineStep[]
  disclaimer: string
}

export interface ApplicationBoardColumn {
  key: string
  label: string
  count: number
  tone: string
}

export interface ApplicationRecord {
  id: string
  company: string
  role: string
  city: string
  resumeVersion: string
  sourceNote: string
  status: string
  updatedAt: string
  nextAction: string
}

export interface CommunicationLog {
  applicationId: string
  stage: string
  note: string
  timestamp: string
}

export interface ApplicationTrackerData {
  mode: string
  boardColumns: ApplicationBoardColumn[]
  applications: ApplicationRecord[]
  communicationLogs: CommunicationLog[]
  riskBoundaries: string[]
  disclaimer: string
}
