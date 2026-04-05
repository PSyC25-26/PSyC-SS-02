// Estado global
let currentRole = null;
let currentClienteId = null; // Guardar ID del cliente logueado

// SIMULACIÓN LOGIN (despues habra que conectarlo con backend real)
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    // Aqui habrá que llamar al backend
    // Por ahora, hacemos mediante simulación:
    if (email === 'admin@banco.com' && password === 'admin123') {
        currentRole = 'admin';
        currentClienteId = null;
        iniciarSesion();
    } else if (email === 'cliente@banco.com' && password === 'cliente123') {
        currentRole = 'cliente';
        // TODO: Obtener el ID real del cliente desde el backend
        currentClienteId = 41; // ID del cliente de prueba
        iniciarSesion();
    } else {
        mostrarError('Credenciales inválidas');
    }
});

// INICIAR SESION
function iniciarSesion() {
    // Ocultar login, mostrar app
    document.getElementById('loginScreen').style.display = 'none';
    document.getElementById('mainApp').style.display = 'block';

    // Mostramos rol
    document.getElementById('userRole').innerHTML =
        `<strong>Rol:</strong> ${currentRole === 'admin' ? '👑 Administrador' : '👤 Cliente'}`;

    // Si es admin, generar menú normal
    if (currentRole === 'admin') {
        generarMenuAdmin();
    } else {
        // Si es cliente, cargar directamente sus cuentas
        cargarCuentasCliente();
    }
}

// Menú para ADMIN
function generarMenuAdmin() {
    const menuDiv = document.getElementById('menuDinamico');
    menuDiv.innerHTML = '';

    const opciones = [
        { nombre: '📋 Gestionar Clientes', modal: 'listarClientes', archivo: 'listarClientes.html' },
        { nombre: '🏦 Gestionar Cuentas', modal: 'listarCuentas', archivo: 'listarCuentas.html' },
        { nombre: '🔍 Consultar Cuentas', modal: 'consultarCuentasForm', archivo: 'consultarCuentas.html' }
    ];

    opciones.forEach(op => {
        const btn = document.createElement('button');
        btn.textContent = op.nombre;
        btn.className = 'btn-menu'; // Mantiene el estilo de los otros botones
        btn.onclick = () => GestorModales.abrir(op.modal, op.archivo);
        menuDiv.appendChild(btn);
    });
}

// Cargar cuentas del cliente directamente (sin menú)
async function cargarCuentasCliente() {
    const menuDiv = document.getElementById('menuDinamico');
    const modalContainer = document.getElementById('modalContainer');

    // Limpiar menú y modal
    menuDiv.innerHTML = '';
    modalContainer.innerHTML = '';

    // Mostrar título
    const titulo = document.createElement('h2');
    titulo.textContent = '🏦 Mis Cuentas Bancarias';
    titulo.style.marginBottom = '20px';
    menuDiv.appendChild(titulo);

    // Crear contenedor para las cuentas
    const cuentasContainer = document.createElement('div');
    cuentasContainer.id = 'listaCuentasCliente';
    cuentasContainer.innerHTML = '<p>🔄 Cargando sus cuentas...</p>';
    menuDiv.appendChild(cuentasContainer);

    // Cargar cuentas
    await cargarCuentasDelCliente();
}

async function cargarCuentasDelCliente() {
    const cuentasContainer = document.getElementById('listaCuentasCliente');
    if (!cuentasContainer) return;

    try {
        const response = await fetch(`/api/cuentas?clienteId=${currentClienteId}`);

        if (response.ok) {
            const cuentas = await response.json();
            mostrarCuentasCliente(cuentas);
        } else if (response.status === 500 || response.status === 404) {
            cuentasContainer.innerHTML = `
                <div style="text-align: center; padding: 40px;">
                    <p>📭 <strong>No tiene cuentas registradas</strong></p>
                    <p>Contacte con el administrador para crear una cuenta.</p>
                </div>
            `;
        } else {
            cuentasContainer.innerHTML = '<p class="error">Error al cargar sus cuentas</p>';
        }
    } catch (error) {
        console.error('Error:', error);
        cuentasContainer.innerHTML = '<p class="error">Error de conexión con el servidor</p>';
    }
}

function mostrarCuentasCliente(cuentas) {
    const cuentasContainer = document.getElementById('listaCuentasCliente');
    if (!cuentasContainer) return;

    if (cuentas.length === 0) {
        cuentasContainer.innerHTML = `
            <div style="text-align: center; padding: 40px;">
                <p>📭 <strong>No tiene cuentas registradas</strong></p>
                <p>Contacte con el administrador para crear una cuenta.</p>
            </div>
        `;
        return;
    }

    // Mostrar como lista simple
    cuentasContainer.innerHTML = `
        <div class="lista-cuentas">
            <div class="lista-header">
                <span>Número de Cuenta</span>
                <span>Saldo</span>
                <span>Tipo</span>
            </div>
            <div class="lista-items">
                ${cuentas.map(cuenta => `
                    <div class="cuenta-item" data-cuenta='${JSON.stringify(cuenta)}'>
                        <span class="cuenta-numero-lista">${cuenta.numeroCuenta}</span>
                        <span class="cuenta-saldo-lista">${cuenta.saldo?.toFixed(2) || '0.00'} €</span>
                        <span class="cuenta-tipo-lista">${cuenta.tipoCuenta === 'CORRIENTE' ? '💳 Corriente' : '🏦 Ahorro'}</span>
                    </div>
                `).join('')}
            </div>
        </div>
    `;

    // Evento: al hacer clic en una cuenta, abrir modal de detalles
    document.querySelectorAll('.cuenta-item').forEach(item => {
        item.addEventListener('click', () => {
            const cuenta = JSON.parse(item.dataset.cuenta);
            window.cuentaSeleccionada = cuenta;
            GestorModales.abrir('detalleCuenta', 'detalleCuenta.html');
        });
    });
}

// Función para recargar las cuentas del cliente (después de operaciones)
window.recargarCuentasCliente = function() {
    if (currentRole === 'cliente') {
        cargarCuentasCliente();
    }
};

// CERRAR SESION
document.getElementById('logoutBtn').addEventListener('click', () => {
    currentRole = null;
    currentClienteId = null;
    document.getElementById('mainApp').style.display = 'none';
    document.getElementById('loginScreen').style.display = 'flex';
    document.getElementById('modalContainer').innerHTML = '';
    document.getElementById('menuDinamico').innerHTML = '';
});

// MOSTRAR ERRORES
function mostrarError(mensaje) {
    const errorDiv = document.getElementById('loginError');
    if (errorDiv) {
        errorDiv.textContent = mensaje;
        errorDiv.style.display = 'block';
    } else {
        console.error('Error: ', mensaje);
    }
}

// QUITAR ERRORES AL ESCRIBIR
document.getElementById('loginForm').addEventListener('input', () => {
    const errorDiv = document.getElementById('loginError');
    if (errorDiv) errorDiv.style.display = 'none';
});