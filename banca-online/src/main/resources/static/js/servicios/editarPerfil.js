function initEditarPerfil() {
    // Aumentamos ligeramente el tiempo a 200ms para asegurar que el HTML esté listo
    setTimeout(() => {
        const form = document.getElementById('formEditarPerfil');
        const seccionVista = document.getElementById('seccionVistaPerfil');
        const btnIrAEditar = document.getElementById('btnIrAEditar');
        const btnCancelar = document.getElementById('btnCancelarEdicion');

        const msgExito = document.getElementById('mensajeExitoPerfil');
        const msgError = document.getElementById('mensajeErrorPerfil');

        // Referencias de visualización
        const refs = {
            nombre: document.getElementById('verNombre'),
            dni: document.getElementById('verDni'),
            email: document.getElementById('verEmail'),
            tel: document.getElementById('verTelefono'),
            dir: document.getElementById('verDireccion')
        };

        async function cargarDatos() {
            try {
                const email = Auth.getEmail();
                if (!email) {
                    console.error("No se encontró email en la sesión");
                    return;
                }

                const response = await fetchConAuth(`/api/clientes/email/${email}`);
                if (response.ok) {
                    const c = await response.json();
                    console.log("Datos recibidos:", c); // <--- Revisa la consola (F12) para ver los nombres de los campos

                    // Rellenar etiquetas de vista (usando || '' para evitar que salga "undefined")
                    if (refs.nombre) {
                        const nombreCompleto = `${c.nombre || ''} ${c.primerApellido || ''} ${c.segundoApellido || ''}`.trim();
                        refs.nombre.textContent = nombreCompleto || 'Sin nombre';
                    }
                    if (refs.dni) refs.dni.textContent = c.dni || 'No disponible';
                    if (refs.email) refs.email.textContent = c.email || email;
                    if (refs.tel) refs.tel.textContent = c.telefono || 'No indicado';
                    if (refs.dir) refs.dir.textContent = c.direccion || 'No indicada';

                    // Rellenar inputs para edición (para que el usuario vea lo que ya tiene)
                    const inputTel = document.getElementById('perfilTelefono');
                    const inputDir = document.getElementById('perfilDireccion');
                    if (inputTel) inputTel.value = c.telefono || '';
                    if (inputDir) inputDir.value = c.direccion || '';
                }
            } catch (error) {
                console.error("Error al cargar perfil:", error);
                if (refs.nombre) refs.nombre.textContent = "Error de conexión";
            }
        }

        cargarDatos();

        // Navegación entre Vista y Edición
        if (btnIrAEditar) {
            btnIrAEditar.onclick = () => {
                seccionVista.style.display = 'none';
                form.style.display = 'block';
                msgExito.style.display = 'none'; // Limpiar mensajes al cambiar de pestaña
            };
        }

        if (btnCancelar) {
            btnCancelar.onclick = () => {
                form.style.display = 'none';
                seccionVista.style.display = 'block';
            };
        }

        // Envío del formulario
        form.onsubmit = async (e) => {
            e.preventDefault();
            msgExito.style.display = 'none';
            msgError.style.display = 'none';

            const btnGuardar = document.getElementById('btnGuardarCambios');
            btnGuardar.disabled = true;
            btnGuardar.textContent = 'Actualizando...';

            const payload = {
                telefono: document.getElementById('perfilTelefono').value.trim() || null,
                direccion: document.getElementById('perfilDireccion').value.trim() || null
            };

            try {
                const response = await fetchConAuth('/api/clientes/perfil', {
                    method: 'PUT',
                    body: JSON.stringify(payload)
                });

                if (response.ok) {
                    msgExito.textContent = '✅ Perfil actualizado correctamente';
                    msgExito.style.display = 'block';

                    // Volvemos a cargar los datos para que la vista de lectura se actualice
                    await cargarDatos();

                    setTimeout(() => {
                        GestorModales.cerrar('editarPerfil');
                    }, 2000);
                } else {
                    msgError.textContent = '❌ No se pudo actualizar el perfil';
                    msgError.style.display = 'block';
                    btnGuardar.disabled = false;
                    btnGuardar.textContent = 'Guardar Cambios';
                }
            } catch (error) {
                msgError.textContent = '❌ Error de red';
                msgError.style.display = 'block';
                btnGuardar.disabled = false;
                btnGuardar.textContent = 'Guardar Cambios';
            }
        };
    }, 200);
}

window.initEditarPerfil = initEditarPerfil;