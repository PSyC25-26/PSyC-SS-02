import { request } from '@playwright/test';
import { BASE_URL } from './env';

export async function assertBackendReachable(baseURL = BASE_URL): Promise<void> {
  const ctx = await request.newContext();
  try {
    const res = await ctx.get(baseURL, { timeout: 5_000 });
    if (!res.ok() && res.status() !== 403) {
      throw new Error(`Backend respondió con status inesperado: ${res.status()}`);
    }
  } finally {
    await ctx.dispose();
  }
}
