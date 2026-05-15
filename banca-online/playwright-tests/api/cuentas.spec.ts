import { test, expect } from '@playwright/test';
import { BASE_URL } from '../support/env';
import { loginComoAdmin, bearerHeaders, getClientes } from '../support/api';

test.describe('API — cuentas', () => {
  test('POST /api/cuentas crea una cuenta y retorna 201', async ({ request }) => {
    const token = await loginComoAdmin(request);
    const clientesRes = await getClientes(request, token);
    const clientes = await clientesRes.json();

    test.skip(clientes.length === 0, 'No hay clientes en BD — se omite el test de cuenta');

    const res = await request.post(`${BASE_URL}/api/cuentas`, {
      headers: bearerHeaders(token),
      data: {
        clienteId: clientes[0].id,
        tipoCuenta: 'CORRIENTE',
        saldoInicial: 500.0,
      },
    });

    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.numeroCuenta).toBeDefined();
    expect(body.numeroCuenta.startsWith('ES')).toBe(true);
    expect(body.saldo).toBe(500.0);
  });
});
