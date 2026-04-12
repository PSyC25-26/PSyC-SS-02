function initListarCuentas() {
    console.log('🚀 Inicializando listarCuentas...');

    setTimeout(() => {
        const selectCliente = document.getElementById('selectClienteFiltro');
        const listaCuentas = document.getElementById('listaCuentas');
        const btnCrear = document.getElementById('btnCrearCuenta');
        const btnAplicarFiltros = document.getElementById('btnAplicarFiltros');
        const btnLimpiarFiltros = document.getElementById('btnLimpiarFiltros');
        
        // Referencias a los filtros
        const filtroIban = document.getElementById('filtroIban');
        const filtroSaldoMin = document.getElementById('filtroSaldoMin');
        const filtroSaldoMax = document.getElementById('filtroSaldoMax');
        const filtroTipoCuenta = document.getElementById('filtroTipoCuenta');

        let clientes = [];
        let cuentasOriginales = []; // Guardar las cuentas originales sin filtrar

        async function cargarClientesSelect() {
            if (!selectCliente) return;
            selectCliente.innerHTML = '<option value="">Cargando clientes...</option>';

            try {
                const response = await fetchConAuth('/api/clientes');
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
                cuentasOriginales = [];
                return;
            }

            listaCuentas.innerHTML = '<p>🔄 Cargando cuentas...</p>';

            try {
                const response = await fetchConAuth(`/api/cuentas?clienteId=${clienteId}`);

                if (response.status === 500 || response.status === 404) {
                    listaCuentas.innerHTML = `
                        <div style="text-align: center; padding: 40px;">
                            <p>📭 <strong>Este cliente no tiene cuentas registradas</strong></p>
                            <p>Puede crear una nueva cuenta usando el botón "➕ Nueva Cuenta"</p>
                        </div>
                    `;
                    cuentasOriginales = [];
                    return;
                }

                if (response.ok) {
                    const cuentas = await response.json();
                    cuentasOriginales = cuentas; // Guardar copia original
                    
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
                    cuentasOriginales = [];
                }
            } catch (error) {
                console.error('Error:', error);
                listaCuentas.innerHTML = '<p class="error">❌ Error de conexión con el servidor</p>';
                cuentasOriginales = [];
            }
        }

        // Función para aplicar filtros
        function aplicarFiltros() {
            if (!cuentasOriginales || cuentasOriginales.length === 0) return;

            const ibanFilter = filtroIban?.value.toLowerCase() || '';
            const saldoMin = parseFloat(filtroSaldoMin?.value) || 0;
            const saldoMax = parseFloat(filtroSaldoMax?.value) || Infinity;
            const tipoFilter = filtroTipoCuenta?.value || '';

            const cuentasFiltradas = cuentasOriginales.filter(cuenta => {
                // Filtro por IBAN
                const cumpleIban = !ibanFilter || cuenta.numeroCuenta.toLowerCase().includes(ibanFilter);
                
                // Filtro por saldo mínimo y máximo
                const saldo = cuenta.saldo || 0;
                const cumpleSaldoMin = saldo >= saldoMin;
                const cumpleSaldoMax = saldo <= saldoMax;
                
                // Filtro por tipo de cuenta
                const cumpleTipo = !tipoFilter || cuenta.tipoCuenta === tipoFilter;
                
                return cumpleIban && cumpleSaldoMin && cumpleSaldoMax && cumpleTipo;
            });

            mostrarCuentas(cuentasFiltradas);
            
            // Mostrar mensaje de resultados
            const totalSpan = document.querySelector('.total-cuentas');
            if (totalSpan) {
                totalSpan.innerHTML = `✅ Mostrando ${cuentasFiltradas.length} de ${cuentasOriginales.length} cuentas`;
            }
        }

        // Función para limpiar filtros
        function limpiarFiltros() {
            if (filtroIban) filtroIban.value = '';
            if (filtroSaldoMin) filtroSaldoMin.value = '';
            if (filtroSaldoMax) filtroSaldoMax.value = '';
            if (filtroTipoCuenta) filtroTipoCuenta.value = '';
            
            // Mostrar todas las cuentas originales
            if (cuentasOriginales && cuentasOriginales.length > 0) {
                mostrarCuentas(cuentasOriginales);
            }
        }

        function mostrarCuentas(cuentas) {
            if (!listaCuentas) return;

            if (cuentas.length === 0) {
                listaCuentas.innerHTML = `
                    <div style="text-align: center; padding: 40px;">
                        <p>🔍 <strong>No hay cuentas que coincidan con los filtros</strong></p>
                        <p>Prueba con otros criterios de búsqueda</p>
                    </div>
                `;
                return;
            }

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
                                <td>${cuenta.tipoCuenta === 'CORRIENTE' ? '💳 Corriente' : '🏦 Ahorro'}</td>
                                <td class="saldo-cuenta" style="color: ${(cuenta.saldo || 0) < 0 ? '#e74c3c' : '#27ae60'}; font-weight: bold;">
                                    ${(cuenta.saldo || 0).toFixed(2)} €
                                </td>
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
                    cuentasOriginales = [];
                }
            };
        }

        // Evento: botón aplicar filtros
        if (btnAplicarFiltros) {
            btnAplicarFiltros.onclick = aplicarFiltros;
        }

        // Evento: botón limpiar filtros
        if (btnLimpiarFiltros) {
            btnLimpiarFiltros.onclick = limpiarFiltros;
        }

        // Evento: filtros automáticos al escribir (opcional - descomentar si se quiere)
        if (filtroIban) {
            filtroIban.addEventListener('input', aplicarFiltros);
        }
        if (filtroTipoCuenta) {
            filtroTipoCuenta.addEventListener('change', aplicarFiltros);
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

        // Cargar clientes al abrir
        cargarClientesSelect();

        console.log('✅ listarCuentas inicializado con filtros');
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
            const response = await fetchConAuth(`/api/cuentas?clienteId=${id}`);

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
                        listaCuentas.innerHTML = `
                            <table class="tabla-cuentas">
                                <thead><tr><th>Número</th><th>Tipo</th><th>Saldo</th></tr></thead>
                                <tbody>
                                    ${cuentas.map(c => `
                                        <tr><td><strong>${c.numeroCuenta}</strong></td>
                                        <td>${c.tipoCuenta}</td>
                                        <td>${(c.saldo || 0).toFixed(2)} €</td>
                                        </tr>
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