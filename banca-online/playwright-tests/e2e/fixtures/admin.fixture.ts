import { test as base } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { MainShellPage } from '../pages/MainShellPage';
import { ADMIN_EMAIL, ADMIN_PASSWORD } from '../../support/env';

type AdminFixtures = {
  adminShell: MainShellPage;
};

export const test = base.extend<AdminFixtures>({
  adminShell: async ({ page }, use) => {
    const login = new LoginPage(page);
    await login.goto();
    await login.fillCredentials(ADMIN_EMAIL, ADMIN_PASSWORD);
    await login.submit();
    const shell = new MainShellPage(page);
    await shell.waitForMainApp();
    await use(shell);
  },
});

export { expect } from '@playwright/test';
