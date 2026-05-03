// Referencias globales para que las funciones de recarga puedan acceder
let listaCuentasRef;

// 1. FUNCIÓN PARA DIBUJAR LA TABLA
function mostrarCuentas(cuentas) {
    const lista = listaCuentasRef || document.getElementById('listaCuentas');
    if (!lista) return;

    if (cuentas.length === 0) {
        lista.innerHTML = `
            <div style="text-align: center; padding: 40px;">
                <p>🔍 <strong>No hay cuentas que coincidan con los filtros</strong></p>
                <p>Prueba con otros criterios de búsqueda</p>
            </div>
        `;
        return;
    }

    lista.innerHTML = `
        <table class="tabla-cuentas">
            <thead>
                <tr>
                    <th>Número de Cuenta</th>
                    <th>Tipo</th>
                    <th>Saldo</th>
                    <th>Estado</th>
                    <th>Fecha Creación</th>
                    <th>Acciones</th>
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
                        <td>
                            <span class="badge ${cuenta.activa ? 'badge-exito' : 'badge-error'}">
                                ${cuenta.activa ? 'Activa' : 'Inactiva'}
                            </span>
                        </td>
                        <td>${cuenta.fechaCreacion || '-'}</td>
                        <td>
                            ${cuenta.activa ? `
                                <button class="btn-icon-danger btn-eliminar-cuenta" 
                                        data-cuenta='${JSON.stringify(cuenta)}'
                                        title="Desactivar cuenta">
                                    🗑️
                                </button>
                            ` : '<span style="color:gray; font-size:0.8em;">Sin acciones</span>'}
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
        <p class="total-cuentas">✅ Total: ${cuentas.length} cuentas</p>
    `;

    // Asignar eventos a los botones de eliminar después de crear la tabla
    document.querySelectorAll('.btn-eliminar-cuenta').forEach(btn => {
        btn.onclick = () => {
            const cuentaData = JSON.parse(btn.dataset.cuenta);
            abrirModalEliminar(cuentaData);
        };
    });
}

// 2. FUNCIÓN PARA ABRIR EL MODAL DE ELIMINAR
function abrirModalEliminar(cuenta) {
    window.cuentaSeleccionada = cuenta;
    GestorModales.abrir('eliminarCuenta', 'eliminarCuenta.html');
}

// 3. INICIALIZACIÓN PRINCIPAL
function initListarCuentas() {
    console.log('🚀 Inicializando listarCuentas...');

    setTimeout(() => {
        listaCuentasRef = document.getElementById('listaCuentas');
        const selectCliente = document.getElementById('selectClienteFiltro');
        const btnCrear = document.getElementById('btnCrearCuenta');
        const btnAplicarFiltros = document.getElementById('btnAplicarFiltros');
        const btnLimpiarFiltros = document.getElementById('btnLimpiarFiltros');

        const filtroIban = document.getElementById('filtroIban');
        const filtroSaldoMin = document.getElementById('filtroSaldoMin');
        const filtroSaldoMax = document.getElementById('filtroSaldoMax');
        const filtroTipoCuenta = document.getElementById('filtroTipoCuenta');

        let cuentasOriginales = [];

        async function cargarClientesSelect() {
            if (!selectCliente) return;
            selectCliente.innerHTML = '<option value="">Cargando clientes...</option>';
            try {
                const response = await fetchConAuth('/api/clientes');
                if (response.ok) {
                    const clientes = await response.json();
                    selectCliente.innerHTML = '<option value="">-- Seleccione un cliente --</option>';
                    clientes.forEach(cliente => {
                        const option = document.createElement('option');
                        option.value = cliente.id;
                        option.textContent = `${cliente.id} - ${cliente.nombre} ${cliente.primerApellido || ''} (${cliente.email})`;
                        selectCliente.appendChild(option);
                    });
                }
            } catch (error) { console.error('Error:', error); }
        }

        async function cargarCuentas(clienteId) {
            if (!listaCuentasRef) return;
            if (!clienteId) {
                listaCuentasRef.innerHTML = '<p>📋 Seleccione un cliente para ver sus cuentas</p>';
                return;
            }
            listaCuentasRef.innerHTML = '<p>🔄 Cargando cuentas...</p>';
            try {
                const response = await fetchConAuth(`/api/cuentas?clienteId=${clienteId}`);
                if (response.ok) {
                    const cuentas = await response.json();
                    cuentasOriginales = cuentas;
                    mostrarCuentas(cuentas);
                } else {
                    listaCuentasRef.innerHTML = '<p>📭 Este cliente no tiene cuentas activas.</p>';
                }
            } catch (error) { console.error('Error:', error); }
        }

        function aplicarFiltros() {
            if (!cuentasOriginales.length) return;
            const iban = filtroIban?.value.toLowerCase() || '';
            const min = parseFloat(filtroSaldoMin?.value) || 0;
            const max = parseFloat(filtroSaldoMax?.value) || Infinity;
            const tipo = filtroTipoCuenta?.value || '';

            const filtradas = cuentasOriginales.filter(c => {
                return (!iban || c.numeroCuenta.toLowerCase().includes(iban)) &&
                    ((c.saldo || 0) >= min) && ((c.saldo || 0) <= max) &&
                    (!tipo || c.tipoCuenta === tipo);
            });
            mostrarCuentas(filtradas);
        }

        if (selectCliente) selectCliente.onchange = () => cargarCuentas(selectCliente.value);
        if (btnAplicarFiltros) btnAplicarFiltros.onclick = aplicarFiltros;
        if (btnLimpiarFiltros) btnLimpiarFiltros.onclick = () => {
            filtroIban.value = ''; filtroSaldoMin.value = ''; filtroSaldoMax.value = ''; filtroTipoCuenta.value = '';
            mostrarCuentas(cuentasOriginales);
        };

        if (btnCrear) {
            btnCrear.onclick = () => {
                if (!selectCliente.value) return alert('Seleccione un cliente primero');
                window.clienteIdParaCuenta = selectCliente.value;
                GestorModales.abrir('crearCuentaForm', 'crearCuentaForm.html');
            };
        }

        cargarClientesSelect();
    }, 100);
}

// 4. FUNCIÓN GLOBAL DE RECARGA (Llamada desde el modal de eliminar)
window.recargarListaCuentas = async function(clienteId) {
    const id = clienteId || document.getElementById('selectClienteFiltro')?.value;
    if (!id) return;
    try {
        const response = await fetchConAuth(`/api/cuentas?clienteId=${id}`);
        if (response.ok) {
            const cuentas = await response.json();
            mostrarCuentas(cuentas);
        }
    } catch (e) { console.error(e); }
};

window.initListarCuentas = initListarCuentas;