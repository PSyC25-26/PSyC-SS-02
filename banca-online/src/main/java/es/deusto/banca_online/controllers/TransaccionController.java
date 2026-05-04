package es.deusto.banca_online.controllers;

import es.deusto.banca_online.dto.TransaccionResponse;
import es.deusto.banca_online.services.TransaccionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {
    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @GetMapping("/{cuentaId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
    public ResponseEntity<List<TransaccionResponse>> verHistorial(@PathVariable Long cuentaId) {
        // Aquí podrías añadir una validación extra para asegurar que el cliente es dueño de la cuentaId
        return ResponseEntity.ok(transaccionService.obtenerHistorial(cuentaId));
    }
}