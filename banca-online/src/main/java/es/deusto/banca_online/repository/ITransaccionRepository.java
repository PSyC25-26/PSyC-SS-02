package es.deusto.banca_online.repository;
import es.deusto.banca_online.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITransaccionRepository extends JpaRepository<Transaccion, Long> {
}
