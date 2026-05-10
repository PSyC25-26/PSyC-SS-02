package es.deusto.banca_online.controllers;

/**
 * @file CuentaController.java
 * @brief Controlador REST para la gestión de cuentas bancarias, depósitos, retiros y transferencias.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.dto.*;
import es.deusto.banca_online.services.CuentaService;
import es.deusto.banca_online.services.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * Controlador que expone los servicios relacionados con las cuentas bancarias.
 * Gestiona operaciones como creación, depósitos, retiros y eliminación de cuentas.
 */
@Controller
@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;
    private final TransferService transferService;

    public CuentaController(CuentaService cuentaService, TransferService transferService) {
        this.cuentaService = cuentaService;
        this.transferService = transferService;
    }

    //POST /cuentas
    /**
     * Crea una nueva cuenta bancaria (solo accesible por administradores).
     * @param request Datos de la cuenta: número, saldo inicial y tipo.
     * @return Detalles de la cuenta creada.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuentaResponse> crearCuenta(@RequestBody @Valid CuentaRequest request) {
        try {
            CuentaResponse response = cuentaService.crearCuenta(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (IllegalArgumentException e)

        {
            // Capturamos IllegalArgumentException (tipo de cuenta inválido)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cliente no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e; // Otras excepciones se lanzan normalmente
        }
    }

    //GET /cuentas?clienteId=X
    /**
     * Obtiene el listado de todas las cuentas asociadas a un cliente específico.
     * Esta consulta es de uso exclusivo para administradores del sistema.
     * * @param clienteId Identificador único del cliente cuyas cuentas se desean consultar.
     * @return ResponseEntity con la lista de cuentas del cliente (200 OK) si el cliente existe,
     * o estado 404 Not Found si el identificador del cliente no es válido o no existe.
     * @throws RuntimeException Si ocurre un error inesperado en el servidor durante la consulta.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CuentaResponse>> obtenerCuentas(@RequestParam Long clienteId) {
        try {
            return ResponseEntity.ok(cuentaService.obtenerCuentasPorCliente(clienteId));
        }
        catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cliente no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e; // Otras excepciones se lanzan normalmente
        }
    }

    //GET  /saldo por Id
    /**
     * Consulta el saldo disponible de una cuenta específica.
     * Implementa validación de seguridad para asegurar que un cliente solo pueda 
     * acceder al saldo de sus propias cuentas, mientras que el administrador 
     * tiene acceso total.
     * * @param cuentaId Identificador numérico de la cuenta a consultar.
     * @param authentication Objeto de autenticación para verificar la identidad del solicitante.
     * @return ResponseEntity con un objeto SaldoResponse (200 OK) o estado 404 Not Found 
     * si la cuenta no existe.
     * @throws RuntimeException Si ocurre un error inesperado durante la consulta.
     */
    @ResponseBody
    @GetMapping("/saldo/{cuentaId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
    public ResponseEntity<SaldoResponse> verSaldo(@PathVariable Long cuentaId,
                                                   Authentication authentication) {
        try {
            Double saldo = cuentaService.obtenerSaldo(cuentaId, authentication);
            return ResponseEntity.ok(new SaldoResponse(saldo));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cuenta no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    /**
     * Carga y devuelve la vista HTML para la consulta de saldo.
     * Este método mapea la ruta "/saldo" hacia la plantilla "consultar-saldo.html", 
     * permitiendo al usuario visualizar la interfaz de usuario correspondiente.
     * * @return String con el nombre de la plantilla de la vista a renderizar.
     */
    @GetMapping("/saldo")
    public String vistaSaldo() {
        return "consultar-saldo";
    }
    
    //GET /cuentas/{clienteId}
    /**
     * Recupera el listado de cuentas asociadas a un ID de cliente a través de la ruta.
     * Incluye validación de seguridad: un cliente solo puede consultar sus propias cuentas,
     * mientras que el administrador tiene acceso a cualquier cliente.
     * * @param clienteId Identificador numérico del cliente en la URL.
     * @param authentication Datos del usuario autenticado para validar permisos.
     * @return ResponseEntity con la lista de cuentas (200 OK) o 404 Not Found si el cliente no existe.
     * @throws RuntimeException Si ocurre un error interno en la consulta.
     */
    @GetMapping("/{clienteId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
    public ResponseEntity<List<CuentaResponse>> obtenerCuentasPorClienteId(@PathVariable Long clienteId,
                                                                            Authentication authentication) {
        try {
            return ResponseEntity.ok(cuentaService.obtenerCuentasPorCliente(clienteId, authentication));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cliente no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    //Transferencia de dinero entre cuentas
    /**
     * Ejecuta una transferencia de fondos entre dos cuentas bancarias.
     * El servicio valida que la cuenta de origen pertenezca al usuario autenticado 
     * (si es un cliente) y que existan fondos suficientes.
     * * @param transferenciaDTO Objeto con cuenta origen, destino, monto y concepto.
     * @param authentication Usuario que solicita la operación.
     * @return ResponseEntity con el resumen de la transferencia realizada.
     * @throws RuntimeException Si alguna de las cuentas no existe (404) o hay errores de lógica.
     */
    @PostMapping("/transferir")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
    public ResponseEntity<TransferenciaDTO> transferirDinero(@RequestBody @Valid TransferenciaDTO transferenciaDTO,
                                                             Authentication authentication) {
        try {
            TransferenciaDTO transferencia = transferService.transferirDinero(transferenciaDTO, authentication);
            return ResponseEntity.ok(transferencia);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cuenta no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    // POST /cuentas/deposito
    /**
     * Realiza un ingreso de capital en una cuenta bancaria.
     * Incrementa el saldo de la cuenta destino y registra el movimiento en el historial.
     * * @param request DTO que contiene el ID de la cuenta y la cantidad a depositar.
     * @param authentication Usuario autenticado que realiza la operación.
     * @return ResponseEntity con los datos actualizados de la cuenta (200 OK).
     * @throws IllegalArgumentException Si el monto es negativo o nulo (400 Bad Request).
     * @throws RuntimeException Si la cuenta especificada no existe (404 Not Found).
     */
    @PostMapping("/depositar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
    public ResponseEntity<CuentaResponse> depositarDinero(@RequestBody @Valid DepositoRequest request,
                                                          Authentication authentication) {
        try {
            CuentaResponse response = cuentaService.depositarDinero(request.getCuentaId(), request.getMonto(), authentication);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cuenta no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    // POST /cuentas/retiro
    /**
     * Realiza un retiro de dinero de una cuenta específica.
     * @param request Datos del retiro (monto y cuentaId).
     * @param authentication Usuario que solicita la operación para validar permisos.
     * @return Estado actualizado de la cuenta tras el retiro.
     */
    @PostMapping("/retirar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
    public ResponseEntity<CuentaResponse> retirarDinero(@RequestBody @Valid RetiroRequest request,
                                                        Authentication authentication) {
        try {
            CuentaResponse response = cuentaService.retirarDinero(request.getCuentaId(), request.getMonto(), authentication);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Capturamos errores de validación (monto <= 0) o de lógica (saldo insuficiente)
            // Enviamos un 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        } catch (RuntimeException e) {
            // Capturamos si la cuenta no existe
            if (e.getMessage() != null && e.getMessage().contains("Cuenta no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    // HU2.3: Eliminar cuenta inactiva
    /**
     * Elimina una cuenta bancaria del sistema (Acceso exclusivo ADMIN).
     * La cuenta solo puede ser eliminada si su saldo es exactamente cero.
     * * @param id Identificador numérico de la cuenta que se desea eliminar.
     * @return ResponseEntity con:
     * - 204 No Content: Si la cuenta se eliminó correctamente.
     * - 400 Bad Request: Si la cuenta tiene saldo positivo (restricción de negocio).
     * - 404 Not Found: Si no existe ninguna cuenta con el ID proporcionado.
     * @throws RuntimeException Si ocurre un error inesperado en el servidor.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCuentaInactiva(@PathVariable Long id) {
        try {
            cuentaService.eliminarCuentaInactiva(id);
            return ResponseEntity.noContent().build(); // 204 OK
        } catch (IllegalArgumentException e) {
            // Saldo > 0, enviamos 400 Bad Request, restriccion
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            // No existe, enviamos 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}