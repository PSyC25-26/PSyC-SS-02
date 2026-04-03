// Elementos del DOM
const formBuscarCuentas = document.getElementById('formBuscarCuentas');
const clienteIdInput = document.getElementById('clienteId');
const mensajeExito = document.getElementById('mensajeExito');
const mensajeError = document.getElementById('mensajeError');
const listaCuentasContainer = document.getElementById('listaCuentasContainer');
const noCuentasContainer = document.getElementById('noCuentasContainer');
const listaCuentas = document.getElementById('listaCuentas');
const tituloCuentas = document.getElementById('tituloCuentas');

// Escuchar el envio del formulario
formBuscarCuentas.addEventListener('submit', async (e) => {
    // Evitamos que la página se recargue
    e.preventDefault();

    // Limpiamos mensajes y contenedores previos
    ocultarMensajes();
    listaCuentasContainer.style.display = 'none';
    noCuentasContainer.style.display = 'none';

    // Obtenemos el ID del cliente del input
    const clienteId = clienteIdInput.value.trim();

    // Validamos que el ID sea valido (no vacio y mayor a 0)
    if (!clienteId || parseInt(clienteId) <= 0) {
        mostrarError('Introduce un ID de cliente válido');
        return;
    }

    // Llamamos a la funcion que consulta las cuentas
    await consultarCuentas(clienteId);
});

// Realiza peticion GET al endpoint
async function consultarCuentas(clienteId) {
    try {
        // Realizamos la peticion GET al endpoint /cuentas/{clienteId}
        const response = await fetch(`/cuentas/${clienteId}`);

        // Validamos que la respuesta sea correcta
        if (!response.ok) {
            // Si es 404, el cliente no existe
            if (response.status === 404) {
                mostrarError('El cliente no existe o no tiene cuentas');
                noCuentasContainer.style.display = 'block';
                return;
            } else {
                // Cualquier otro error HTTP
                mostrarError('Error al obtener las cuentas');
                return;
            }
        }

        // Convertimos la respuesta JSON a array de cuentas
        const cuentas = await response.json();

        // Verificamos si la lista esta vacia
        if (cuentas.length === 0) {
            noCuentasContainer.style.display = 'block';
            mostrarError('Este cliente no tiene cuentas registradas');
            return;
        }

        // Si hay cuentas, las mostramos en pantalla
        mostrarCuentas(cuentas);
        // Y mostramos un mensaje de exito
        mostrarExito(`Se encontraron ${cuentas.length} cuenta(s)`);

    } catch (error) {
        // Capturamos errores de conexion o parsing
        console.error('Error:', error);
        mostrarError('Error de conexión con el servidor');
    }
}

// Renderiza las cuentas en el DOM
function mostrarCuentas(cuentas) {
    // Limpiamos la lista anterior para evitar duplicados
    listaCuentas.innerHTML = '';

    // Iteramos sobre cada cuenta y creamos una tarjeta
    cuentas.forEach(cuenta => {
        // Creamos un div para la tarjeta
        const cuentaCard = document.createElement('div');
        cuentaCard.className = 'cliente-card'; // Reutilizamos estilos existentes
        
        // Rellenamos la tarjeta con la informaciopn de la cuenta
        cuentaCard.innerHTML = `
            <p class="cliente-nombre">Cuenta #${cuenta.id}</p>
            <p><strong>Número de Cuenta:</strong> ${cuenta.numeroCuenta}</p>
            <p class="cliente-email"><strong>Saldo:</strong> ${cuenta.saldo.toFixed(2)} €</p>
            <p><strong>Tipo:</strong> ${cuenta.tipoCuenta}</p>
            <p><small>Cliente ID: ${cuenta.clienteId}</small></p>
        `;
        
        // Añadimos la tarjeta al contenedor
        listaCuentas.appendChild(cuentaCard);
    });

    // Actualizamos el titulo con el numero de cuentas encontradas
    tituloCuentas.textContent = `Cuentas del Cliente ${clienteIdInput.value} (${cuentas.length})`;
    
    // Mostramos el contenedor de cuentas
    listaCuentasContainer.style.display = 'block';
}

function mostrarExito(mensaje) {
    // Establecemos el texto del mensaje
    mensajeExito.textContent = mensaje;
    // Lo mostramos
    mensajeExito.style.display = 'block';
    // Ocultamos cualquier mensaje de error
    mensajeError.style.display = 'none';

    // Autoocultar despues de 4 segundos
    setTimeout(() => {
        mensajeExito.style.display = 'none';
    }, 4000);
}

function mostrarError(mensaje) {
    // Establecemos el texto del mensaje
    mensajeError.textContent = mensaje;
    // Lo mostramos
    mensajeError.style.display = 'block';
    // Ocultamos cualquier mensaje de exito
    mensajeExito.style.display = 'none';
}

function ocultarMensajes() {
    mensajeExito.style.display = 'none';
    mensajeError.style.display = 'none';
}