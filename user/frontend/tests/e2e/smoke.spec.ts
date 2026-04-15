import { expect, test } from '@playwright/test'

test('renders the minimal H5 base and handles a click', async ({ page }) => {
  await page.goto('/')

  await expect(page.getByTestId('app-title')).toHaveText('user/frontend 最小测试基座')
  await expect(page.getByTestId('runtime-label')).toHaveText('uni-app H5')
  await expect(page.getByTestId('counter-value')).toHaveText('0')

  await page.getByTestId('increment-button').click()

  await expect(page.getByTestId('counter-value')).toHaveText('1')
})
