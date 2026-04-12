function initEliminarClienteForm() {
    console.log('🚀 Inicializando formulario eliminar cliente...');

    setTimeout(() => {
        const btnConfirmar = document.getElementById('btnConfirmarEliminar');
        const btnCerrar = document.getElementById('btnCerrarEliminarCliente');
        const btnCancelar = document.getElementById('btnCancelarEliminar');
        const mensajeExito = document.getElementById('mensajeExitoEliminar');
        const mensajeError = document.getElementById('mensajeErrorEliminar');
        const spanId = document.getElementById('eliminarId');
        const spanNombre = document.getElementById('eliminarNombre');
        const spanEmail = document.getElementById('eliminarEmail');

        let clienteId = null;

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
            const modal = document.getElementById('modal-eliminarClienteForm');
            if (modal) modal.remove();
            window.clienteIdParaEliminar = null;
        }

        function actualizarListaClientes() {
            if (window.recargarListaClientes) {
                window.recargarListaClientes();
            }
        }

        // Cargar datos del cliente a eliminar
        async function cargarDatosCliente(id) {
            try {
                const response = await fetchConAuth(`/api/clientes/${id}`);
                if (response.ok) {
                    const cliente = await response.json();
                    clienteId = cliente.id;
                    if (spanId) spanId.textContent = cliente.id;
                    if (spanNombre) spanNombre.textContent = `${cliente.nombre || ''} ${cliente.primerApellido || ''}`.trim() || 'Sin nombre';
                    if (spanEmail) spanEmail.textContent = cliente.email || 'Sin email';
                } else {
                    mostrarError('Error al cargar los datos del cliente');
                    setTimeout(() => cerrarModal(), 2000);
                }
            } catch (error) {
                console.error('Error:', error);
                mostrarError('Error de conexión con el servidor');
                setTimeout(() => cerrarModal(), 2000);
            }
        }

        // Eliminar cliente
        async function eliminarCliente() {
            if (!clienteId) return;

            ocultarMensajes();
            if (btnConfirmar) {
                btnConfirmar.disabled = true;
                btnConfirmar.textContent = 'Eliminando...';
            }

            try {
                const response = await fetchConAuth(`/api/clientes/${clienteId}`, {
                    method: 'DELETE'
                });

                if (response.status === 204) {
                    mostrarExito('✅ Cliente eliminado con éxito');
                    setTimeout(() => {
                        cerrarModal();
                        actualizarListaClientes();
                    }, 2000);
                } else if (response.status === 404) {
                    mostrarError('Cliente no encontrado');
                } else {
                    mostrarError('Error al eliminar el cliente');
                }
            } catch (error) {
                console.error('Error:', error);
                mostrarError('Error de conexión con el servidor');
            } finally {
                if (btnConfirmar) {
                    btnConfirmar.disabled = false;
                    btnConfirmar.textContent = '🗑 Eliminar';
                }
            }
        }

        // Botones
        if (btnCerrar) btnCerrar.onclick = cerrarModal;
        if (btnCancelar) btnCancelar.onclick = cerrarModal;
        if (btnConfirmar) btnConfirmar.onclick = eliminarCliente;

        // Cargar datos del cliente preseleccionado
        if (window.clienteIdParaEliminar) {
            cargarDatosCliente(window.clienteIdParaEliminar);
        } else {
            mostrarError('No se ha seleccionado ningún cliente');
            setTimeout(() => cerrarModal(), 2000);
        }

        console.log('✅ eliminarClienteForm inicializado');
    }, 100);
}

window.initEliminarClienteForm = initEliminarClienteForm;