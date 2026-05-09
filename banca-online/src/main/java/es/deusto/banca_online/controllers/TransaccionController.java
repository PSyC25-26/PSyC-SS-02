package es.deusto.banca_online.controllers;

import es.deusto.banca_online.dto.TransaccionResponse;
import es.deusto.banca_online.services.TransaccionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * Controlador para la consulta de movimientos y transacciones.
 */
@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {
    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    /**
     * Obtiene el historial de movimientos de una cuenta.
     * @param cuentaId Identificador único de la cuenta.
     * @return Lista de transacciones (depósitos, retiros, transferencias) ordenadas por fecha.
     */
    @GetMapping("/{cuentaId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
    public ResponseEntity<List<TransaccionResponse>> verHistorial(@PathVariable Long cuentaId) {
        // Aquí podrías añadir una validación extra para asegurar que el cliente es dueño de la cuentaId
        return ResponseEntity.ok(transaccionService.obtenerHistorial(cuentaId));
    }
}