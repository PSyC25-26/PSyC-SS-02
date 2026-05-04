// js/servicios/historialTransacciones.js
function initHistorialTransacciones() {
    console.log('📜 Inicializando historial...');

    // 1. Intentamos coger la cuenta de la variable global o del localStorage
    let cuenta = window.cuentaSeleccionada;

    if (!cuenta) {
        const cuentaGuardada = localStorage.getItem('cuentaSeleccionada');
        if (cuentaGuardada) {
            cuenta = JSON.parse(cuentaGuardada);
            window.cuentaSeleccionada = cuenta; // La restauramos
            console.log('📦 Cuenta recuperada de localStorage para historial:', cuenta.numeroCuenta);
        }
    }

    if (!cuenta) {
        console.error("❌ No hay cuenta seleccionada para el historial");
        const contenedor = document.getElementById('contenedorMovimientos');
        if (contenedor) contenedor.innerHTML = '<p class="error">Error: No se seleccionó ninguna cuenta.</p>';
        return;
    }

    // 2. Rellenar el encabezado del modal con el número de cuenta
    const spanNumero = document.getElementById('historialCuentaNumero');
    if (spanNumero) spanNumero.textContent = cuenta.numeroCuenta;

    // 3. Llamar a la función que trae los datos del backend
    cargarDatosHistorial(cuenta.id);
}

async function cargarDatosHistorial(cuentaId) {
    const contenedor = document.getElementById('contenedorMovimientos');
    if (!contenedor) return;

    try {
        // fetchConAuth añade automáticamente el Token JWT
        const response = await fetchConAuth(`/api/transacciones/${cuentaId}`);

        if (response.ok) {
            const transacciones = await response.json();

            if (transacciones.length === 0) {
                contenedor.innerHTML = '<p style="padding: 20px; text-align: center;">No hay movimientos registrados.</p>';
                return;
            }

            // Pintamos la lista de movimientos
            contenedor.innerHTML = transacciones.map(t => {
                // Lógica para saber si el dinero entra o sale
                const esEntrada = t.descripcion.includes('RECIBIDA') || t.descripcion.includes('Depósito');
                const colorMonto = esEntrada ? '#28a745' : '#dc3545';
                const signo = esEntrada ? '+' : '-';

                return `
                <div class="transaccion-item" style="display: grid; grid-template-columns: 2fr 1fr 1fr; padding: 12px; border-bottom: 1px solid #eee; align-items: center;">
                    <div style="display: flex; flex-direction: column;">
                        <span style="font-weight: 500; font-size: 0.95em;">${t.descripcion}</span>
                        <span style="font-size: 0.75em; color: #888;">ID: ${t.id || 'N/A'}</span>
                    </div>
                    <span style="font-weight: bold; color: ${colorMonto}; text-align: right; padding-right: 15px;">
                        ${signo}${t.total.toFixed(2)} €
                    </span>
                    <span style="font-size: 0.85em; color: #666; text-align: center;">
                        ${new Date(t.fecha).toLocaleDateString()}
                    </span>
                </div>
                `;
            }).join('');

        } else {
            contenedor.innerHTML = '<p class="error">No se pudo cargar el historial (Error: ' + response.status + ')</p>';
        }
    } catch (error) {
        console.error("Error en fetch historial:", error);
        contenedor.innerHTML = '<p class="error">Error de conexión con el servidor.</p>';
    }
}

// Importante: Exponer la función al objeto window para que GestorModales la encuentre
window.initHistorialTransacciones = initHistorialTransacciones;