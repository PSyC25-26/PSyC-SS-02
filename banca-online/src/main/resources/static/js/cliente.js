// Lista de clientes
let clientes = [];

// Elementos del DOM
const formulario = document.getElementById('miForm');
const btnAbrir = document.getElementById('btnAbrir');
const btnCerrar = document.getElementById('btnCerrar');
const btnCancelar = document.getElementById('btnCancelar');
const form = document.getElementById('formCliente');
const mensajeExito = document.getElementById('mensajeExito');
const mensajeError = document.getElementById('mensajeError');
const listaClientes = document.getElementById('listaClientes');



/*------------------------------------
   FUNCIONALIDAD BÁSICA BOTONES
------------------------------------*/

// ABRIR formulario
btnAbrir.addEventListener('click', () => {
    formulario.style.display = 'block';
    form.reset(); // Limpiar formulario
    ocultarMensajes();
});

// CERRAR formulario
btnCerrar.addEventListener('click', cerrarForm);

// CANCELAR formulario
btnCancelar.addEventListener('click', cerrarForm);

// CERRAR formulario (click fuera del formulario)
window.addEventListener('click', (event) => {
    if (event.target === formulario) {
        cerrarForm();
    }
});


function cerrarForm() {
    formulario.style.display = 'none';
    ocultarMensajes();
}



/*-----------------
      MENSAJES
-----------------*/
function mostrarExito(mensaje) {
    mensajeExito.textContent = mensaje;
    mensajeExito.style.display = 'block';
    mensajeError.style.display = 'none';

    // Auto-ocultar después de 3 segundos
    setTimeout(() => {
        mensajeExito.style.display = 'none';
    }, 3000);
}

function mostrarError(mensaje) {
    mensajeError.textContent = mensaje;
    mensajeError.style.display = 'block';
    mensajeExito.style.display = 'none';
}

function ocultarMensajes() {
    mensajeExito.style.display = 'none';
    mensajeError.style.display = 'none';
}





/*---------------------------
    CONEXIÓN CON LA API
---------------------------*/

// Enviar formulario
form.addEventListener('submit', async (event) => {
    event.preventDefault(); // Que no se recargue la pagina

    // Mostrar indicador de carga
    const btnGuardar = document.getElementById('btnGuardar');
    btnGuardar.disabled = true; // No se puede clickar
    btnGuardar.textContent = 'Guardando...'; //Cambiamos el texto

    // Ocultamos los mensajes de exito o error
    ocultarMensajes();

    // Recogemos los datos del formulario
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
        // Llamada a la API
        const response = await fetch('/api/clientes', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            const nuevoCliente = await response.json();

            // Mostrar mensaje de éxito
            mostrarExito('✅ Cliente creado con éxito');

            // Actualizar lista
            await cargarClientes();

            // Cerrar formulario después de 2 segundos
            setTimeout(() => {
                cerrarForm();
            }, 2000);
        } else {
            // Manejar errores de validación
            const error = await response.json();
            mostrarError(error.message || 'Error al crear el cliente');
        }
    } catch (error) {
        // Error de red o servidor
        mostrarError('Error de conexión con el servidor');
        console.error('Error:', error);
    } finally {
        // Restaurar botón
        btnGuardar.disabled = false;
        btnGuardar.textContent = 'Guardar Cliente';
    }
});





// ============================================
// #125 - Cargar y mostrar clientes
// ============================================

async function cargarClientes() {
    try {
        const response = await fetch('/api/clientes');
        if (response.ok) {
            clientes = await response.json();
            mostrarClientes();
        }
    } catch (error) {
        console.error('Error al cargar clientes:', error);
        listaClientes.innerHTML = '<p class="error">Error al cargar los clientes</p>';
    }
}

function mostrarClientes() {
    if (clientes.length === 0) {
        listaClientes.innerHTML = '<p>No hay clientes registrados</p>';
        return;
    }

    listaClientes.innerHTML = clientes.map(cliente => `
        <div class="cliente-card">
            <p class="cliente-nombre">${cliente.nombre || ''} ${cliente.primerApellido || ''}</p>
            <p class="cliente-email">${cliente.email}</p>
            <p><small>ID: ${cliente.id}</small></p>
            <p><small>DNI: ${cliente.dni || 'No especificado'}</small></p>
        </div>
    `).join('');
}

// Cargar clientes al iniciar la página
document.addEventListener('DOMContentLoaded', cargarClientes);