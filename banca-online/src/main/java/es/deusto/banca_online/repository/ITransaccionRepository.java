package es.deusto.banca_online.repository;

/**
 * @file ITransaccionRepository.java
 * @brief Repositorio JPA para la entidad Transaccion, con consultas por cuenta origen y destino.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio para la entidad Transaccion.
 * Gestiona el historial de movimientos bancarios, permitiendo recuperar
 * transferencias, depósitos y retiros realizados en el sistema.
 */
@Repository
public interface ITransaccionRepository extends JpaRepository<Transaccion, Long> {
    /**
     * Recupera el historial completo de transacciones asociadas a una cuenta,
     * ya sea como cuenta de origen o como cuenta de destino.
     * Los resultados se devuelven ordenados de forma cronológica inversa (más recientes primero).
     * * @param origenId Identificador de la cuenta que emite el dinero.
     * @param destinoId Identificador de la cuenta que recibe el dinero.
     * @return Lista de transacciones ordenadas por fecha descendente.
     */
    List<Transaccion> findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(Long origenId, Long destinoId);
}