import { test as base } from '@playwright/test';
import { loginComoAdmin, crearCliente, crearCuenta } from '../../support/api';
import { clientePayload } from '../../support/data';

type SesionClienteFixtures = {
  clienteInfo: Record<string, unknown>;
  cuentaInfo: Record<string, unknown>;
  cuentaDestino: Record<string, unknown>;
};

export const test = base.extend<SesionClienteFixtures>({
  clienteInfo: async ({ request }, use) => {
    const token = await loginComoAdmin(request);
    const payload = clientePayload();
    const res = await crearCliente(request, token, payload);
    const cliente = await res.json();
    await use({ ...cliente, _password: payload.password });
  },

  cuentaInfo: async ({ request, clienteInfo }, use) => {
    const token = await loginComoAdmin(request);
    const res = await crearCuenta(request, token, {
      clienteId: clienteInfo.id,
      tipoCuenta: 'CORRIENTE',
      saldoInicial: 1000.0,
    });
    await use(await res.json());
  },

  cuentaDestino: async ({ request, clienteInfo }, use) => {
    const token = await loginComoAdmin(request);
    const res = await crearCuenta(request, token, {
      clienteId: clienteInfo.id,
      tipoCuenta: 'AHORRO',
      saldoInicial: 0,
    });
    await use(await res.json());
  },
});

export { expect } from '@playwright/test';
