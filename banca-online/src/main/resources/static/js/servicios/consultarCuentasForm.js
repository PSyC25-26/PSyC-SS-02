window.initConsultarCuentasForm = function() {
    const form = document.getElementById('formBuscarCuentas');
    
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const clienteId = document.getElementById('clienteId').value;

        try {
            // Conexión con el Backend JPA
            const response = await fetch(`/api/cuentas/cliente/${clienteId}`);
            const cuentas = await response.json();
            
            // Renderizar resultados en el contenedor del modal
            const lista = document.getElementById('listaCuentas');
            lista.innerHTML = cuentas.map(cta => `
                <div class="cuenta-item">
                    <p><strong>IBAN:</strong> ${cta.numeroCuenta}</p>
                    <p><strong>Saldo:</strong> ${cta.saldo} €</p>
                </div>
            `).join('');
            
            document.getElementById('listaCuentasContainer').style.display = 'block';
        } catch (error) {
            console.error("Error al consultar:", error);
        }
    });
};