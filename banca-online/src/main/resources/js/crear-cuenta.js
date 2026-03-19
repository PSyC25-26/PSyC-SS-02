//Validar los datos en la interfaz
function validarFormulario() {
    let valido = true;

    const clienteId = document.getElementById('clienteId').value.trim();
    const tipoCuenta = document.getElementById('tipoCuenta').value;
    const saldoInicial = document.getElementById('saldoInicial').value;

    //errores
    document.querySelectorAll('.error-msg').forEach(e => e.style.display = 'none');
    document.querySelectorAll('input, select').forEach(e => e.classList.remove('error'));

    if (!clienteId || parseInt(clienteId) <= 0) {
        mostrarErrorCampo('clienteId', 'err-clienteId');
        valido = false;
    }

    if (!tipoCuenta) {
        mostrarErrorCampo('tipoCuenta', 'err-tipoCuenta');
        valido = false;
    }

    if (saldoInicial !== '' && parseFloat(saldoInicial) < 0) {
        mostrarErrorCampo('saldoInicial', 'err-saldoInicial');
        valido = false;
    }

    return valido;
}

function mostrarErrorCampo(campoId, errId) {
    document.getElementById(campoId).classList.add('error');
    document.getElementById(errId).style.display = 'block';
}

//Integrar formulario con endpoint POST /cuentas
//Probar la creación desde el frontend
async function crearCuenta() {
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

        const data = await response.json();

        //Mostrar mensajes de éxito o error
        if (response.ok) {
            mostrarMensaje(
                `✅ Cuenta creada con éxito. Número: ${data.numeroCuenta} | Saldo: ${data.saldo}€`,
                'exito'
            );
            limpiarFormulario();
        } else {
            mostrarMensaje(`❌ Error: ${data.message || 'No se pudo crear la cuenta.'}`, 'error');
        }
    } catch (error) {
        mostrarMensaje('❌ Error de conexión con el servidor.', 'error');
    }
}

function mostrarMensaje(texto, tipo) {
    const div = document.getElementById('mensaje');
    div.textContent = texto;
    div.className = tipo;
    div.style.display = 'block';
}

function limpiarFormulario() {
    document.getElementById('clienteId').value = '';
    document.getElementById('tipoCuenta').value = '';
    document.getElementById('saldoInicial').value = '';
}