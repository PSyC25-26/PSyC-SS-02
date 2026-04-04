function initEditarClienteForm() {
    console.log('🚀 Inicializando formulario editar cliente...');

    setTimeout(() => {
        const form = document.getElementById('formEditarCliente');
        const btnCerrar = document.getElementById('btnCerrarEditarCliente');
        const btnCancelar = document.getElementById('btnCancelarEditar');
        const mensajeExito = document.getElementById('mensajeExitoEditar');
        const mensajeError = document.getElementById('mensajeErrorEditar');

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
            const modal = document.getElementById('modal-editarClienteForm');
            if (modal) modal.remove();
            // Limpiar el ID guardado
            window.clienteIdParaEditar = null;
        }

        function actualizarListaClientes() {
            if (window.recargarListaClientes) {
                window.recargarListaClientes();
            }
        }

        // Cargar datos del cliente seleccionado
        async function cargarDatosCliente(id) {
            try {
                const response = await fetch(`/api/clientes/${id}`);
                if (response.ok) {
                    const cliente = await response.json();

                    document.getElementById('editId').value = cliente.id;
                    document.getElementById('editNombre').value = cliente.nombre || '';
                    document.getElementById('editPrimerApellido').value = cliente.primerApellido || '';
                    document.getElementById('editSegundoApellido').value = cliente.segundoApellido || '';
                    document.getElementById('editDni').value = cliente.dni || '';
                    document.getElementById('editEmail').value = cliente.email || '';
                    document.getElementById('editTelefono').value = cliente.telefono || '';
                    document.getElementById('editDireccion').value = cliente.direccion || '';

                    const fn = cliente.fechaNacimiento;
                    document.getElementById('editFechaNacimiento').value = Array.isArray(fn)
                        ? `${fn[0]}-${String(fn[1]).padStart(2, '0')}-${String(fn[2]).padStart(2, '0')}`
                        : fn || '';

                    ocultarMensajes();
                    console.log('✅ Datos cargados para cliente ID:', id);
                } else {
                    mostrarError('Error al cargar los datos del cliente');
                }
            } catch (error) {
                console.error('Error:', error);
                mostrarError('Error de conexión con el servidor');
            }
        }

        // Botones de cierre
        if (btnCerrar) btnCerrar.onclick = cerrarModal;
        if (btnCancelar) btnCancelar.onclick = cerrarModal;

        // Cargar datos del cliente preseleccionado
        if (window.clienteIdParaEditar) {
            cargarDatosCliente(window.clienteIdParaEditar);
        } else {
            mostrarError('No se ha seleccionado ningún cliente');
            setTimeout(() => cerrarModal(), 2000);
        }

        // Enviar formulario de edición
        if (form) {
            form.onsubmit = async (e) => {
                e.preventDefault();
                ocultarMensajes();

                const btnGuardar = form.querySelector('button[type="submit"]');
                if (btnGuardar) {
                    btnGuardar.disabled = true;
                    btnGuardar.textContent = 'Guardando...';
                }

                const id = document.getElementById('editId').value;
                const datos = {
                    dni: document.getElementById('editDni').value,
                    nombre: document.getElementById('editNombre').value,
                    primerApellido: document.getElementById('editPrimerApellido').value,
                    segundoApellido: document.getElementById('editSegundoApellido').value,
                    fechaNacimiento: document.getElementById('editFechaNacimiento').value,
                    email: document.getElementById('editEmail').value,
                    telefono: document.getElementById('editTelefono').value,
                    direccion: document.getElementById('editDireccion').value
                };

                try {
                    const response = await fetch(`/api/clientes/${id}`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(datos)
                    });

                    if (response.ok) {
                        mostrarExito('✅ Cliente actualizado con éxito');
                        setTimeout(() => {
                            cerrarModal();
                            actualizarListaClientes();
                        }, 2000);
                    } else {
                        const error = await response.json();
                        mostrarError(error.message || 'Error al actualizar el cliente');
                    }
                } catch (error) {
                    console.error('Error:', error);
                    mostrarError('Error de conexión con el servidor');
                } finally {
                    if (btnGuardar) {
                        btnGuardar.disabled = false;
                        btnGuardar.textContent = 'Guardar Cambios';
                    }
                }
            };
        }

        console.log('✅ editarClienteForm inicializado');
    }, 100);
}

window.initEditarClienteForm = initEditarClienteForm;