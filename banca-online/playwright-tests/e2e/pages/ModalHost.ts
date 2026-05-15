import { Page, expect } from '@playwright/test';

export class ModalHost {
  private container;

  constructor(page: Page) {
    this.container = page.locator('#modalContainer');
  }

  async waitForContent(selector: string) {
    await expect(this.container.locator(selector)).toBeVisible();
  }

  locator(selector: string) {
    return this.container.locator(selector);
  }
}
