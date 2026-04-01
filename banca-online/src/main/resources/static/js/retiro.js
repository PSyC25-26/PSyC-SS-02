document.addEventListener('DOMContentLoaded', function() {

    const formRetiro = document.getElementById('formRetiro');
    const mensajeExito = document.getElementById('mensajeExito');
    const mensajeError = document.getElementById('mensajeError');

    formRetiro.addEventListener('submit', function(event) {
        event.preventDefault();

        // Limpiamos mensajes previos
        mensajeExito.style.display = 'none';
        mensajeError.style.display = 'none';

        const cuentaId = document.getElementById('cuentaId').value;
        const monto = document.getElementById('monto').value;

        const retiroRequest = {
            cuentaId: parseInt(cuentaId),
            monto: parseFloat(monto)
        };

        // Hacemos la petición POST al nuevo endpoint de retiro
        fetch('/cuentas/retiro', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(retiroRequest)
        })
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw response;
                }
            })
            .then(data => {
                // Tarea 1: Mostrar mensaje de éxito
                mensajeExito.textContent = `¡Retiro de ${monto}€ realizado! Nuevo saldo de la cuenta ${data.numeroCuenta}: ${data.saldo}€`;
                mensajeExito.style.display = 'block';
                formRetiro.reset();
            })
            .catch(error => {
                // Tarea 2: Validar saldo y mostrar mensaje de error
                if (error.status === 404) {
                    mensajeError.textContent = 'Error: No se encontró ninguna cuenta con ese ID.';
                } else if (error.status === 400) {
                    // El código 400 se lanza en el backend cuando el monto es negativo o cuando NO hay saldo suficiente
                    mensajeError.textContent = 'Error: No se pudo realizar el retiro. Comprueba que el monto sea correcto y que tengas saldo suficiente en tu cuenta.';
                } else {
                    mensajeError.textContent = 'Error inesperado al procesar el retiro.';
                }
                mensajeError.style.display = 'block';
            });
    });
});