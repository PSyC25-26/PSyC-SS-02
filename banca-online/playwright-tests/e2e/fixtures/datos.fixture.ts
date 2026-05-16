import { test as adminTest } from './admin.fixture';
import { loginComoAdmin, crearCliente, crearCuenta } from '../../support/api';
import { clientePayload } from '../../support/data';

type DatosFixtures = {
  clientePrevio: Record<string, unknown>;
  cuentaPrevia: Record<string, unknown>;
};

export const test = adminTest.extend<DatosFixtures>({
  clientePrevio: async ({ request }, use) => {
    const token = await loginComoAdmin(request);
    const res = await crearCliente(request, token, clientePayload());
    await use(await res.json());
  },

  cuentaPrevia: async ({ request, clientePrevio }, use) => {
    const token = await loginComoAdmin(request);
    const res = await crearCuenta(request, token, {
      clienteId: clientePrevio.id,
      tipoCuenta: 'CORRIENTE',
      saldoInicial: 1000.0,
    });
    await use(await res.json());
  },
});

export { expect } from './admin.fixture';
