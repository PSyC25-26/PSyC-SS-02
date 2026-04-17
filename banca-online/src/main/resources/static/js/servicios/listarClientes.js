function initListarClientes() {
    console.log('🚀 Inicializando listarClientes...');

    // Esperar a que el DOM esté listo
    setTimeout(() => {
        const listaClientes = document.getElementById('listaClientes');
        const btnCrear = document.getElementById('btnCrearCliente');
        const btnEditar = document.getElementById('btnEditarCliente');
        const btnEliminar = document.getElementById('btnEliminarCliente');

        // Función para cargar y mostrar clientes
        async function cargarClientes() {
            if (!listaClientes) return;

            listaClientes.innerHTML = '<p>Cargando clientes...</p>';

            try {
                const response = await fetchConAuth('/api/clientes');
                if (response.ok) {
                    const clientes = await response.json();
                    mostrarClientes(clientes);
                } else {
                    listaClientes.innerHTML = '<p class="error">Error al cargar los clientes</p>';
                }
            } catch (error) {
                console.error('Error:', error);
                listaClientes.innerHTML = '<p class="error">Error de conexión con el servidor</p>';
            }
        }

        function mostrarClientes(clientes) {
            if (!listaClientes) return;

            if (clientes.length === 0) {
                listaClientes.innerHTML = '<p>No hay clientes registrados</p>';
                return;
            }

            // Ahora usa clases CSS en lugar de estilos inline
            listaClientes.innerHTML = `
        <table class="tabla-clientes">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Nombre</th>
                    <th>Email</th>
                    <th>DNI</th>
                </tr>
            </thead>
            <tbody>
                ${clientes.map(cliente => `
                    <tr class="fila-cliente" data-id="${cliente.id}">
                        <td>${cliente.id}</td>
                        <td><strong>${cliente.nombre || ''} ${cliente.primerApellido || ''}</strong></td>
                        <td class="cliente-email">${cliente.email}</td>
                        <td>${cliente.dni || 'N/A'}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
        <p class="total-clientes">✅ Total: ${clientes.length} clientes</p>
    `;

            // Agregar evento de selección
            document.querySelectorAll('.fila-cliente').forEach(fila => {
                fila.addEventListener('click', () => {
                    document.querySelectorAll('.fila-cliente').forEach(f => {
                        f.classList.remove('seleccionado');
                    });
                    fila.classList.add('seleccionado');
                    console.log('Cliente seleccionado ID:', fila.dataset.id);
                });
            });
        }

        // Botones de acción (por ahora solo console.log)
        if (btnCrear) {
            btnCrear.onclick = () => {
                console.log('➡️ Abriendo modal crear cliente');
                // Aquí después abriremos el modal de crear cliente
                GestorModales.abrir('crearClienteForm', 'crearClienteForm.html');
            };
        }

        if (btnEditar) {
            btnEditar.onclick = () => {
                const seleccionado = document.querySelector('.fila-cliente.seleccionado');
                if (seleccionado) {
                    const id = seleccionado.dataset.id;
                    console.log('➡️ Editar cliente ID:', id);
                    // Guardar el ID seleccionado para pasarlo al modal
                    window.clienteIdParaEditar = id;
                    GestorModales.abrir('editarClienteForm', 'editarClienteForm.html');
                } else {
                    alert('Seleccione un cliente primero');
                }
            };
        }

        if (btnEliminar) {
            btnEliminar.onclick = () => {
                const seleccionado = document.querySelector('.fila-cliente.seleccionado');
                if (seleccionado) {
                    const id = seleccionado.dataset.id;
                    const nombre = seleccionado.querySelector('td:nth-child(2)')?.innerText || 'Cliente';
                    console.log('➡️ Eliminar cliente ID:', id);

                    // Guardar el ID para que lo coja el modal
                    window.clienteIdParaEliminar = id;
                    GestorModales.abrir('eliminarClienteForm', 'eliminarClienteForm.html');
                } else {
                    alert('Seleccione un cliente primero');
                }
            };
        }

        // Cargar clientes
        cargarClientes();

        console.log('✅ listarClientes inicializado correctamente');
    }, 100);
}


// Función global para recargar la lista de clientes
window.mostrarClientes = mostrarClientes;  // ← ESTA LÍNEA ES LA CLAVE
window.recargarListaClientes = async function() {
    const listaClientes = document.getElementById('listaClientes');
    if (!listaClientes) return;

    listaClientes.innerHTML = '<p>Cargando clientes...</p>';

    try {
        const response = await fetchConAuth('/api/clientes');
        if (response.ok) {
            const clientes = await response.json();
            // Usar la función mostrarClientes que ya tienes
            if (typeof mostrarClientes === 'function') {
                mostrarClientes(clientes);
            } else {
                // Si no hay función mostrarClientes, recargar la página
                location.reload();
            }
        } else {
            listaClientes.innerHTML = '<p class="error">Error al cargar los clientes</p>';
        }
    } catch (error) {
        console.error('Error:', error);
        listaClientes.innerHTML = '<p class="error">Error de conexión con el servidor</p>';
    }
};

window.initListarClientes = initListarClientes;
