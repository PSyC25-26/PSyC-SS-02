import { test, expect } from './fixtures/datos.fixture';
import { clientePayload } from '../support/data';
import { ModalHost } from './pages/ModalHost';

test.describe('E2E parte de admin gestión de clientes', () => {
  test('admin crea un cliente nuevo desde el formulario', async ({ page, adminShell }) => {
    await adminShell.clickMenuItem('📋 Gestionar Clientes');
    const modal = new ModalHost(page);
    await modal.waitForContent('#listaClientes');

    await page.click('#btnCrearCliente');
    await expect(page.locator('#modal-crearClienteForm')).toBeVisible();

    const payload = clientePayload();
    await page.fill('#nombre', payload.nombre as string);
    await page.fill('#primerApellido', payload.primerApellido as string);
    await page.fill('#dni', payload.dni as string);
    await page.fill('#email', payload.email as string);
    await page.fill('#password', payload.password as string);
    await page.fill('#fechaNacimiento', payload.fechaNacimiento as string);

    await page.click('#formCliente button[type="submit"]');
    await expect(page.locator('#mensajeExito')).toBeVisible();
    await expect(page.locator('#mensajeExito')).toContainText('Cliente creado con éxito');
  });

  test('admin edita un cliente existente', async ({ page, adminShell, clientePrevio }) => {
    await adminShell.clickMenuItem('📋 Gestionar Clientes');
    const modal = new ModalHost(page);
    await modal.waitForContent('#listaClientes');

    // Esperar a que cargue la fila del cleinte
    await expect(page.locator(`.fila-cliente[data-id="${clientePrevio.id}"]`)).toBeVisible();
    await page.locator(`.fila-cliente[data-id="${clientePrevio.id}"]`).click();

    await page.click('#btnEditarCliente');
    await expect(page.locator('#modal-editarClienteForm')).toBeVisible();
    // Esperar a que el formulario se rellene
    await expect(page.locator('#editNombre')).not.toHaveValue('');

    await page.fill('#editTelefono', '666123456');
    await page.click('#formEditarCliente button[type="submit"]');

    await expect(page.locator('#mensajeExitoEditar')).toBeVisible();
    await expect(page.locator('#mensajeExitoEditar')).toContainText('actualizado con éxito');
  });
});
