// Esperamos a que todo el HTML cargue antes de ejecutar el script
document.addEventListener('DOMContentLoaded', function() {

    // Obtenemos las referencias a los elementos del HTML
    const formDeposito = document.getElementById('formDeposito');
    const mensajeExito = document.getElementById('mensajeExito');
    const mensajeError = document.getElementById('mensajeError');

    // Escuchamos el evento 'submit' (cuando el usuario hace clic en el botón de depositar)
    formDeposito.addEventListener('submit', function(event) {

        // Evitamos que la página se recargue, que es el comportamiento por defecto de HTML
        event.preventDefault();

        // Limpiamos mensajes anteriores por si el usuario está intentando de nuevo
        mensajeExito.style.display = 'none';
        mensajeError.style.display = 'none';

        // Obtenemos los valores de los inputs
        const cuentaId = document.getElementById('cuentaId').value;
        const monto = document.getElementById('monto').value;

        // Creamos el objeto DTO (Data Transfer Object) tal como lo espera el Backend
        const depositoRequest = {
            cuentaId: parseInt(cuentaId), // Nos aseguramos de que sea un número entero
            monto: parseFloat(monto)      // Nos aseguramos de que sea un número decimal
        };

        // Hacemos la petición POST al servidor
        fetch('/cuentas/deposito', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json' // Indicamos que enviamos un JSON
            },
            body: JSON.stringify(depositoRequest)  // Convertimos nuestro objeto JS a texto JSON
        })
            .then(response => {
                if (response.ok) {
                    // Si la respuesta es 200 OK, convertimos la respuesta a JSON
                    return response.json();
                } else {
                    // Si hay un error (ej. 404 o 400), lanzamos la respuesta para capturarla abajo
                    throw response;
                }
            })
            .then(data => {
                // Si todo fue bien, mostramos el éxito usando los datos de la CuentaResponse que devuelve el backend
                mensajeExito.textContent = `¡Depósito de ${monto}€ realizado con éxito! Nuevo saldo de la cuenta ${data.numeroCuenta}: ${data.saldo}€`;
                mensajeExito.style.display = 'block';

                // Limpiamos los campos del formulario para el siguiente uso
                formDeposito.reset();
            })
            .catch(error => {
                // Manejamos los errores según el código HTTP que enviamos desde nuestro Controlador Java
                if (error.status === 404) {
                    mensajeError.textContent = 'Error: No se encontró ninguna cuenta con ese ID.';
                } else if (error.status === 400) {
                    mensajeError.textContent = 'Error: Datos inválidos. Comprueba que el monto sea correcto.';
                } else {
                    mensajeError.textContent = 'Error inesperado al procesar el depósito. Inténtalo de nuevo más tarde.';
                }
                mensajeError.style.display = 'block';
            });
    });
});