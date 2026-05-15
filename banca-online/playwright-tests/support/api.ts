import { APIRequestContext } from '@playwright/test';
import { BASE_URL, ADMIN_EMAIL, ADMIN_PASSWORD } from './env';

export async function loginComoAdmin(request: APIRequestContext): Promise<string> {
  const res = await request.post(`${BASE_URL}/api/auth/login`, {
    data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
  });
  const body = await res.json();
  return body.token as string;
}

export function bearerHeaders(token: string) {
  return { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };
}

export async function getClientes(request: APIRequestContext, token: string) {
  return request.get(`${BASE_URL}/api/clientes`, { headers: bearerHeaders(token) });
}

export async function crearCliente(
  request: APIRequestContext,
  token: string,
  data: Record<string, unknown>,
) {
  return request.post(`${BASE_URL}/api/clientes`, {
    headers: bearerHeaders(token),
    data,
  });
}

export async function crearCuenta(
  request: APIRequestContext,
  token: string,
  data: Record<string, unknown>,
) {
  return request.post(`${BASE_URL}/api/cuentas`, {
    headers: bearerHeaders(token),
    data,
  });
}
