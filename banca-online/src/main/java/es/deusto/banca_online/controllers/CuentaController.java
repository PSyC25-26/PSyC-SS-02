package es.deusto.banca_online.controllers;

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.dto.SaldoResponse;
import es.deusto.banca_online.services.CuentaService;
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

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
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
}