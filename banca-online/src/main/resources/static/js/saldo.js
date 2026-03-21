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