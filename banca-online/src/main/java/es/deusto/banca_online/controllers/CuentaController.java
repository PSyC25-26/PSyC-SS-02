package es.deusto.banca_online.controllers;

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.services.CuentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    //POST /cuentas
    @PostMapping
    public ResponseEntity<CuentaResponse> crearCuenta(@RequestBody CuentaRequest request) {
        CuentaResponse response = cuentaService.crearCuenta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //GET /cuentas?clienteId=X
    @GetMapping
    public ResponseEntity<List<CuentaResponse>> obtenerCuentas(@RequestParam Long clienteId) {
        return ResponseEntity.ok(cuentaService.obtenerCuentasPorCliente(clienteId));
    }
}