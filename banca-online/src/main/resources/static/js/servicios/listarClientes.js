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
                const response = await fetch('/api/clientes');
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

            // Crear tabla para mejor visualización
            listaClientes.innerHTML = `
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr>
                            <th style="text-align: left; padding: 10px; border-bottom: 2px solid #3498db;">ID</th>
                            <th style="text-align: left; padding: 10px; border-bottom: 2px solid #3498db;">Nombre</th>
                            <th style="text-align: left; padding: 10px; border-bottom: 2px solid #3498db;">Email</th>
                            <th style="text-align: left; padding: 10px; border-bottom: 2px solid #3498db;">DNI</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${clientes.map(cliente => `
                            <tr style="cursor: pointer;" data-id="${cliente.id}" class="fila-cliente">
                                <td style="padding: 10px; border-bottom: 1px solid #ddd;">${cliente.id}</td>
                                <td style="padding: 10px; border-bottom: 1px solid #ddd;">
                                    <strong>${cliente.nombre || ''} ${cliente.primerApellido || ''}</strong>
                                </td>
                                <td style="padding: 10px; border-bottom: 1px solid #ddd;">${cliente.email}</td>
                                <td style="padding: 10px; border-bottom: 1px solid #ddd;">${cliente.dni || 'N/A'}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
                <p style="margin-top: 10px; font-size: 12px; color: #666;">
                    ✅ Total: ${clientes.length} clientes
                </p>
            `;

            // Agregar evento de selección a cada fila
            document.querySelectorAll('.fila-cliente').forEach(fila => {
                fila.addEventListener('click', () => {
                    // Remover selección anterior
                    document.querySelectorAll('.fila-cliente').forEach(f => {
                        f.style.backgroundColor = '';
                    });
                    // Seleccionar nueva fila
                    fila.style.backgroundColor = '#e3f2fd';
                    console.log('Cliente seleccionado ID:', fila.dataset.id);
                });
            });
        }

        // Botones de acción (por ahora solo console.log)
        if (btnCrear) {
            btnCrear.onclick = () => {
                console.log('➡️ Abrir modal crear cliente');
                // Aquí después abriremos el modal de crear cliente
                alert('Próximamente: Crear cliente');
            };
        }

        if (btnEditar) {
            btnEditar.onclick = () => {
                const seleccionado = document.querySelector('.fila-cliente[style*="background-color: rgb(227, 242, 253)"]');
                if (seleccionado) {
                    console.log('➡️ Editar cliente ID:', seleccionado.dataset.id);
                    alert(`Editar cliente ID: ${seleccionado.dataset.id}`);
                } else {
                    alert('Seleccione un cliente primero');
                }
            };
        }

        if (btnEliminar) {
            btnEliminar.onclick = () => {
                const seleccionado = document.querySelector('.fila-cliente[style*="background-color: rgb(227, 242, 253)"]');
                if (seleccionado) {
                    console.log('➡️ Eliminar cliente ID:', seleccionado.dataset.id);
                    alert(`Eliminar cliente ID: ${seleccionado.dataset.id}`);
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

window.initListarClientes = initListarClientes;