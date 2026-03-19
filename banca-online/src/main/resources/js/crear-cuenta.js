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
