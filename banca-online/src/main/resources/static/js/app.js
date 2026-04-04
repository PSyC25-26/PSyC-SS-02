// Estado global
let currentRole = null;

// SIMULACIÓN LOGIN (despues habra que conectarlo con backend real)
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    // Aqui habrá que llamar al backend
    // Por ahora, hacemos mediante simulación:
    if (email === 'admin@banco.com' && password === 'admin123') {
        currentRole = 'admin';
        iniciarSesion();
    } else if (email === 'cliente@banco.com' && password === 'cliente123') {
        currentRole = 'cliente';
        iniciarSesion();
    } else {
        mostrarError('Credenciales inválidas');
    }
});


// INICIAR SESION
function iniciarSesion() {
    // Ocultar login, mostrar app
    document.getElementById('loginScreen').style.display = 'none'; //Desactivamos el login
    document.getElementById('mainApp').style.display = 'block'; //Activamos el main

    // Mostramos rol
    document.getElementById('userRole').innerHTML =
        `<strong>Rol:</strong> ${currentRole === 'admin' ? '👑 Administrador' : '👤 Cliente'}`;

    // Generamos el menu según el rol
    generarMenu();
}



// CERRAR SESION
document.getElementById('logoutBtn').addEventListener('click', () => {
    currentRole = null;
    document.getElementById('mainApp').style.display = 'none';
    document.getElementById('loginScreen').style.display = 'flex';
    document.getElementById('modalContainer').innerHTML = '';
});


function generarMenu() {
    const menuDiv = document.getElementById('menuDinamico');
    menuDiv.innerHTML = '';

    const opciones = currentRole === 'admin'
        ? [
            { nombre: '➕ Crear cliente', modal: 'crearCliente', archivo: 'clienteForm.html' },
            { nombre: '➕ Crear cuenta', modal: 'crearCuenta', archivo: 'crearCuenta.html' },
            { nombre: '📋 Ver clientes', modal: 'verClientes', archivo: 'verClientes.html' }
        ]
        : [
            { nombre: '💰 Consultar saldo', modal: 'consultarSaldo', archivo: 'consultarSaldo.html' }
        ];

    opciones.forEach(op => {
        const btn = document.createElement('button');
        btn.textContent = op.nombre;
        btn.className = 'btn-menu';
        btn.onclick = () => GestorModales.abrir(op.modal, op.archivo);
        menuDiv.appendChild(btn);
    });
}


// MOSTRAR ERRORES
function mostrarError(mensaje) {
    const errorDiv = document.getElementById('loginError');
    if (errorDiv) {
        errorDiv.textContent = mensaje;
        errorDiv.style.display = 'block';
    } else {
        console.error('Error: ', mensaje); // fallback
    }
}

// QUITAR ERRORES AL ESCRIBIR
document.getElementById('loginForm').addEventListener('input', () => {
    const errorDiv = document.getElementById('loginError');
    if (errorDiv) errorDiv.style.display = 'none';
});