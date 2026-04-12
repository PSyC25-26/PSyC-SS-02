function initCrearCuentaForm() {
    console.log('🚀 Inicializando formulario crear cuenta...');

    setTimeout(() => {
        const form = document.getElementById('formCrearCuenta');
        const btnCerrar = document.getElementById('btnCerrarCrearCuenta');
        const btnCancelar = document.getElementById('btnCancelarCrearCuenta');
        const mensajeExito = document.getElementById('mensajeExitoCuenta');
        const mensajeError = document.getElementById('mensajeErrorCuenta');

        function ocultarMensajes() {
            if (mensajeExito) mensajeExito.style.display = 'none';
            if (mensajeError) mensajeError.style.display = 'none';
        }

        function mostrarExito(mensaje) {
            if (mensajeExito) {
                mensajeExito.textContent = mensaje;
                mensajeExito.style.display = 'block';
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

        function cerrarModal() {
            const modal = document.getElementById('modal-crearCuentaForm');
            if (modal) modal.remove();
            window.clienteIdParaCuenta = null;
        }

        function recargarListaCuentas() {
            if (window.recargarListaCuentas) {
                const input = document.getElementById('clienteId');
                const clienteId = input ? input.value : null;
                window.recargarListaCuentas(clienteId);
            }
        }

        // Validar formulario
        function validarFormulario() {
            const clienteId = document.getElementById('clienteId').value;
            const tipoCuenta = document.getElementById('tipoCuenta').value;
            const saldo = document.getElementById('saldoInicial').value;

            if (!clienteId || parseInt(clienteId) <= 0) {
                mostrarError('El ID del cliente debe ser un número positivo.');
                return false;
            }
            if (!tipoCuenta) {
                mostrarError('Debe seleccionar un tipo de cuenta.');
                return false;
            }
            if (saldo !== '' && parseFloat(saldo) < 0) {
                mostrarError('El saldo inicial no puede ser negativo.');
                return false;
            }
            return true;
        }

        // Botones de cierre
        if (btnCerrar) btnCerrar.onclick = cerrarModal;
        if (btnCancelar) btnCancelar.onclick = cerrarModal;

        // Prellenar clienteId si viene seleccionado
        if (window.clienteIdParaCuenta) {
            const inputClienteId = document.getElementById('clienteId');
            if (inputClienteId) {
                inputClienteId.value = window.clienteIdParaCuenta;
                inputClienteId.readOnly = true;
            }
        }

        // Enviar formulario
        if (form) {
            form.onsubmit = async (e) => {
                e.preventDefault();
                ocultarMensajes();

                if (!validarFormulario()) return;

                const btnGuardar = form.querySelector('button[type="submit"]');
                if (btnGuardar) {
                    btnGuardar.disabled = true;
                    btnGuardar.textContent = 'Guardando...';
                }

                const formData = {
                    clienteId: parseInt(document.getElementById('clienteId').value),
                    tipoCuenta: document.getElementById('tipoCuenta').value,
                    saldoInicial: parseFloat(document.getElementById('saldoInicial').value) || 0.0
                };

                try {
                    const response = await fetchConAuth('/api/cuentas', {
                        method: 'POST',
                        body: JSON.stringify(formData)
                    });

                    if (response.ok) {
                        const data = await response.json();
                        mostrarExito(`✅ Cuenta creada con éxito! Número: ${data.numeroCuenta}`);
                        form.reset();
                        setTimeout(() => {
                            cerrarModal();
                            recargarListaCuentas();
                        }, 2000);
                    } else {
                        const error = await response.json();
                        mostrarError(error.message || 'Error al crear la cuenta');
                    }
                } catch (error) {
                    console.error('Error:', error);
                    mostrarError('Error de conexión con el servidor');
                } finally {
                    if (btnGuardar) {
                        btnGuardar.disabled = false;
                        btnGuardar.textContent = 'Guardar Cuenta';
                    }
                }
            };
        }

        console.log('✅ crearCuentaForm inicializado');
    }, 100);
}

window.initCrearCuentaForm = initCrearCuentaForm;