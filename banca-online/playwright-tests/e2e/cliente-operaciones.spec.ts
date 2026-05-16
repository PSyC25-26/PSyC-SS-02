import { test, expect } from './fixtures/sesionCliente.fixture';
import { LoginPage } from './pages/LoginPage';

test.describe('E2E parte operaciones de cliente', () => {
  test.beforeEach(async ({ page, clienteInfo, cuentaInfo: _cuentaInfo }) => {
    const login = new LoginPage(page);
    await login.goto();
    await login.fillCredentials(clienteInfo.email as string, clienteInfo._password as string);
    await login.submit();
    await expect(page.locator('#mainApp')).toBeVisible();
    // Esperar a que carguen las cuentas del cliente
    await expect(page.locator('#listaCuentasCliente')).toBeVisible();
    await expect(page.locator('.cuenta-item')).toBeVisible();
  });

  test('cliente ve sus cuentas al iniciar sesión', async ({ page }) => {
    await expect(page.locator('.cuenta-item')).toBeVisible();
    await expect(page.locator('.cuenta-numero-lista')).toBeVisible();
  });

  test('cliente deposita dinero en su cuenta', async ({ page }) => {
    await page.locator('.cuenta-item').first().click();
    await expect(page.locator('#modal-detalleCuenta')).toBeVisible();
    await expect(page.locator('#detalleNumeroCuenta')).not.toHaveText('');

    await page.click('#btnDepositarDetalle');
    await expect(page.locator('#modal-depositoForm')).toBeVisible();
    await expect(page.locator('#cuentaNumeroDeposito')).not.toHaveText('');

    await page.fill('#montoDeposito', '100');
    await page.click('#formDepositoForm button[type="submit"]');

    await expect(page.locator('#mensajeExitoDepositoForm')).toBeVisible();
    await expect(page.locator('#mensajeExitoDepositoForm')).toContainText('Depósito exitoso');
  });

  test('cliente retira dinero de su cuenta', async ({ page }) => {
    await page.locator('.cuenta-item').first().click();
    await expect(page.locator('#modal-detalleCuenta')).toBeVisible();
    await expect(page.locator('#detalleNumeroCuenta')).not.toHaveText('');

    await page.click('#btnRetirarDetalle');
    await expect(page.locator('#modal-retiroForm')).toBeVisible();
    await expect(page.locator('#cuentaNumeroRetiro')).not.toHaveText('');

    await page.fill('#montoRetiro', '50');
    await page.click('#formRetiro button[type="submit"]');

    await expect(page.locator('#mensajeExitoRetiro')).toBeVisible();
    await expect(page.locator('#mensajeExitoRetiro')).toContainText('Retiro exitoso');
  });

  test('cliente transfiere dinero a otra cuenta', async ({ page, cuentaDestino }) => {
    await page.locator('.cuenta-item').first().click();
    await expect(page.locator('#modal-detalleCuenta')).toBeVisible();
    await expect(page.locator('#detalleNumeroCuenta')).not.toHaveText('');

    await page.click('#btnTransferirDetalle');
    await expect(page.locator('#modal-transferenciaForm')).toBeVisible();
    await expect(page.locator('#cuentaNumeroOrigen')).not.toHaveText('');

    await page.fill('#cuentaDestino', cuentaDestino.numeroCuenta as string);
    await page.fill('#montoTransferencia', '100');
    await page.click('#formTransferenciaForm button[type="submit"]');

    await expect(page.locator('#mensajeExitoTransferenciaForm')).toBeVisible();
    await expect(page.locator('#mensajeExitoTransferenciaForm')).toContainText('Transferencia exitosa');
  });

  test('cliente ve el historial de transacciones', async ({ page }) => {
    await page.locator('.cuenta-item').first().click();
    await expect(page.locator('#modal-detalleCuenta')).toBeVisible();
    await expect(page.locator('#detalleNumeroCuenta')).not.toHaveText('');

    await page.click('#btnHistorialDetalle');
    await expect(page.locator('#modal-historialTransacciones')).toBeVisible();
    await expect(page.locator('#contenedorMovimientos')).toBeVisible();
  });
});
