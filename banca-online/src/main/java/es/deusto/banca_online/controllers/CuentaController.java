package es.deusto.banca_online.controllers;

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.dto.SaldoResponse;
import es.deusto.banca_online.dto.TransferenciaDTO;
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
@RequestMapping("/cuentas")
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

    // PUT /cuentas/1/depositar/50
    @PutMapping("/{cuentaId}/depositar/{monto}")
    public ResponseEntity<CuentaResponse> depositarDinero(
            @PathVariable Long cuentaId,
            @PathVariable Double monto) {
        try {
            // Llamamos a nuestro nuevo servicio
            CuentaResponse response = cuentaService.depositarDinero(cuentaId, monto);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Capturamos el error si el monto es inválido (por ejemplo negativo, etc...)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cuenta no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }
}