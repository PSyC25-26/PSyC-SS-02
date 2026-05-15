import { test, expect } from '@playwright/test';
import { BASE_URL, ADMIN_EMAIL, ADMIN_PASSWORD } from '../support/env';

test.describe('API — auth', () => {
  test('login retorna token JWT y rol ADMIN', async ({ request }) => {
    const res = await request.post(`${BASE_URL}/api/auth/login`, {
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.token).toBeDefined();
    expect(body.token.length).toBeGreaterThan(10);
    expect(body.rol).toBe('ADMIN');
  });

  test('login con credenciales incorrectas retorna 401', async ({ request }) => {
    const res = await request.post(`${BASE_URL}/api/auth/login`, {
      data: { email: 'malo@test.com', password: 'wrongpassword' },
    });

    expect(res.status()).toBe(401);
  });
});
