export function uniqueDni(): string {
  return `PW-${Date.now().toString().slice(-8)}`;
}

export function uniqueEmail(): string {
  return `pwtest-${Date.now()}@test.com`;
}

export function clientePayload(overrides: Record<string, unknown> = {}) {
  return {
    dni: uniqueDni(),
    nombre: 'Test Playwright',
    primerApellido: 'Cliente',
    email: uniqueEmail(),
    fechaNacimiento: '1990-01-01',
    password: 'testpass123',
    ...overrides,
  };
}
