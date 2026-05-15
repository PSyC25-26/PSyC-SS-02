import { test, expect } from '@playwright/test';
import { BASE_URL } from '../support/env';
import { loginComoAdmin, bearerHeaders } from '../support/api';
import { clientePayload } from '../support/data';

test.describe('API — clientes', () => {
  test('GET /api/clientes sin token retorna 403', async ({ request }) => {
    const res = await request.get(`${BASE_URL}/api/clientes`);
    expect(res.status()).toBe(403);
  });

  test('GET /api/clientes con JWT retorna 200 y array', async ({ request }) => {
    const token = await loginComoAdmin(request);
    const res = await request.get(`${BASE_URL}/api/clientes`, {
      headers: bearerHeaders(token),
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(Array.isArray(body)).toBe(true);
  });

  test('POST /api/clientes crea un cliente y retorna 201', async ({ request }) => {
    const token = await loginComoAdmin(request);
    const res = await request.post(`${BASE_URL}/api/clientes`, {
      headers: bearerHeaders(token),
      data: clientePayload(),
    });

    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.id).toBeDefined();
    expect(body.nombre).toBe('Test Playwright');
  });
});
