package es.deusto.banca_online.services;

/**
 * @file TransaccionService.java
 * @brief Servicio para la consulta y mapeo del historial de transacciones bancarias.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.dto.TransaccionResponse;
import es.deusto.banca_online.entity.Transaccion;
import es.deusto.banca_online.repository.ITransaccionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import es.deusto.banca_online.entity.ETipoTransaccion;

/**
 * Servicio para la consulta del historial de movimientos.
 * Transforma las entidades de transacción en respuestas legibles para el usuario,
 * incluyendo descripciones dinámicas según el tipo de movimiento.
 */
@Service
public class TransaccionService {
    /**
     * Atributo que proporciona acceso a las operaciones de persistencia 
     * en la base de datos para las transacciones.
     */
    private final ITransaccionRepository transaccionRepository;

    /**
     * Constructor para la inyección de dependencias de TransaccionService.
     * Inicializa el repositorio necesario para gestionar las consultas del historial
     * de movimientos bancarios.
     * * @param transaccionRepository Repositorio encargado de la persistencia de transacciones.
     */
    public TransaccionService(ITransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    /**
     * Obtiene todos los movimientos (entrantes y salientes) de una cuenta específica.
     * @param cuentaId ID de la cuenta a consultar.
     * @return Lista de TransaccionResponse ordenadas por fecha descendente.
     */
    public List<TransaccionResponse> obtenerHistorial(Long cuentaId) {
        List<Transaccion> transacciones = transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(cuentaId, cuentaId);

        return transacciones.stream().map(t -> {
            TransaccionResponse res = new TransaccionResponse();
            res.setId(t.getId());
            res.setTotal(t.getTotal());
            res.setFecha(t.getFecha().toString());
            res.setTipo(t.getTipo().name());

            String numOrigen = t.getCuentaOrigen() != null ? t.getCuentaOrigen().getNumeroCuenta() : "N/A";
            String numDestino = t.getCuentaDestino() != null ? t.getCuentaDestino().getNumeroCuenta() : "N/A";

            res.setCuentaOrigenNum(numOrigen);
            res.setCuentaDestinoNum(numDestino);

            // Lógica de descripción inteligente
            if (t.getTipo() == ETipoTransaccion.TRANSFERENCIA) {
                if (t.getCuentaDestino() != null && t.getCuentaDestino().getId().equals(cuentaId)) {
                    res.setDescripcion("Transferencia RECIBIDA de " + numOrigen);
                } else {
                    res.setDescripcion("Transferencia ENVIADA a " + numDestino);
                }
            } else {
                res.setDescripcion(t.getDescripcion());
            }

            return res;
        }).collect(Collectors.toList());
    }
}