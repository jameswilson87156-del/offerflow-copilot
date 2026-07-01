import type { EvidenceCoverageData, EvidenceDetail, EvidenceItem, EvidenceLibraryData } from '../types'

const sourceChain = (
  test: 'verified' | 'partial' | 'missing',
  actions: 'verified' | 'partial' | 'missing',
  review: 'verified' | 'partial',
) => [
  { label: 'README', status: 'verified' as const, note: '结构与边界说明' },
  { label: '真实截图', status: 'verified' as const, note: '本地页面证据' },
  { label: '后端测试', status: test, note: test === 'missing' ? '当前不适用或待补充' : '关键契约检查' },
  { label: 'GitHub Actions', status: actions, note: actions === 'verified' ? '自动构建记录' : '待补完整记录' },
  { label: '人工确认', status: review, note: review === 'verified' ? '事实与来源已复核' : '部分表述待复核' },
]

const detail = (
  relatedSkills: string[],
  suitableRoles: string[],
  interviewAnswers: string[],
  riskBoundaries: string[],
  chain: EvidenceDetail['sourceChain'],
): EvidenceDetail => ({ relatedSkills, suitableRoles, interviewAnswers, riskBoundaries, sourceChain: chain })

const items: EvidenceItem[] = [
  {
    id: 'evidence-mcp', projectName: 'MCP Tool Gateway', projectSlug: 'mcp-tool-gateway',
    summary: 'Spring Boot 工具网关，展示统一注册、调用审计与 Trace 证据。',
    abilityTags: ['Java 后端', 'AI Agent 工具', 'Trace / Human Review'],
    evidenceSources: ['README', '接口设计', '后端测试', 'Trace'], credibility: '强',
    matchableRequirements: ['Spring Boot', 'MCP Tool', '接口设计', '审计与异常处理'],
    humanReviewStatus: 'Confirmed', updatedAt: '2026-07-01',
    detail: detail(
      ['Java', 'Spring Boot 3', 'JSON-RPC', 'Tool Registry', 'Audit Log', 'Trace Evidence'],
      ['Java 后端实习生', 'AI Agent 工程实习生', '后端开发工程师'],
      ['如何设计统一 Tool Gateway？', '如何处理 Tool 注册、权限与调用审计？', '如何实现异常处理与 Trace 追踪？'],
      ['不声明已通过真实生产流量验证', '不将演示审计等同生产级安全', '性能数字需有独立压测证据'],
      sourceChain('verified', 'partial', 'verified'),
    ),
  },
  {
    id: 'evidence-devflow', projectName: 'DevFlow Copilot', projectSlug: 'devflow-copilot',
    summary: '从任务拆解到工具执行的 AI Coding Workflow 演示。',
    abilityTags: ['AI Coding', 'AI 应用开发', 'Trace / Human Review'],
    evidenceSources: ['README', '截图', '接口设计', 'GitHub Actions'], credibility: '强',
    matchableRequirements: ['AI Workflow', 'Prompt Workflow', '工程交付', '协作开发'],
    humanReviewStatus: 'Confirmed', updatedAt: '2026-06-30',
    detail: detail(
      ['AI Workflow', 'Prompt', 'Tool Calling', 'Vue 3', 'Human Review'],
      ['AI Coding 实习生', 'AI 应用开发实习生', '全栈开发实习生'],
      ['如何拆解 AI Coding 任务？', '如何防止工具执行失控？', 'Human Review 放在哪个节点？'],
      ['没有真实 Provider 稳定性结论', '任务结果仍需人工复核', '不把 demo 描述为生产平台'],
      sourceChain('partial', 'verified', 'verified'),
    ),
  },
  {
    id: 'evidence-rag', projectName: 'Enterprise Ticket RAG Copilot', projectSlug: 'enterprise-ticket-rag',
    summary: '带引用、Trace 与 Human Review 的企业工单 RAG 演示。',
    abilityTags: ['RAG / Knowledge', 'AI 应用开发', 'Trace / Human Review'],
    evidenceSources: ['README', 'Trace', '效果评估', '截图'], credibility: '中',
    matchableRequirements: ['RAG', '知识检索', 'Provider fallback', '引用追踪'],
    humanReviewStatus: 'Needs review', updatedAt: '2026-06-29',
    detail: detail(
      ['RAG', 'Knowledge Retrieval', 'Citation', 'Trace', 'Provider fallback'],
      ['AI 应用开发实习生', 'RAG 工程实习生', 'Java 后端实习生'],
      ['如何评估检索质量？', '引用如何与答案绑定？', 'Provider 失败时如何回退？'],
      ['没有真实客户数据', '没有大规模线上效果数据', '评测样本是匿名化演示集'],
      sourceChain('partial', 'missing', 'partial'),
    ),
  },
  {
    id: 'evidence-portfolio', projectName: 'Portfolio Hub', projectSlug: 'portfolio-hub',
    summary: '作品集聚合、真实页面截图与持续构建证据。',
    abilityTags: ['前端工程', 'CI / 部署'],
    evidenceSources: ['真实截图', 'GitHub Actions', '部署记录', 'README'], credibility: '中',
    matchableRequirements: ['Vue 3', '前端工程', 'CI', 'Deployment'],
    humanReviewStatus: 'Confirmed', updatedAt: '2026-06-28',
    detail: detail(
      ['Vue 3', 'TypeScript', 'Vite', 'Playwright', 'GitHub Actions'],
      ['前端开发实习生', 'AI Coding 实习生', '全栈开发实习生'],
      ['如何保证作品集截图可信？', 'CI 如何验证构建？', '响应式页面如何验收？'],
      ['演示部署不代表生产 SLA', '截图只证明特定版本页面', '无真实用户或流量数据'],
      sourceChain('missing', 'verified', 'verified'),
    ),
  },
]

