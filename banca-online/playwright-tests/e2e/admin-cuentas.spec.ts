import { test, expect } from './fixtures/datos.fixture';
import { ModalHost } from './pages/ModalHost';

test.describe('E2E parte admin gestión de cuentas', () => {
  test('admin crea una cuenta para un cliente', async ({ page, adminShell, clientePrevio }) => {
    await adminShell.clickMenuItem('🏦 Gestionar Cuentas');
    const modal = new ModalHost(page);
    await modal.waitForContent('#selectClienteFiltro');

    // Esperar a que el dropdown cargue las opciones
    await expect(
      page.locator(`#selectClienteFiltro option[value="${clientePrevio.id}"]`),
    ).toBeAttached();
    await page.selectOption('#selectClienteFiltro', String(clientePrevio.id));

    await page.click('#btnCrearCuenta');
    await expect(page.locator('#modal-crearCuentaForm')).toBeVisible();
    // Esperar a que el formulario rellene el clienteId
    await expect(page.locator('#clienteId')).not.toHaveValue('');

    await page.selectOption('#tipoCuenta', 'CORRIENTE');
    await page.fill('#saldoInicial', '500');

    await page.click('#formCrearCuenta button[type="submit"]');
    await expect(page.locator('#mensajeExitoCuenta')).toBeVisible();
    await expect(page.locator('#mensajeExitoCuenta')).toContainText('Cuenta creada con éxito');
  });

  test('admin filtra cuentas por cliente y ve la cuenta existente', async ({
    page,
    adminShell,
    clientePrevio,
    cuentaPrevia,
  }) => {
    await adminShell.clickMenuItem('🏦 Gestionar Cuentas');
    const modal = new ModalHost(page);
    await modal.waitForContent('#selectClienteFiltro');

    await expect(
      page.locator(`#selectClienteFiltro option[value="${clientePrevio.id}"]`),
    ).toBeAttached();
    await page.selectOption('#selectClienteFiltro', String(clientePrevio.id));

    await expect(page.locator('.tabla-cuentas')).toBeVisible();
    await expect(page.locator('.tabla-cuentas')).toContainText(cuentaPrevia.numeroCuenta as string);
  });
});
