import { test, expect } from './fixtures/admin.fixture';
import { LoginPage } from './pages/LoginPage';

test.describe('E2E — login', () => {
  test('admin login muestra el panel principal con rol Administrador', async ({ page, adminShell }) => {
    await expect(page.locator('#mainApp')).toBeVisible();
    const role = await adminShell.userRole();
    expect(role).toContain('Administrador');
  });

  test('credenciales inválidas muestran mensaje de error', async ({ page }) => {
    const login = new LoginPage(page);
    await login.goto();
    await login.fillCredentials('noexiste@test.com', 'wrongpass');
    await login.submit();
    await login.assertErrorVisible('Credenciales inválidas');
  });
});
