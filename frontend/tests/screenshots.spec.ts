import { expect, test, type Page } from '@playwright/test'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const currentDir = path.dirname(fileURLToPath(import.meta.url))
const docsDir = path.resolve(currentDir, '../../docs/images')

async function expectNoHorizontalOverflow(page: Page) {
  const dimensions = await page.evaluate(() => ({
    viewport: document.documentElement.clientWidth,
    page: document.documentElement.scrollWidth,
  }))
  expect(dimensions.page).toBeLessThanOrEqual(dimensions.viewport)
}

async function verifyJdWorkbench(page: Page) {
  const consoleErrors: string[] = []
  page.on('console', (message) => {
    if (message.type() === 'error') consoleErrors.push(message.text())
  })

  await page.goto('/jd-analyzer')
  await expect(page.getByRole('heading', { name: 'JD 证据匹配工作台' })).toBeVisible()
  await expect(page.getByRole('heading', { name: '简历证据匹配画布' })).toBeVisible()
  await expect(page.getByText('MCP Tool Gateway', { exact: true })).toBeVisible()
  await expect(page.getByText('82', { exact: true })).toBeVisible()
  await expect(page.getByText('本地 API', { exact: true })).toBeVisible()
  await expectNoHorizontalOverflow(page)
  expect(consoleErrors).toEqual([])
}

async function verifyEvidenceLibrary(page: Page) {
  const consoleErrors: string[] = []
  page.on('console', (message) => {
    if (message.type() === 'error') consoleErrors.push(message.text())
  })

  await page.goto('/evidence-library')
  await expect(page.getByRole('heading', { name: '简历证据库' })).toBeVisible()
  await expect(page.getByRole('heading', { name: '项目证据' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Evidence Detail' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Evidence Coverage Map' })).toBeVisible()
  await expect(page.getByText('Evidence Ready', { exact: true })).toBeVisible()
  await expect(page.locator('button.library-card')).toHaveCount(4)
  await expectNoHorizontalOverflow(page)
  expect(consoleErrors).toEqual([])
}

async function verifyHumanReview(page: Page) {
  const consoleErrors: string[] = []
  page.on('console', (message) => {
    if (message.type() === 'error') consoleErrors.push(message.text())
  })

  await page.goto('/human-review')
  await expect(page.getByRole('heading', { name: '人工复核中心' })).toBeVisible()
  await expect(page.getByRole('heading', { name: '审核队列' })).toBeVisible()
  await expect(page.getByRole('heading', { name: '人工复核操作' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Trace Evidence' })).toBeVisible()
  await expect(page.getByRole('banner').getByText('12 个待复核', { exact: true })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'STAR 回答草稿：MCP Tool Gateway 项目深挖' })).toBeVisible()
  await expect(page.locator('.risk-highlight')).toHaveCount(7)
  await expectNoHorizontalOverflow(page)
  expect(consoleErrors).toEqual([])
}

test('captures 1440 JD workbench', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 })
  await verifyJdWorkbench(page)
  await page.screenshot({ path: path.join(docsDir, 'offerflow-dashboard.png'), fullPage: true })
})

test('captures 1920 JD workbench', async ({ page }) => {
  await page.setViewportSize({ width: 1920, height: 1080 })
  await verifyJdWorkbench(page)
  await page.screenshot({ path: path.join(docsDir, 'large/offerflow-dashboard.png'), fullPage: true })
})

test('captures 1440 evidence library', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 })
  await verifyEvidenceLibrary(page)
  await page.screenshot({ path: path.join(docsDir, 'offerflow-evidence-library.png'), fullPage: true })
})

test('captures 1920 evidence library', async ({ page }) => {
  await page.setViewportSize({ width: 1920, height: 1080 })
  await verifyEvidenceLibrary(page)
  await page.screenshot({ path: path.join(docsDir, 'large/offerflow-evidence-library.png'), fullPage: true })
})

test('captures 1440 human review center', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 })
  await verifyHumanReview(page)
  await page.screenshot({ path: path.join(docsDir, 'offerflow-human-review.png'), fullPage: true })
})

test('captures 1920 human review center', async ({ page }) => {
  await page.setViewportSize({ width: 1920, height: 1080 })
  await verifyHumanReview(page)
  await page.screenshot({ path: path.join(docsDir, 'large/offerflow-human-review.png'), fullPage: true })
})

test('routes avoid horizontal overflow at 1366 by 768', async ({ page }) => {
  await page.setViewportSize({ width: 1366, height: 768 })
  await verifyJdWorkbench(page)
  await verifyEvidenceLibrary(page)
  await verifyHumanReview(page)
})
