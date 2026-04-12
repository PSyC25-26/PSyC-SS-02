function initRetiroForm() {
    console.log('🚀 Inicializando formulario de retiro...');

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
            const mensajeError = document.getElementById('mensajeErrorRetiroForm');
            if (mensajeError) {
                mensajeError.textContent = 'No se ha seleccionado ninguna cuenta';
                mensajeError.style.display = 'block';
            }
            setTimeout(() => cerrarModal(), 2000);
            return;
        }

        // Cargar datos de la cuenta
        const cuentaNumeroSpan = document.getElementById('cuentaNumeroRetiro');
        const cuentaSaldoSpan = document.getElementById('cuentaSaldoRetiro');

        if (cuentaNumeroSpan) {
            cuentaNumeroSpan.textContent = cuenta.numeroCuenta;
        }
        if (cuentaSaldoSpan) {
            cuentaSaldoSpan.textContent = cuenta.saldo?.toFixed(2) || '0.00';
        }

        const form = document.getElementById('formRetiroForm');
        const btnCerrar = document.getElementById('btnCerrarRetiroForm');
        const btnCancelar = document.getElementById('btnCancelarRetiroForm');
        const mensajeExito = document.getElementById('mensajeExitoRetiroForm');
        const mensajeError = document.getElementById('mensajeErrorRetiroForm');

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
            const saldoSpan = document.getElementById('cuentaSaldoRetiro');
            if (saldoSpan && nuevoSaldo !== undefined) {
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
                    btnGuardar.textContent = 'Confirmar Retiro';
                }
            }
        }

        function cerrarModal() {
            if (timeoutId) {
                clearTimeout(timeoutId);
            }
            const modal = document.getElementById('modal-retiroForm');
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
                    mostrarError('Error: Cuenta no encontrada');
                    return;
                }

                const montoInput = document.getElementById('montoRetiro');
                const monto = montoInput?.value;

                if (!monto || parseFloat(monto) <= 0) {
                    mostrarError('El monto debe ser mayor a 0.');
                    return;
                }

                // Verificar saldo suficiente
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
                    cuentaId: cuenta.id,        // Usar el ID numérico
                    monto: parseFloat(monto)
                };

                console.log('📤 Retirando de cuenta ID:', cuenta.id);
                console.log('📤 Monto:', formData.monto);

                try {
                    const response = await fetchConAuth('/api/cuentas/retirar', {
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
                        mostrarExito(`✅ Retiro exitoso! Nuevo saldo: ${data.saldo.toFixed(2)} €`, data.saldo);
                        form.reset();
                        setTimeout(() => {
                            cerrarModal();
                        }, 2000);
                    } else {
                        const error = await response.json();
                        mostrarError(error.message || 'Error al realizar el retiro');
                        if (btnGuardar) {
                            btnGuardar.disabled = false;
                            btnGuardar.textContent = 'Confirmar Retiro';
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
                        btnGuardar.textContent = 'Confirmar Retiro';
                    }
                }
            };
        }

        console.log('✅ retiroForm inicializado');
    }, 100);
}

window.initRetiroForm = initRetiroForm;