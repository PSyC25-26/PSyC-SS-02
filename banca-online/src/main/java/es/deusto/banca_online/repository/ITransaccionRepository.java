package es.deusto.banca_online.repository;
import es.deusto.banca_online.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ITransaccionRepository extends JpaRepository<Transaccion, Long> {
    List<Transaccion> findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(Long origenId, Long destinoId);
}