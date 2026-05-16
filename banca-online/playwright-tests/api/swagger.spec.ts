import { test, expect } from '@playwright/test';
import { BASE_URL } from '../support/env';

test.describe('API Swagger Doc', () => {
  test('GET /swagger-ui.html retorna 403 (bloqueado por seguridad)', async ({ request }) => {
    const res = await request.get(`${BASE_URL}/swagger-ui.html`);
    expect(res.status()).toBe(403);
  });
});
