function initDetalleCuenta() {
    console.log('🚀 Inicializando detalle de cuenta...');

    setTimeout(() => {
        // Cargar datos de la cuenta seleccionada
        if (window.cuentaSeleccionada) {
            // Asegurar que la cuenta tiene el id numérico
            console.log('📊 Cuenta seleccionada:', window.cuentaSeleccionada);

            document.getElementById('detalleNumeroCuenta').textContent = window.cuentaSeleccionada.numeroCuenta;
            document.getElementById('detalleTipoCuenta').textContent = window.cuentaSeleccionada.tipoCuenta === 'CORRIENTE' ? '💳 Corriente' : '🏦 Ahorro';
            document.getElementById('detalleSaldoCuenta').innerHTML = `<strong>${window.cuentaSeleccionada.saldo?.toFixed(2) || '0.00'} €</strong>`;
            document.getElementById('detalleFechaCreacion').textContent = window.cuentaSeleccionada.fechaCreacion || 'No disponible';


        }

        const btnCerrar = document.getElementById('btnCerrarDetalleCuenta');
        const btnDepositar = document.getElementById('btnDepositarDetalle');
        const btnRetirar = document.getElementById('btnRetirarDetalle');
        const btnTransferir = document.getElementById('btnTransferirDetalle');
        const mensajeExito = document.getElementById('mensajeExitoDetalle');
        const mensajeError = document.getElementById('mensajeErrorDetalle');
        const btnHistorial = document.getElementById('btnHistorialDetalle');

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
            const modal = document.getElementById('modal-detalleCuenta');
            if (modal) modal.remove();
            window.cuentaSeleccionada = null;
        }

        if (btnCerrar) btnCerrar.onclick = cerrarModal;

        // Abrir modal de depósito - Guardar la cuenta completa (con id)
        if (btnDepositar) {
            btnDepositar.onclick = () => {
                // Guardar la cuenta completa en localStorage
                const cuentaParaGuardar = {
                    id: window.cuentaSeleccionada.id,
                    numeroCuenta: window.cuentaSeleccionada.numeroCuenta,
                    saldo: window.cuentaSeleccionada.saldo,
                    tipoCuenta: window.cuentaSeleccionada.tipoCuenta
                };
                localStorage.setItem('cuentaSeleccionada', JSON.stringify(cuentaParaGuardar));
                console.log('💾 Cuenta guardada para depósito:', cuentaParaGuardar);
                cerrarModal();
                GestorModales.abrir('depositoForm', 'depositoForm.html');
            };
        }

        // Abrir modal de retiro
        if (btnRetirar) {
            btnRetirar.onclick = () => {
                const cuentaParaGuardar = {
                    id: window.cuentaSeleccionada.id,
                    numeroCuenta: window.cuentaSeleccionada.numeroCuenta,
                    saldo: window.cuentaSeleccionada.saldo,
                    tipoCuenta: window.cuentaSeleccionada.tipoCuenta
                };
                localStorage.setItem('cuentaSeleccionada', JSON.stringify(cuentaParaGuardar));
                console.log('💾 Cuenta guardada para retiro:', cuentaParaGuardar);
                cerrarModal();
                GestorModales.abrir('retiroForm', 'retiroForm.html');
            };
        }

        // Abrir modal de transferencia
        if (btnTransferir) {
            btnTransferir.onclick = () => {
                const cuentaParaGuardar = {
                    id: window.cuentaSeleccionada.id,
                    numeroCuenta: window.cuentaSeleccionada.numeroCuenta,
                    saldo: window.cuentaSeleccionada.saldo,
                    tipoCuenta: window.cuentaSeleccionada.tipoCuenta
                };
                localStorage.setItem('cuentaSeleccionada', JSON.stringify(cuentaParaGuardar));
                console.log('💾 Cuenta guardada para transferencia:', cuentaParaGuardar);
                cerrarModal();
                GestorModales.abrir('transferenciaForm', 'transferenciaForm.html');
            };
        }

        if (btnHistorial) {
            btnHistorial.onclick = () => {
                // Guardamos la cuenta en localStorage por si acaso, igual que los otros
                const cuentaParaGuardar = {
                    id: window.cuentaSeleccionada.id,
                    numeroCuenta: window.cuentaSeleccionada.numeroCuenta,
                    saldo: window.cuentaSeleccionada.saldo,
                    tipoCuenta: window.cuentaSeleccionada.tipoCuenta
                };
                localStorage.setItem('cuentaSeleccionada', JSON.stringify(cuentaParaGuardar));

                console.log('📜 Abriendo historial para la cuenta:', cuentaParaGuardar.numeroCuenta);

                // Cerramos el detalle y abrimos el historial
                cerrarModal();
                GestorModales.abrir('historialTransacciones', 'historialTransacciones.html');
            };
        }

        console.log('✅ detalleCuenta inicializado');
    }, 100);
}

window.initDetalleCuenta = initDetalleCuenta;