import { Page, expect } from '@playwright/test';

export class LoginPage {
  constructor(private page: Page) {}

  async goto() {
    await this.page.goto('/');
  }

  async fillCredentials(email: string, password: string) {
    await this.page.fill('#loginEmail', email);
    await this.page.fill('#loginPassword', password);
  }

  async submit() {
    await this.page.click('button[type="submit"]');
  }

  async assertErrorVisible(text?: string) {
    const error = this.page.locator('#loginError');
    await expect(error).toBeVisible();
    if (text) await expect(error).toContainText(text);
  }
}
