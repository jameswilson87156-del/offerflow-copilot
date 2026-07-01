# 设计说明

## 视觉方向

OfferFlow Copilot 使用“冷静的浅色证据审阅工作台”方向：纸白背景、墨蓝文字、电蓝关系线、证据绿确认状态与风险橙提醒。所有真实页面都由 Vue + DOM/CSS 实现，没有把设计参考图用作背景。

P1 的视觉中心是“JD 要求 → 项目证明”的证据匹配画布；P2A 则是“可验证资产档案库”，以项目证据卡、Evidence Detail 来源链与 Coverage Map 展示证据本身的可信度、复用范围和缺口。

## AI-generated visual references

`docs/design/references` 只存放 AI-generated visual references，用于讨论信息层级、视觉方向和页面密度。

- 这些图片不能当作真实运行截图，也不能作为页面背景或产品能力证明。
- `offerflow-jd-evidence-workbench-reference.png` 是 JD 工作台设计参考。
- `offerflow-resume-evidence-library-reference.png` 是简历证据库设计参考。
- `offerflow-human-review-reference.png` 是 Human Review 后续页面设计参考。
- 真实运行截图只能放在 `docs/images` 和 `docs/images/large`。
- README 只能引用由本地应用真实渲染并经过检查的运行截图。

## 响应式策略

- 1920px：完整侧栏、宽证据画布或证据卡网格、右侧详情。
- 1440px：压缩间距与辅助文案，不降低信息层级。
- 1366×768：保留三栏核心工作流，允许纵向滚动，不产生页面横向溢出。
- 窄屏：侧栏收为图标轨道，核心区域改为纵向流。

## 真实运行截图

- `docs/images/offerflow-dashboard.png`：1440×900 的 JD 工作台。
- `docs/images/large/offerflow-dashboard.png`：1920×1080 的 JD 工作台。
- `docs/images/offerflow-evidence-library.png`：1440×900 的简历证据库。
- `docs/images/large/offerflow-evidence-library.png`：1920×1080 的简历证据库。

## 交互与状态

- Vue Router 提供 `/jd-analyzer` 与 `/evidence-library` 两个真实路径。
- 全局搜索会过滤当前页面的岗位要求、项目证据或技能来源。
- 证据库能力分类、项目卡与右侧 Evidence Detail 联动。
- Provider 可切换展示，但只有 `local-rule` 是当前可用演示模式。
- Human Review 明确区分 `Draft`、`Needs review` 与 `Confirmed`。
- 支持键盘焦点与 `prefers-reduced-motion`。
