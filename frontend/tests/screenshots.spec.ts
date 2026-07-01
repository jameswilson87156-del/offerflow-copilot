import { expect, test, type Page } from '@playwright/test'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const currentDir = path.dirname(fileURLToPath(import.meta.url))
const docsDir = path.resolve(currentDir, '../../docs/images')

async function verifyWorkbench(page: Page) {
  const consoleErrors: string[] = []
  page.on('console', (message) => {
    if (message.type() === 'error') consoleErrors.push(message.text())
  })

  await page.goto('/')
  await expect(page.getByRole('heading', { name: 'JD 证据匹配工作台' })).toBeVisible()
  await expect(page.getByRole('heading', { name: '简历证据匹配画布' })).toBeVisible()
  await expect(page.getByText('MCP Tool Gateway', { exact: true })).toBeVisible()
  await expect(page.getByText('82', { exact: true })).toBeVisible()
  await expect(page.getByText('本地 API', { exact: true })).toBeVisible()

  const dimensions = await page.evaluate(() => ({
    viewport: document.documentElement.clientWidth,
    page: document.documentElement.scrollWidth,
  }))
  expect(dimensions.page).toBeLessThanOrEqual(dimensions.viewport)
  expect(consoleErrors).toEqual([])
}

test('captures 1440 desktop workbench', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 })
  await verifyWorkbench(page)
  await page.screenshot({ path: path.join(docsDir, 'offerflow-dashboard.png'), fullPage: true })
})

test('captures 1920 large desktop workbench', async ({ page }) => {
  await page.setViewportSize({ width: 1920, height: 1080 })
  await verifyWorkbench(page)
  await page.screenshot({ path: path.join(docsDir, 'large/offerflow-dashboard.png'), fullPage: true })
})

test('has no horizontal overflow at 1366 by 768', async ({ page }) => {
  await page.setViewportSize({ width: 1366, height: 768 })
  await verifyWorkbench(page)
})
