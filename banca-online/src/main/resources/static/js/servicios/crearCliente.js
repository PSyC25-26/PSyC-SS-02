function initCrearCliente() {
    console.log('🚀 Inicializando crearCliente...');

    setTimeout(() => {
        const btnAbrir = document.getElementById('btnAbrir');
        const formulario = document.getElementById('miForm');
        const btnCerrar = document.getElementById('btnCerrar');
        const btnCancelar = document.getElementById('btnCancelar');
        const form = document.getElementById('formCliente');
        const mensajeExito = document.getElementById('mensajeExito');
        const mensajeError = document.getElementById('mensajeError');
        const listaClientes = document.getElementById('listaClientes');

        // ABRIR formulario
        if (btnAbrir && formulario) {
            const nuevoBtn = btnAbrir.cloneNode(true);
            btnAbrir.parentNode.replaceChild(nuevoBtn, btnAbrir);

            nuevoBtn.onclick = (e) => {
                e.preventDefault();
                console.log('🖱️ Abriendo formulario');

                // Aplicar todos los estilos necesarios
                formulario.style.cssText = `
                    display: flex !important;
                    position: fixed !important;
                    top: 0 !important;
                    left: 0 !important;
                    width: 100% !important;
                    height: 100% !important;
                    background-color: rgba(0,0,0,0.5) !important;
                    z-index: 10000 !important;
                    align-items: center !important;
                    justify-content: center !important;
                `;

                console.log('✅ Estilos aplicados, display:', formulario.style.display);

                if (form) form.reset();
                ocultarMensajes();
            };
            console.log('✅ Botón Nuevo Cliente configurado');
        }

        // CERRAR formulario
        const cerrarForm = () => {
            if (formulario) formulario.style.display = 'none';
            ocultarMensajes();
        };

        if (btnCerrar) btnCerrar.onclick = cerrarForm;
        if (btnCancelar) btnCancelar.onclick = cerrarForm;

        window.addEventListener('click', (event) => {
            if (event.target === formulario) cerrarForm();
        });

        function mostrarExito(mensaje) {
            if (mensajeExito) {
                mensajeExito.textContent = mensaje;
                mensajeExito.style.display = 'block';
                if (mensajeError) mensajeError.style.display = 'none';
            }
            setTimeout(() => {
                if (mensajeExito) mensajeExito.style.display = 'none';
            }, 3000);
        }

        function mostrarError(mensaje) {
            if (mensajeError) {
                mensajeError.textContent = mensaje;
                mensajeError.style.display = 'block';
                if (mensajeExito) mensajeExito.style.display = 'none';
            }
        }

        function ocultarMensajes() {
            if (mensajeExito) mensajeExito.style.display = 'none';
            if (mensajeError) mensajeError.style.display = 'none';
        }

        // Enviar formulario
        if (form) {
            form.onsubmit = async (event) => {
                event.preventDefault();
                const btnGuardar = document.getElementById('btnGuardar');
                if (btnGuardar) {
                    btnGuardar.disabled = true;
                    btnGuardar.textContent = 'Guardando...';
                }
                ocultarMensajes();

                const formData = {
                    dni: document.getElementById('dni').value,
                    nombre: document.getElementById('nombre').value,
                    primerApellido: document.getElementById('primerApellido').value,
                    segundoApellido: document.getElementById('segundoApellido').value,
                    fechaNacimiento: document.getElementById('fechaNacimiento').value,
                    email: document.getElementById('email').value,
                    telefono: document.getElementById('telefono').value,
                    direccion: document.getElementById('direccion').value
                };

                try {
                    const response = await fetch('/api/clientes', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(formData)
                    });

                    if (response.ok) {
                        mostrarExito('✅ Cliente creado con éxito');
                        await cargarClientes();
                        setTimeout(() => cerrarForm(), 2000);
                    } else {
                        const error = await response.json();
                        mostrarError(error.message || 'Error al crear el cliente');
                    }
                } catch (error) {
                    mostrarError('Error de conexión con el servidor');
                } finally {
                    if (btnGuardar) {
                        btnGuardar.disabled = false;
                        btnGuardar.textContent = 'Guardar Cliente';
                    }
                }
            };
        }

        async function cargarClientes() {
            if (!listaClientes) return;
            try {
                const response = await fetch('/api/clientes');
                if (response.ok) {
                    const clientes = await response.json();
                    if (clientes.length === 0) {
                        listaClientes.innerHTML = '<p>No hay clientes registrados</p>';
                    } else {
                        listaClientes.innerHTML = clientes.map(cliente => `
                            <div class="cliente-card">
                                <p class="cliente-nombre">${cliente.nombre || ''} ${cliente.primerApellido || ''}</p>
                                <p class="cliente-email">${cliente.email}</p>
                                <p><small>ID: ${cliente.id}</small></p>
                                <p><small>DNI: ${cliente.dni || 'No especificado'}</small></p>
                                <button class="btn-editar" onclick="window.abrirModalEditar(${cliente.id})">Editar / Eliminar</button>
                            </div>
                        `).join('');
                    }
                }
            } catch (error) {
                console.error('Error:', error);
                listaClientes.innerHTML = '<p class="error">Error al cargar los clientes</p>';
            }
        }

        window.abrirModalEditar = async function(id) {
            try {
                const response = await fetch(`/api/clientes/${id}`);
                if (!response.ok) throw new Error('No encontrado');
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

                document.getElementById('modalEditarEliminar').style.display = 'block';
            } catch (error) {
                alert('Error al cargar el cliente');
            }
        };

        cargarClientes();
        console.log('✅ crearCliente inicializado correctamente');
    }, 100);
}

window.initCrearCliente = initCrearCliente;