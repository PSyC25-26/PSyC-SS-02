function initCrearClienteForm() {
    console.log('🚀 Inicializando formulario crear cliente...');

    setTimeout(() => {
        const form = document.getElementById('formCliente');
        const btnCerrar = document.getElementById('btnCerrarCrearCliente');
        const btnCancelar = document.getElementById('btnCancelarCrearCliente');
        const mensajeExito = document.getElementById('mensajeExito');
        const mensajeError = document.getElementById('mensajeError');

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

        // Cerrar modal
        function cerrarModal() {
            const modal = document.getElementById('modal-crearClienteForm');
            if (modal) modal.remove();
        }

        // Función para actualizar la lista de clientes sin recargar
        function actualizarListaClientes() {
            if (window.recargarListaClientes) {
                window.recargarListaClientes();
            }
        }

        if (btnCerrar) btnCerrar.onclick = cerrarModal;
        if (btnCancelar) btnCancelar.onclick = cerrarModal;

        // Enviar formulario
        if (form) {
            form.onsubmit = async (e) => {
                e.preventDefault();
                ocultarMensajes();

                const btnGuardar = form.querySelector('button[type="submit"]');
                if (btnGuardar) {
                    btnGuardar.disabled = true;
                    btnGuardar.textContent = 'Guardando...';
                }

                const formData = {
                    dni: document.getElementById('dni').value,
                    nombre: document.getElementById('nombre').value,
                    primerApellido: document.getElementById('primerApellido').value,
                    segundoApellido: document.getElementById('segundoApellido').value,
                    fechaNacimiento: document.getElementById('fechaNacimiento').value,
                    email: document.getElementById('email').value,
                    password: document.getElementById('password').value,
                    telefono: document.getElementById('telefono').value,
                    direccion: document.getElementById('direccion').value
                };

                try {
                    const response = await fetchConAuth('/api/clientes', {
                        method: 'POST',
                        body: JSON.stringify(formData)
                    });

                    if (response.ok) {
                        mostrarExito('✅ Cliente creado con éxito');
                        form.reset();
                        setTimeout(() => {
                            cerrarModal();
                            // Actualizar la lista sin recargar
                            actualizarListaClientes();
                        }, 2000);
                    } else {
                        const error = await response.json();
                        mostrarError(error.message || 'Error al crear el cliente');
                    }
                } catch (error) {
                    mostrarError('Error de conexión con el servidor');
                    console.error('Error:', error);
                } finally {
                    if (btnGuardar) {
                        btnGuardar.disabled = false;
                        btnGuardar.textContent = 'Guardar Cliente';
                    }
                }
            };
        }

        console.log('✅ crearClienteForm inicializado');
    }, 100);
}

window.initCrearClienteForm = initCrearClienteForm;