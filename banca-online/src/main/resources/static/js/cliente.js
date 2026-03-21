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
            <button class="btn-editar" onclick="abrirModalEditar(${cliente.id})">Editar / Eliminar</button>
        </div>
    `).join('');
}

// Cargar clientes al iniciar la página
document.addEventListener('DOMContentLoaded', cargarClientes);



// funcion editar
async function abrirModalEditar(id) {
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
            ? `${fn[0]}-${String(fn[1]).padStart(2,'0')}-${String(fn[2]).padStart(2,'0')}`
            : fn || '';
        document.getElementById('modalEditarEliminar').style.display = 'block';

    } catch (error) {
        alert('Error al cargar el cliente');
    }
}

const modalEditarEliminar = document.getElementById('modalEditarEliminar');
const btnCerrarEditar = document.getElementById('btnCerrarEditar');
const formEditar = document.getElementById('formEditar');
const btnEliminar = document.getElementById('btnEliminar');
const mensajeExitoEditar = document.getElementById('mensajeExitoEditar');
const mensajeErrorEditar = document.getElementById('mensajeErrorEditar');

btnCerrarEditar.addEventListener('click', () => {
    modalEditarEliminar.style.display = 'none';
});

window.addEventListener('click', (e) => {
    if (e.target === modalEditarEliminar) modalEditarEliminar.style.display = 'none';
});

function ocultarMensajesEditar() {
    mensajeExitoEditar.style.display = 'none';
    mensajeErrorEditar.style.display = 'none';
}

// GUARDAR cambios (PUT)
formEditar.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('editId').value;
    const datos = {
        dni: document.getElementById('editDni').value,
        nombre: document.getElementById('editNombre').value,
        primerApellido: document.getElementById('editPrimerApellido').value,
        segundoApellido: document.getElementById('editSegundoApellido').value,
        fechaNacimiento: document.getElementById('editFechaNacimiento').value,
        email: document.getElementById('editEmail').value,
        telefono: document.getElementById('editTelefono').value,
        direccion: document.getElementById('editDireccion').value
    };

    try {
        const response = await fetch(`/api/clientes/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(datos)
        });

        if (response.ok) {
            mensajeExitoEditar.textContent = 'Cliente actualizado con éxito';
            mensajeExitoEditar.style.display = 'block';
            await cargarClientes();
            setTimeout(() => { modalEditarEliminar.style.display = 'none'; }, 2000);
        } else {
            const error = await response.json().catch(() => ({}));
            mensajeErrorEditar.textContent = (error.message || 'Error al actualizar');
            mensajeErrorEditar.style.display = 'block';
        }
    } catch {
        mensajeErrorEditar.textContent = 'Error de conexión con el servidor';
        mensajeErrorEditar.style.display = 'block';
    }
});

// ELIMINAR (DELETE)
btnEliminar.addEventListener('click', async () => {
    const id = document.getElementById('editId').value;
    if (!confirm('¿Seguro que quieres eliminar este cliente?')) return;

    try {
        const response = await fetch(`/api/clientes/${id}`, { method: 'DELETE' });

        if (response.status === 204) {
            mensajeExitoEditar.textContent = 'Cliente eliminado con éxito';
            mensajeExitoEditar.style.display = 'block';
            await cargarClientes();
            setTimeout(() => { modalEditarEliminar.style.display = 'none'; }, 2000);
        } else if (response.status === 404) {
            mensajeErrorEditar.textContent = 'Cliente no encontrado';
            mensajeErrorEditar.style.display = 'block';
        } else {
            mensajeErrorEditar.textContent = 'Error al eliminar el cliente';
            mensajeErrorEditar.style.display = 'block';
        }
    } catch {
        mensajeErrorEditar.textContent = 'Error de conexión con el servidor';
        mensajeErrorEditar.style.display = 'block';
    }
});