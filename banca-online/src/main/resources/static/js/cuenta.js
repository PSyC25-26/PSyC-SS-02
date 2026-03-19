// Abrir/cerrar modal
document.getElementById('btnAbrir').addEventListener('click', () => {
    document.getElementById('miForm').style.display = 'block';
});

document.getElementById('btnCerrar').addEventListener('click', cerrarFormulario);
document.getElementById('btnCancelar').addEventListener('click', cerrarFormulario);

function cerrarFormulario() {
    document.getElementById('miForm').style.display = 'none';
    document.getElementById('formCuenta').reset();
    document.getElementById('mensajeExito').style.display = 'none';
    document.getElementById('mensajeError').style.display = 'none';
}

// Cerrar modal al hacer clic fuera
window.addEventListener('click', (e) => {
    if (e.target === document.getElementById('miForm')) cerrarFormulario();
});

// #56 - Validación
function validarFormulario() {
    const clienteId = document.getElementById('clienteId').value;
    const tipoCuenta = document.getElementById('tipoCuenta').value;
    const saldo = document.getElementById('saldoInicial').value;

    if (!clienteId || parseInt(clienteId) <= 0) {
        mostrarError('El ID del cliente debe ser un numero positivo.');
        return false;
    }
    if (!tipoCuenta) {
        mostrarError('Tienes que seleccionar un tipo de cuenta.');
        return false;
    }
    if (saldo !== '' && parseFloat(saldo) < 0) {
        mostrarError('El saldo inicial no puede ser negativo.');
        return false;
    }
    return true;
}

// #54 - Integración con POST /cuentas + #55 - Mensajes éxito/error
document.getElementById('formCuenta').addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!validarFormulario()) return;

    const body = {
        clienteId: parseInt(document.getElementById('clienteId').value),
        tipoCuenta: document.getElementById('tipoCuenta').value,
        saldoInicial: parseFloat(document.getElementById('saldoInicial').value) || 0.0
    };

    try {
        const response = await fetch('/cuentas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (response.ok) {
            const data = await response.json();
            document.getElementById('mensajeExito').textContent =
                `¡Cuenta creada con éxito! Número: ${data.numeroCuenta}`;
            document.getElementById('mensajeExito').style.display = 'block';
            document.getElementById('mensajeError').style.display = 'none';
            document.getElementById('formCuenta').reset();
            cargarCuentas();
        } else {
            mostrarError('Error al crear la cuenta. Inténtalo de nuevo.');
        }
    } catch (error) {
        mostrarError('Error de conexión con el servidor.');
    }
});

function mostrarError(mensaje) {
    const div = document.getElementById('mensajeError');
    div.textContent = ' X ' + mensaje;
    div.style.display = 'block';
    document.getElementById('mensajeExito').style.display = 'none';
}

//Cargar y mostrar cuentas (requiere clienteId como parámetro URL)
async function cargarCuentas() {
    const params = new URLSearchParams(window.location.search);
    const clienteId = params.get('clienteId');
    const contenedor = document.getElementById('listaCuentas');

    if (!clienteId) {
        contenedor.innerHTML = '<p>Indica un <code>?clienteId=X</code> en la URL para ver las cuentas.</p>';
        return;
    }

    try {
        const response = await fetch(`/cuentas?clienteId=${clienteId}`);
        const cuentas = await response.json();

        if (cuentas.length === 0) {
            contenedor.innerHTML = '<p>No hay cuentas registradas para este cliente.</p>';
            return;
        }

        contenedor.innerHTML = cuentas.map(c => `
            <div class="cliente-card">
                <p class="cliente-nombre">${c.numeroCuenta}</p>
                <p><strong>Tipo:</strong> ${c.tipoCuenta}</p>
                <p class="cliente-email">Saldo: ${c.saldo.toFixed(2)} €</p>
            </div>
        `).join('');
    } catch (error) {
        contenedor.innerHTML = '<p>Error al cargar las cuentas.</p>';
    }
}

cargarCuentas();