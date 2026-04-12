function initTransferenciaForm() {
    console.log('🚀 Inicializando formulario de transferencia...');

    setTimeout(() => {
        // Intentar obtener la cuenta seleccionada desde localStorage
        let cuenta = window.cuentaSeleccionada;

        if (!cuenta) {
            const cuentaGuardada = localStorage.getItem('cuentaSeleccionada');
            if (cuentaGuardada) {
                cuenta = JSON.parse(cuentaGuardada);
                window.cuentaSeleccionada = cuenta;
                console.log('📦 Cuenta recuperada de localStorage:', cuenta.numeroCuenta);
            }
        }

        // Verificar que tenemos una cuenta seleccionada
        if (!cuenta) {
            console.error('❌ No hay cuenta seleccionada');
            const mensajeError = document.getElementById('mensajeErrorTransferenciaForm');
            if (mensajeError) {
                mensajeError.textContent = 'No se ha seleccionado ninguna cuenta';
                mensajeError.style.display = 'block';
            }
            setTimeout(() => cerrarModal(), 2000);
            return;
        }

        // Mostrar la cuenta origen (automática)
        console.log('🏦 Cuenta origen seleccionada:', cuenta.numeroCuenta);

        const cuentaNumeroSpan = document.getElementById('cuentaNumeroOrigen');
        const cuentaSaldoSpan = document.getElementById('cuentaSaldoOrigen');

        if (cuentaNumeroSpan) {
            cuentaNumeroSpan.textContent = cuenta.numeroCuenta;
        }
        if (cuentaSaldoSpan) {
            cuentaSaldoSpan.textContent = cuenta.saldo?.toFixed(2) || '0.00';
        }

        const form = document.getElementById('formTransferenciaForm');
        const btnCerrar = document.getElementById('btnCerrarTransferenciaForm');
        const btnCancelar = document.getElementById('btnCancelarTransferenciaForm');
        const mensajeExito = document.getElementById('mensajeExitoTransferenciaForm');
        const mensajeError = document.getElementById('mensajeErrorTransferenciaForm');

        let timeoutId = null;
        let isRequestCompleted = false;

        function ocultarMensajes() {
            if (mensajeExito) mensajeExito.style.display = 'none';
            if (mensajeError) mensajeError.style.display = 'none';
        }

        function mostrarExito(mensaje, nuevoSaldo) {
            if (mensajeExito) {
                mensajeExito.textContent = mensaje;
                mensajeExito.style.display = 'block';
            }
            const saldoSpan = document.getElementById('cuentaSaldoOrigen');
            if (saldoSpan && nuevoSaldo !== undefined && nuevoSaldo !== null) {
                saldoSpan.textContent = nuevoSaldo.toFixed(2);
            }
            setTimeout(() => {
                if (mensajeExito) mensajeExito.style.display = 'none';
            }, 3000);
        }

        function mostrarError(mensaje) {
            if (mensajeError) {
                mensajeError.textContent = mensaje;
                mensajeError.style.display = 'block';
            }
        }

        function mostrarAlertaTimeout(btnGuardar) {
            if (!isRequestCompleted) {
                alert('⏰ La transacción está tardando más de lo esperado. Por favor, verifica tu conexión y vuelve a intentarlo.');
                if (btnGuardar) {
                    btnGuardar.disabled = false;
                    btnGuardar.textContent = 'Confirmar Transferencia';
                }
            }
        }

        function cerrarModal() {
            if (timeoutId) {
                clearTimeout(timeoutId);
            }
            const modal = document.getElementById('modal-transferenciaForm');
            if (modal) modal.remove();
            window.cuentaSeleccionada = null;
            localStorage.removeItem('cuentaSeleccionada');
            if (window.recargarCuentasCliente) {
                window.recargarCuentasCliente();
            }
        }

        if (btnCerrar) btnCerrar.onclick = cerrarModal;
        if (btnCancelar) btnCancelar.onclick = cerrarModal;

        if (form) {
            form.onsubmit = async (e) => {
                e.preventDefault();
                ocultarMensajes();

                if (!cuenta) {
                    mostrarError('Error: Cuenta origen no encontrada');
                    return;
                }

                const cuentaDestino = document.getElementById('cuentaDestino').value;
                const montoInput = document.getElementById('montoTransferencia');
                const monto = montoInput?.value;

                if (!cuentaDestino || cuentaDestino.trim() === '') {
                    mostrarError('Debe ingresar una cuenta destino.');
                    return;
                }

                if (!monto || parseFloat(monto) <= 0) {
                    mostrarError('El monto debe ser mayor a 0.');
                    return;
                }

                if (cuentaDestino === cuenta.numeroCuenta) {
                    mostrarError('No puede transferir a la misma cuenta.');
                    return;
                }

                if (parseFloat(monto) > cuenta.saldo) {
                    mostrarError(`Saldo insuficiente. Saldo actual: ${cuenta.saldo?.toFixed(2) || '0.00'} €`);
                    return;
                }

                const btnGuardar = form.querySelector('button[type="submit"]');
                if (btnGuardar) {
                    btnGuardar.disabled = true;
                    btnGuardar.textContent = 'Procesando...';
                }

                if (timeoutId) {
                    clearTimeout(timeoutId);
                }
                isRequestCompleted = false;

                timeoutId = setTimeout(() => mostrarAlertaTimeout(btnGuardar), 5000);

                const formData = {
                    cuentaOrigen: cuenta.numeroCuenta,
                    cuentaDestino: cuentaDestino,
                    cantidad: parseFloat(monto)
                };

                console.log('📤 Transferencia:');
                console.log('   Origen Número:', cuenta.numeroCuenta);
                console.log('   Destino Número:', cuentaDestino);
                console.log('   Cantidad:', formData.cantidad);

                try {
                    const response = await fetchConAuth('/api/cuentas/transferir', {
                        method: 'POST',
                        body: JSON.stringify(formData)
                    });

                    isRequestCompleted = true;
                    if (timeoutId) {
                        clearTimeout(timeoutId);
                    }

                    console.log('📥 Response status:', response.status);

                    if (response.ok) {
                        const data = await response.json();
                        console.log('📥 Respuesta del servidor:', data);

                        // Intentar obtener el saldo de diferentes formas
                        let nuevoSaldo = null;
                        if (data.saldo !== undefined) {
                            nuevoSaldo = data.saldo;
                        } else if (data.nuevoSaldoOrigen !== undefined) {
                            nuevoSaldo = data.nuevoSaldoOrigen;
                        }

                        if (nuevoSaldo !== null) {
                            mostrarExito(`✅ Transferencia exitosa! Nuevo saldo: ${nuevoSaldo.toFixed(2)} €`, nuevoSaldo);
                        } else {
                            mostrarExito(`✅ Transferencia exitosa!`, null);
                        }

                        form.reset();
                        setTimeout(() => {
                            cerrarModal();
                        }, 2000);
                    } else {
                        let errorMensaje = 'Error al realizar la transferencia';
                        try {
                            const error = await response.json();
                            errorMensaje = error.message || errorMensaje;
                        } catch (e) {
                            // Si no se puede parsear el error
                        }
                        mostrarError(errorMensaje);
                        if (btnGuardar) {
                            btnGuardar.disabled = false;
                            btnGuardar.textContent = 'Confirmar Transferencia';
                        }
                    }
                } catch (error) {
                    isRequestCompleted = true;
                    if (timeoutId) {
                        clearTimeout(timeoutId);
                    }
                    console.error('Error:', error);
                    mostrarError('Error de conexión con el servidor');
                    if (btnGuardar) {
                        btnGuardar.disabled = false;
                        btnGuardar.textContent = 'Confirmar Transferencia';
                    }
                }
            };
        }

        console.log('✅ transferenciaForm inicializado');
    }, 100);
}

window.initTransferenciaForm = initTransferenciaForm;