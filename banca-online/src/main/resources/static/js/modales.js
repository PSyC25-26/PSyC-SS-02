// CARGA HTML EXTERNOS
const GestorModales = {
    // Cache para no recargar el mismo HTML varias veces
    cache: {},

    // ABRIR un modal cargando su HTML desde archivo
    async abrir(modalNombre, archivoHtml) {
        // Obtenemos el contenedor donde va a ir el modal
        const modalContainer = document.getElementById('modalContainer');

        try {
            // Si se ha cargado antes el modal, lo cogemos de caché
            let html = this.cache[archivoHtml];

            // De lo contrario, hacemos fetch para traer el HTML de la carpeta correspondiente
            if (!html) {
                const response = await fetch(`modales/${archivoHtml}`);
                if (!response.ok) throw new Error('No se pudo cargar el modal'); // Manejo de errores de carga de modal
                html = await response.text();
                this.cache[archivoHtml] = html; // Lo metemos en caché
            }

            // Creamos la estructura para el HTML
            // CREAMOS LA ESTRUCTURA PARA EL HTML
            const modalHtml = `
                <div id="modal-${modalNombre}" style="display: block !important; position: fixed !important; top: 0 !important; left: 0 !important; width: 100% !important; height: 100% !important; background-color: rgba(0,0,0,0.5) !important; z-index: 10000 !important;">
                    <div style="display: flex; align-items: center; justify-content: center; min-height: 100%;">
                        <div class="form-contenido" style="margin: 20px; width: 90%; max-width: 500px;">
                            <div class="form-header">
                                <h2>${this.getTitulo(modalNombre)}</h2>
                                <button class="btn-cerrar" onclick="GestorModales.cerrar('${modalNombre}')">×</button>
                            </div>
                            <div class="form-scroll">
                                ${html}
                            </div>
                        </div>
                    </div>
                </div>
            `;

            // Insertamos el modal en el DOM
            modalContainer.innerHTML = modalHtml;

            // Ejecutar el script específico del modal
            await this.cargarScript(modalNombre);

        } catch (error) { // Capturamos errores
            console.error('Error cargando modal:', error);
            modalContainer.innerHTML = `<div class="mensaje-error">Error al cargar el formulario</div>`;
        }
    },


    // CERRAR el modal
    cerrar(modalNombre) {
        const modal = document.getElementById(`modal-${modalNombre}`); // Buscamos el modal
        if (modal) modal.remove(); // Eliminamos el modal si es que existe
    },


    // DEVOLVER el TÍTULO de cada modal
    getTitulo(modalNombre) {
        const titulos = {
            crearCliente: '👤 Crear nuevo cliente',
            consultarSaldo: '💰 Consultar saldo',
            deposito: '💵 Depositar dinero',
            retiro: '💸 Retirar dinero',
            transferencia: '🔄 Transferir dinero',
            crearCuenta: '👤 Crear nueva cuenta'
        };
        return titulos[modalNombre] || 'Formulario';
    },


    // CARGAR el SCRIPT de cada modal
    async cargarScript(modalNombre) {
        // Eliminar script anterior si existe
        const scriptAnterior = document.getElementById(`script-${modalNombre}`); // Buscamos el script del modal
        if (scriptAnterior) scriptAnterior.remove(); // Eliminamos el script si es que existe

        return new Promise((resolve) => {
            const script = document.createElement('script');
            script.id = `script-${modalNombre}`;
            script.src = `js/servicios/${modalNombre}.js`;
            script.onload = () => {
                if (window[`init${modalNombre.charAt(0).toUpperCase() + modalNombre.slice(1)}`]) {
                    window[`init${modalNombre.charAt(0).toUpperCase() + modalNombre.slice(1)}`]();
                }
                resolve();
            };
            script.onerror = () => {
                console.error(`Error cargando script: ${modalNombre}.js`);
                resolve();
            };
            document.body.appendChild(script);
        });
    }
};