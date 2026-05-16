import { test, expect } from './fixtures/admin.fixture';
import { ModalHost } from './pages/ModalHost';

test.describe('E2E parte navegación admin', () => {
  test('logout regresa a la pantalla de login', async ({ page, adminShell }) => {
    await adminShell.logout();
    await expect(page.locator('#loginScreen')).toBeVisible();
  });

  test('admin abre modal listar clientes', async ({ page, adminShell }) => {
    await adminShell.clickMenuItem('📋 Gestionar Clientes');
    const modal = new ModalHost(page);
    await modal.waitForContent('#listaClientes');
  });

  test('admin abre modal listar cuentas', async ({ page, adminShell }) => {
    await adminShell.clickMenuItem('🏦 Gestionar Cuentas');
    const modal = new ModalHost(page);
    await modal.waitForContent('#selectClienteFiltro');
  });
});
