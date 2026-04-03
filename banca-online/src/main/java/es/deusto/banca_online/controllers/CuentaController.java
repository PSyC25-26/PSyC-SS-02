package es.deusto.banca_online.controllers;

import es.deusto.banca_online.dto.*;
import es.deusto.banca_online.services.CuentaService;
import es.deusto.banca_online.services.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping
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
    @GetMapping
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
    @ResponseBody
    @GetMapping("/saldo/{cuentaId}")
    public ResponseEntity<SaldoResponse> verSaldo(@PathVariable Long cuentaId) {
        try {
            Double saldo = cuentaService.obtenerSaldo(cuentaId);
            return ResponseEntity.ok(new SaldoResponse(saldo));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cuenta no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    @GetMapping("/saldo")
    public String vistaSaldo() {
        return "consultar-saldo";
    }
    
    //GET /cuentas/{clienteId}
    @GetMapping("/{clienteId}")
    public ResponseEntity<List<CuentaResponse>> obtenerCuentasPorClienteId(@PathVariable Long clienteId) {
        try {
            return ResponseEntity.ok(cuentaService.obtenerCuentasPorCliente(clienteId));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cliente no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    //Transferencia de dinero entre cuentas
    @PostMapping("/transferir")
    public ResponseEntity<TransferenciaDTO> transferirDinero(@RequestBody @Valid TransferenciaDTO transferenciaDTO) {
        try {
            TransferenciaDTO transferencia = transferService.transferirDinero(transferenciaDTO);
            return ResponseEntity.ok(transferencia);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cuenta no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    // POST /cuentas/deposito
    @PostMapping("/depositar")
    public ResponseEntity<CuentaResponse> depositarDinero(@RequestBody @Valid DepositoRequest request) {
        try {
            // Usamos los datos que vienen dentro del DTO
            CuentaResponse response = cuentaService.depositarDinero(request.getCuentaId(), request.getMonto());
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
    @PostMapping("/retirar")
    public ResponseEntity<CuentaResponse> retirarDinero(@RequestBody @Valid RetiroRequest request) {
        try {
            // Llamamos al servicio de retiro que creamos en el paso anterior
            CuentaResponse response = cuentaService.retirarDinero(request.getCuentaId(), request.getMonto());
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
}