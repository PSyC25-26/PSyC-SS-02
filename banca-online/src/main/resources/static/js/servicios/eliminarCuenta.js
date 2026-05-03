function initEliminarCuenta() {
    // Usamos un pequeño timeout para asegurar que el HTML se ha cargado en el DOM
    setTimeout(() => {
        const cuenta = window.cuentaSeleccionada;
        const infoDiv = document.getElementById('infoCuentaEliminar');
        const btnConfirmar = document.getElementById('btnConfirmarEliminar');
        const btnCancelar = document.getElementById('btnCancelarEliminar');
        const mensajeExito = document.getElementById('mensajeExitoEliminar');
        const mensajeError = document.getElementById('mensajeErrorEliminar');

        if (!cuenta) {
            console.error("No hay cuenta seleccionada");
            return;
        }

        if (infoDiv) {
            infoDiv.innerHTML = `
                <strong>Número:</strong> ${cuenta.numeroCuenta}<br>
                <strong>Saldo actual:</strong> ${cuenta.saldo.toFixed(2)}€<br>
                <strong>Titular ID:</strong> ${cuenta.clienteId}
            `;
        }

        btnConfirmar.onclick = async () => {
            mensajeError.style.display = 'none';
            btnConfirmar.disabled = true;
            btnConfirmar.textContent = 'Procesando...';

            try {
                const response = await fetchConAuth(`/api/cuentas/${cuenta.id}`, {
                    method: 'DELETE'
                });

                if (response.ok) {
                    mensajeExito.textContent = '✅ Cuenta desactivada correctamente';
                    mensajeExito.style.display = 'block';

                    setTimeout(() => {
                        GestorModales.cerrar('eliminarCuenta');
                        const selectCliente = document.getElementById('selectClienteFiltro');
                        if (window.recargarListaCuentas && selectCliente) {
                            window.recargarListaCuentas(selectCliente.value);
                        }
                    }, 2000);
                } else {
                    // Manejo detallado del error del servidor (Saldo > 0)
                    let textoError = 'No se pudo eliminar la cuenta';
                    try {
                        const errorData = await response.json();
                        textoError = errorData.message || textoError;
                    } catch (e) {
                        if (response.status === 400) textoError = '⚠️ La cuenta debe tener saldo 0.00€ para ser desactivada.';
                    }

                    mensajeError.innerHTML = textoError;
                    mensajeError.style.display = 'block';
                    btnConfirmar.disabled = false;
                    btnConfirmar.textContent = 'Confirmar Desactivación';
                }
            } catch (error) {
                mensajeError.textContent = '❌ Error crítico: No se pudo conectar con el servidor.';
                mensajeError.style.display = 'block';
                btnConfirmar.disabled = false;
                btnConfirmar.textContent = 'Confirmar Desactivación';
            }
        };

        btnCancelar.onclick = () => GestorModales.cerrar('eliminarCuenta');
    }, 100);
}

window.initEliminarCuenta = initEliminarCuenta;