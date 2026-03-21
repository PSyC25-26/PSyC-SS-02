// Captura submit del formulario
document.getElementById('formSaldo').addEventListener('submit', async (e) => {
    e.preventDefault();

    const cuentaId = document.getElementById('cuentaId').value.trim();

    // Validación rápida
    if (!cuentaId || parseInt(cuentaId) <= 0) {
        mostrarError('El ID de cuenta debe ser un número positivo.');
        return;
    }

    try {
        const response = await fetch(`/saldo/${cuentaId}`);
        const divSaldo = document.getElementById('saldoMostrado');

        if (response.ok) {
            const data = await response.json();
            divSaldo.textContent = `Saldo de la cuenta ${data.numeroCuenta}: ${data.saldo.toFixed(2)} €`;
            divSaldo.style.color = 'green';
            document.getElementById('mensajeError').style.display = 'none';
        } else if (response.status === 404) {
            mostrarError('La cuenta no existe.');
            divSaldo.textContent = '';
        } else {
            mostrarError('Error al consultar el saldo.');
            divSaldo.textContent = '';
        }
    } catch (error) {
        mostrarError('Error de conexión con el servidor.');
        document.getElementById('saldoMostrado').textContent = '';
    }
});

// Función reutilizable para mostrar errores
function mostrarError(mensaje) {
    const div = document.getElementById('mensajeError');
    div.textContent = ' X ' + mensaje;
    div.style.display = 'block';
    div.style.color = 'red';
    document.getElementById('saldoMostrado').textContent = '';
}

// Función para cargar el saldo desde el endpoint /saldo/{cuentaId}
async function consultarSaldo(cuentaId) {
    const mensajeExito = document.getElementById('mensajeExito');
    const mensajeError = document.getElementById('mensajeError');
    const saldoMostrado = document.getElementById('saldoMostrado');

    mensajeExito.style.display = 'none';
    mensajeError.style.display = 'none';
    saldoMostrado.textContent = '';

    try {
        const response = await fetch(`/saldo/${cuentaId}`);
        if (!response.ok) throw new Error('Cuenta no encontrada');

        const data = await response.json();
        saldoMostrado.textContent = `Saldo actual: ${data.saldo.toFixed(2)} €`;
        mensajeExito.style.display = 'block';
        mensajeExito.textContent = 'Saldo cargado correctamente.';
    } catch (error) {
        mensajeError.style.display = 'block';
        mensajeError.textContent = 'Error: ' + error.message;
    }
}

// Escuchar submit del formulario
document.getElementById('formSaldo').addEventListener('submit', (e) => {
    e.preventDefault();
    const cuentaId = document.getElementById('cuentaId').value;
    if (cuentaId && parseInt(cuentaId) > 0) {
        consultarSaldo(cuentaId);
    } else {
        const mensajeError = document.getElementById('mensajeError');
        mensajeError.style.display = 'block';
        mensajeError.textContent = 'Introduce un ID de cuenta válido.';
    }
});

// Ejecutar al cargar la página
document.addEventListener('DOMContentLoaded', cargarSaldo);