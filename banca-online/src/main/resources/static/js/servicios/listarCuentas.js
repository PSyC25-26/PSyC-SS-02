function initListarCuentas() {
    console.log('🚀 Inicializando listarCuentas...');

    setTimeout(() => {
        const selectCliente = document.getElementById('selectClienteFiltro');
        const listaCuentas = document.getElementById('listaCuentas');
        const btnCrear = document.getElementById('btnCrearCuenta');
        const btnVerCuentas = document.getElementById('btnVerCuentasCliente');

        let clientes = [];

        async function cargarClientesSelect() {
            if (!selectCliente) return;
            selectCliente.innerHTML = '<option value="">Cargando clientes...</option>';

            try {
                const response = await fetch('/api/clientes');
                if (response.ok) {
                    clientes = await response.json();
                    selectCliente.innerHTML = '<option value="">-- Seleccione un cliente --</option>';
                    clientes.forEach(cliente => {
                        const option = document.createElement('option');
                        option.value = cliente.id;
                        option.textContent = `${cliente.id} - ${cliente.nombre} ${cliente.primerApellido || ''} (${cliente.email})`;
                        selectCliente.appendChild(option);
                    });
                } else {
                    selectCliente.innerHTML = '<option value="">Error al cargar clientes</option>';
                }
            } catch (error) {
                console.error('Error:', error);
                selectCliente.innerHTML = '<option value="">Error de conexión</option>';
            }
        }

        async function cargarCuentas(clienteId) {
            if (!listaCuentas) return;
            if (!clienteId) {
                listaCuentas.innerHTML = '<p>📋 Seleccione un cliente para ver sus cuentas</p>';
                return;
            }

            listaCuentas.innerHTML = '<p>🔄 Cargando cuentas...</p>';

            try {
                const response = await fetch(`/api/cuentas?clienteId=${clienteId}`);

                // Manejar diferentes códigos de respuesta
                if (response.status === 500) {
                    listaCuentas.innerHTML = `
                        <div style="text-align: center; padding: 40px;">
                            <p>📭 <strong>Este cliente no tiene cuentas registradas</strong></p>
                            <p>Puede crear una nueva cuenta usando el botón "➕ Nueva Cuenta"</p>
                        </div>
                    `;
                    return;
                }

                if (response.status === 404) {
                    listaCuentas.innerHTML = `
                        <div style="text-align: center; padding: 40px;">
                            <p>📭 <strong>No se encontraron cuentas para este cliente</strong></p>
                            <p>Puede crear una nueva cuenta usando el botón "➕ Nueva Cuenta"</p>
                        </div>
                    `;
                    return;
                }

                if (response.ok) {
                    const cuentas = await response.json();
                    if (cuentas.length === 0) {
                        listaCuentas.innerHTML = `
                            <div style="text-align: center; padding: 40px;">
                                <p>📭 <strong>Este cliente no tiene cuentas registradas</strong></p>
                                <p>Puede crear una nueva cuenta usando el botón "➕ Nueva Cuenta"</p>
                            </div>
                        `;
                    } else {
                        mostrarCuentas(cuentas);
                    }
                } else {
                    listaCuentas.innerHTML = `<p class="error">❌ Error ${response.status}: No se pudieron cargar las cuentas</p>`;
                }
            } catch (error) {
                console.error('Error:', error);
                listaCuentas.innerHTML = '<p class="error">❌ Error de conexión con el servidor</p>';
            }
        }

        function mostrarCuentas(cuentas) {
            if (!listaCuentas) return;

            listaCuentas.innerHTML = `
                <table class="tabla-cuentas">
                    <thead>
                        <tr>
                            <th>Número de Cuenta</th>
                            <th>Tipo</th>
                            <th>Saldo</th>
                            <th>Fecha Creación</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${cuentas.map(cuenta => `
                            <tr>
                                <td><strong>${cuenta.numeroCuenta}</strong></td>
                                <td>${cuenta.tipoCuenta}</td>
                                <td class="saldo-cuenta">${cuenta.saldo?.toFixed(2) || '0.00'} €</td>
                                <td>${cuenta.fechaCreacion || '-'}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
                <p class="total-cuentas">✅ Total: ${cuentas.length} cuentas</p>
            `;
        }

        // Evento: seleccionar cliente
        if (selectCliente) {
            selectCliente.onchange = () => {
                const clienteId = selectCliente.value;
                if (clienteId) {
                    cargarCuentas(clienteId);
                } else {
                    listaCuentas.innerHTML = '<p>📋 Seleccione un cliente para ver sus cuentas</p>';
                }
            };
        }

        // Botón: crear cuenta
        if (btnCrear) {
            btnCrear.onclick = () => {
                const clienteSeleccionado = selectCliente?.value;
                if (!clienteSeleccionado) {
                    alert('Primero seleccione un cliente');
                    return;
                }
                window.clienteIdParaCuenta = clienteSeleccionado;
                GestorModales.abrir('crearCuentaForm', 'crearCuentaForm.html');
            };
        }

        // Botón: ver cuentas
        if (btnVerCuentas) {
            btnVerCuentas.onclick = () => {
                if (selectCliente?.value) {
                    cargarCuentas(selectCliente.value);
                } else {
                    alert('Seleccione un cliente primero');
                }
            };
        }

        // Cargar clientes al abrir
        cargarClientesSelect();

        console.log('✅ listarCuentas inicializado');
    }, 100);
}

// Función global para recargar cuentas
window.recargarListaCuentas = async function(clienteId) {
    const selectCliente = document.getElementById('selectClienteFiltro');
    const listaCuentas = document.getElementById('listaCuentas');

    if (!selectCliente || !listaCuentas) return;

    const id = clienteId || selectCliente.value;
    if (id) {
        listaCuentas.innerHTML = '<p>🔄 Actualizando...</p>';
        try {
            const response = await fetch(`/api/cuentas?clienteId=${id}`);

            if (response.status === 500 || response.status === 404) {
                listaCuentas.innerHTML = `
                    <div style="text-align: center; padding: 40px;">
                        <p>📭 <strong>Este cliente no tiene cuentas registradas</strong></p>
                        <p>Puede crear una nueva cuenta usando el botón "➕ Nueva Cuenta"</p>
                    </div>
                `;
                return;
            }

            if (response.ok) {
                const cuentas = await response.json();
                if (cuentas.length === 0) {
                    listaCuentas.innerHTML = `
                        <div style="text-align: center; padding: 40px;">
                            <p>📭 <strong>Este cliente no tiene cuentas registradas</strong></p>
                            <p>Puede crear una nueva cuenta usando el botón "➕ Nueva Cuenta"</p>
                        </div>
                    `;
                } else {
                    if (typeof mostrarCuentas === 'function') {
                        mostrarCuentas(cuentas);
                    } else {
                        // Fallback si mostrarCuentas no está disponible
                        listaCuentas.innerHTML = `
                            <table class="tabla-cuentas">
                                <thead><tr><th>Número</th><th>Tipo</th><th>Saldo</th></tr></thead>
                                <tbody>
                                    ${cuentas.map(c => `
                                        <tr><td><strong>${c.numeroCuenta}</strong></td>
                                        <td>${c.tipoCuenta}</td>
                                        <td>${c.saldo?.toFixed(2) || '0.00'} €</td></tr>
                                    `).join('')}
                                </tbody>
                            </table>
                            <p>✅ Total: ${cuentas.length} cuentas</p>
                        `;
                    }
                }
            } else {
                listaCuentas.innerHTML = '<p class="error">Error al cargar las cuentas</p>';
            }
        } catch (error) {
            console.error('Error:', error);
            listaCuentas.innerHTML = '<p class="error">Error de conexión</p>';
        }
    }
};

window.initListarCuentas = initListarCuentas;