export const evidenceLibraryFallback: EvidenceLibraryData = {
  mode: 'mock/local-rule',
  total: 4,
  categories: [
    { key: 'all', label: '全部证据', count: 4 },
    { key: 'java', label: 'Java 后端', count: 2 },
    { key: 'ai-app', label: 'AI 应用开发', count: 3 },
    { key: 'agent', label: 'AI Agent 工具', count: 2 },
    { key: 'rag', label: 'RAG / Knowledge', count: 1 },
    { key: 'ai-coding', label: 'AI Coding', count: 1 },
    { key: 'frontend', label: '前端工程', count: 2 },
    { key: 'ci', label: 'CI / 部署', count: 2 },
    { key: 'trace', label: 'Trace / Human Review', count: 3 },
  ],
  items,
  disclaimer: '证据库仅使用匿名化演示项目数据，不代表真实客户、用户、线上流量或生产级效果。',
}

export const evidenceCoverageFallback: EvidenceCoverageData = {
  mode: 'mock/local-rule',
  items: [
    { skill: 'Java', level: '强支撑', score: 92, supportingProjects: ['MCP Tool Gateway', 'Enterprise Ticket RAG Copilot'], gap: '补充复杂并发案例' },
    { skill: 'Spring Boot', level: '强支撑', score: 94, supportingProjects: ['MCP Tool Gateway', 'Enterprise Ticket RAG Copilot'], gap: '补充事务压测记录' },
    { skill: 'Vue 3', level: '中支撑', score: 68, supportingProjects: ['Portfolio Hub', 'DevFlow Copilot'], gap: '缺少组件测试证据' },
    { skill: 'AI Workflow', level: '强支撑', score: 88, supportingProjects: ['DevFlow Copilot', 'MCP Tool Gateway'], gap: '需人工核验编排边界' },
    { skill: 'RAG', level: '中支撑', score: 72, supportingProjects: ['Enterprise Ticket RAG Copilot'], gap: '缺少大规模离线评测' },
    { skill: 'Trace', level: '强支撑', score: 86, supportingProjects: ['MCP Tool Gateway', 'DevFlow Copilot', 'Enterprise Ticket RAG Copilot'], gap: '补充失败链路样例' },
    { skill: 'CI', level: '中支撑', score: 74, supportingProjects: ['Portfolio Hub', 'DevFlow Copilot'], gap: '缺少发布审批记录' },
    { skill: 'Deployment', level: '弱支撑', score: 46, supportingProjects: ['Portfolio Hub'], gap: '仅有演示部署，不代表生产运维' },
  ],
  note: '覆盖度由确定性 local-rule 根据证据类型与人工确认状态计算，不是岗位录取概率。保持空缺比虚构证据更重要。',
}
