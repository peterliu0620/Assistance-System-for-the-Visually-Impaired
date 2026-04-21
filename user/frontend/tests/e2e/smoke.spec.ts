import { expect, test } from '@playwright/test'

test('renders the real uni-app home page', async ({ page }) => {
  await page.goto('/')

  await expect(page.getByText('智能视觉辅助')).toBeVisible()
  await expect(page.getByText('把识别、寻物和安全提醒放进一个更清晰的入口')).toBeVisible()
})
