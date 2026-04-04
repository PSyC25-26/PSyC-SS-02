// Función para mostrar errores
function mostrarError(mensaje) {
    const mensajeError = document.getElementById('mensajeError');
    mensajeError.style.display = 'block';
    mensajeError.textContent = '❌ ' + mensaje;

    document.getElementById('mensajeExito').style.display = 'none';
    document.getElementById('saldoMostrado').textContent = '';
}

// Función para mostrar saldo correctamente
function mostrarSaldo(saldo) {
    const mensajeExito = document.getElementById('mensajeExito');
    const saldoMostrado = document.getElementById('saldoMostrado');

    mensajeExito.style.display = 'block';
    mensajeExito.textContent = '✅ Saldo cargado correctamente.';
    saldoMostrado.textContent = `Saldo actual: ${saldo.toFixed(2)} €`;

    document.getElementById('mensajeError').style.display = 'none';
}

// Función principal para consultar saldo
async function consultarSaldo(cuentaId) {
    if (!cuentaId || parseInt(cuentaId) <= 0) {
        mostrarError('Introduce un ID de cuenta válido.');
        return;
    }

    try {
        const response = await fetch(`/cuentas/saldo/${cuentaId}`);

        if (!response.ok) {
            if (response.status === 404) {
                mostrarError('La cuenta no existe.');
            } else {
                mostrarError('Error al obtener el saldo.');
            }
            return;
        }

        const data = await response.json();

        if (data.saldo == null) {
            mostrarError('No hay saldo disponible para esta cuenta.');
            return;
        }

        mostrarSaldo(data.saldo);
    } catch (error) {
        mostrarError('Error de conexión con el servidor.');
        console.error(error);
    }
}

// Listener único de submit
document.getElementById('formSaldo').addEventListener('submit', (e) => {
    e.preventDefault();
    const cuentaId = document.getElementById('cuentaId').value.trim();
    consultarSaldo(cuentaId);
});