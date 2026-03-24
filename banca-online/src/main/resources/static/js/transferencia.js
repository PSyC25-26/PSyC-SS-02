//URL de la API
const BASE_URL = 'http://localhost:8080';

const formTransferencia = document.getElementById('formTransferencia');
const btnTransferir = document.getElementById('btnTransferir');
const btnTexto = document.getElementById('btnTexto');
const btnLimpiar = document.getElementById('btnLimpiar');
const mensajeTransferencia = document.getElementById('mensajeTransferencia');
const resumenTransferencia = document.getElementById('resumenTransferencia');

//Mostrar mensaje
function mostrarMensaje(texto, tipo) {
    mensajeTransferencia.textContent = texto;
    mensajeTransferencia.className = "mensaje-transferencia " + tipo;
    mensajeTransferencia.style.display = "block";
}

// Ocultar mensaje
function ocultarMensaje() {
    mensajeTransferencia.style.display = "none";
}

// Mostrar resumen datos
function mostrarResumen(data) {
    document.getElementById('resumenOrigen').textContent = data.cuentaOrigen;
    document.getElementById('resumenDestino').textContent = data.cuentaDestino;
    document.getElementById('resumenCantidad').textContent =
        data.cantidad + " €";

    resumenTransferencia.style.display = "block";
}

// Ocultar resumen
function ocultarResumen() {
    resumenTransferencia.style.display = "none";
}

// Spinner
function setLoading(loading) {
    btnTransferir.disabled = loading;

    if (loading) {
        btnTexto.innerHTML = '<span class="loading-spinner"></span> Procesando...';
    } else {
        btnTexto.textContent = "Realizar Transferencia";
    }
}

// Limpiar el form
btnLimpiar.addEventListener('click', () => {
    formTransferencia.reset();
    ocultarMensaje();
    ocultarResumen();
});

// Enviar el form POST
formTransferencia.addEventListener('submit', async (e) => {
    e.preventDefault();

    ocultarMensaje();
    ocultarResumen();

    const cuentaOrigen = document.getElementById('cuentaOrigen').value.trim();
    const cuentaDestino = document.getElementById('cuentaDestino').value.trim();
    const cantidad = parseFloat(document.getElementById('cantidad').value);

    // Validaciones
    if (!cuentaOrigen || !cuentaDestino) {
        mostrarMensaje('Completa todos los campos', 'error');
        return;
    }

    if (cuentaOrigen === cuentaDestino) {
        mostrarMensaje('Las cuentas no pueden ser iguales', 'error');
        return;
    }

    if (isNaN(cantidad) || cantidad <= 0) {
        mostrarMensaje('Cantidad inválida', 'error');
        return;
    }

    const datos = { cuentaOrigen, cuentaDestino, cantidad };

    setLoading(true);

    try {
        const response = await fetch(BASE_URL + '/cuentas/transferir', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(datos)
        });

        if (response.ok) {
            const data = await response.json();

            mostrarMensaje('Transferencia realizada con éxito', 'exito');
            mostrarResumen(data);
            formTransferencia.reset();

        } else if (response.status === 404) {
            mostrarMensaje('Cuenta no encontrada', 'no-encontrada');

        } else {
            mostrarMensaje('Error al realizar la transferencia', 'error');
        }

    } catch (error) {
        console.error(error);
        mostrarMensaje('Error de conexión', 'error');
    }

    setLoading(false);
});
