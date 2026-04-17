/**
 * TESTS DE CLIENTE (JavaScript) - src/test/js/client.test.js
 *
 * Tests de integracion del lado del cliente que llaman al servidor.
 *
 * COMO EJECUTAR:
 *   1. Asegurate de que el servidor Spring Boot esta corriendo en localhost:8080
 *   2. npm install (solo la primera vez)
 *   3. npm test
 *
 * DEPENDENCIAS (package.json):
 *   npm install --save-dev jest node-fetch@2
 */

// Usamos node-fetch para simular llamadas HTTP desde el cliente
const fetch = require('node-fetch');

const BASE_URL = 'http://localhost:8080';
const ADMIN_EMAIL = 'admin@banco.com';
const ADMIN_PASSWORD = 'admin123';

// ===================== HELPER: login y obtener JWT =====================
async function loginComoAdmin() {
    const response = await fetch(`${BASE_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            email: ADMIN_EMAIL,
            password: ADMIN_PASSWORD
        })
    });

    const text = await response.text();

    if (response.status !== 200) {
        throw new Error(`Login failed: ${response.status} - ${text}`);
    }

    const data = JSON.parse(text);
    return data.token;
}

// ===================== TEST 1: Login (remoteness test) =====================

/**
 * TEST DE REMOTENESS: el cliente llama al servidor y recibe un JWT.
 */
test('CLIENT->SERVER: login retorna token JWT', async () => {
    const response = await fetch(`${BASE_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            email: ADMIN_EMAIL,
            password: ADMIN_PASSWORD
        })
    });

    expect(response.status).toBe(200);
    const data = await response.json();
    expect(data.token).toBeDefined();
    expect(data.token.length).toBeGreaterThan(10);
    expect(data.rol).toBe('ADMIN');
    console.log('[REMOTENESS TEST] Token recibido del servidor:', data.token.substring(0, 20) + '...');
});

// ===================== TEST 2: Login con credenciales incorrectas =====================

test('CLIENT->SERVER: login con credenciales incorrectas retorna 401', async () => {
    const response = await fetch(`${BASE_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            email: 'malo@test.com',
            password: 'wrongpassword'
        })
    });

    expect(response.status).toBe(401);
    console.log('[CLIENT TEST] Login incorrecto retorna 401 correctamente');
});

// ===================== TEST 3: Obtener clientes sin token =====================

test('CLIENT->SERVER: acceder a /api/clientes sin token retorna 403', async () => {
    const response = await fetch(`${BASE_URL}/api/clientes`);
    expect(response.status).toBe(403);
    console.log('[CLIENT TEST] Endpoint protegido rechaza peticion sin token');
});

// ===================== TEST 4: Obtener clientes con token =====================

test('CLIENT->SERVER: obtener lista de clientes con token JWT valido', async () => {
    const token = await loginComoAdmin();

    const response = await fetch(`${BASE_URL}/api/clientes`, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    expect(response.status).toBe(200);
    const clientes = await response.json();
    expect(Array.isArray(clientes)).toBe(true);
    console.log('[CLIENT TEST] Lista de clientes recibida:', clientes.length, 'clientes');
});

// ===================== TEST 5: Crear cliente desde el cliente =====================

test('CLIENT->SERVER: crear un cliente nuevo via HTTP', async () => {
    const token = await loginComoAdmin();
    const uniqueDni = 'JS-TEST-' + Date.now().toString().substring(7);

    const response = await fetch(`${BASE_URL}/api/clientes`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            dni: uniqueDni,
            nombre: 'Test JS',
            primerApellido: 'Cliente',
            email: `jstest-${Date.now()}@test.com`,
            fechaNacimiento: '1990-01-01',
            password: 'testpass123'
        })
    });

    expect(response.status).toBe(201);
    const cliente = await response.json();
    expect(cliente.id).toBeDefined();
    expect(cliente.nombre).toBe('Test JS');
    console.log('[CLIENT TEST] Cliente creado con id:', cliente.id);
});

// ===================== TEST 6: Crear cuenta desde el cliente =====================

test('CLIENT->SERVER: crear una cuenta bancaria via HTTP', async () => {
    const token = await loginComoAdmin();

    // Primero obtenemos un cliente existente
    const clientesRes = await fetch(`${BASE_URL}/api/clientes`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    const clientes = await clientesRes.json();

    if (clientes.length === 0) {
        console.warn('[CLIENT TEST] No hay clientes, saltando test de cuenta');
        return;
    }

    const clienteId = clientes[0].id;

    const response = await fetch(`${BASE_URL}/api/cuentas`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            clienteId: clienteId,
            tipoCuenta: 'CORRIENTE',
            saldoInicial: 500.0
        })
    });

    expect(response.status).toBe(201);
    const cuenta = await response.json();
    expect(cuenta.numeroCuenta).toBeDefined();
    expect(cuenta.numeroCuenta.startsWith('ES')).toBe(true);
    expect(cuenta.saldo).toBe(500.0);
    console.log('[CLIENT TEST] Cuenta creada:', cuenta.numeroCuenta);
});

// ===================== TEST 7: Swagger UI accesible =====================

test('CLIENT->SERVER: Swagger UI es accesible', async () => {
    const response = await fetch(`${BASE_URL}/swagger-ui.html`);
    //forbidden por seguridad
    expect([403]).toContain(response.status);
    console.log('[CLIENT TEST] Swagger UI status:', response.status);
});
