import { Page, expect } from '@playwright/test';

export class MainShellPage {
  constructor(private page: Page) {}

  async waitForMainApp() {
    await expect(this.page.locator('#mainApp')).toBeVisible();
  }

  async userRole(): Promise<string | null> {
    return this.page.locator('#userRole').textContent();
  }

  async logout() {
    await this.page.click('#logoutBtn');
  }

  async clickMenuItem(text: string) {
    await this.page.locator('#menuDinamico').getByRole('button', { name: text }).click();
  }
}
