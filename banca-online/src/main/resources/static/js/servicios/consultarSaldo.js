// Función para mostrar errores
function mostrarError(mensaje) {
    const mensajeError = document.getElementById('mensajeError');
    if (!mensajeError) return;

    mensajeError.style.display = 'block';
    mensajeError.textContent = '❌ ' + mensaje;

    const mensajeExito = document.getElementById('mensajeExito');
    if (mensajeExito) mensajeExito.style.display = 'none';

    const saldoMostrado = document.getElementById('saldoMostrado');
    if (saldoMostrado) saldoMostrado.textContent = '';
}

// Función para mostrar saldo correctamente
function mostrarSaldo(saldo) {
    const mensajeExito = document.getElementById('mensajeExito');
    const saldoMostrado = document.getElementById('saldoMostrado');

    if (mensajeExito) {
        mensajeExito.style.display = 'block';
        mensajeExito.textContent = '✅ Saldo cargado correctamente.';
    }

    if (saldoMostrado) {
        saldoMostrado.textContent = `Saldo actual: ${saldo.toFixed(2)} €`;
    }

    const mensajeError = document.getElementById('mensajeError');
    if (mensajeError) mensajeError.style.display = 'none';
}

// Función principal para consultar saldo
async function consultarSaldo(cuentaId) {
    if (!cuentaId || parseInt(cuentaId) <= 0) {
        mostrarError('Introduce un ID de cuenta válido.');
        return;
    }

    try {
        const response = await fetch(`/api/cuentas/saldo/${cuentaId}`);

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

// Función de inicialización (se llama cuando el modal se abre)
function initConsultarSaldo() {
    const formSaldo = document.getElementById('formSaldo');

    if (!formSaldo) {
        console.error('Formulario de saldo no encontrado');
        return;
    }

    // Remover listener anterior para evitar duplicados
    const nuevoForm = formSaldo.cloneNode(true);
    formSaldo.parentNode.replaceChild(nuevoForm, formSaldo);

    // Agregar el listener al nuevo formulario
    nuevoForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const cuentaId = document.getElementById('cuentaId').value.trim();
        consultarSaldo(cuentaId);
    });
}

// Exportar para uso global
window.initConsultarSaldo = initConsultarSaldo;