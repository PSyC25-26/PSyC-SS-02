package es.deusto.banca_online.repository;

import es.deusto.banca_online.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICuentaRepository extends JpaRepository<Cuenta, Long> {
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);
    List<Cuenta> findByClienteId(Long clienteId);
    boolean existsByNumeroCuenta(String numeroCuenta);

    // Filtro para buscar por cliente y coger sus cuentas activas
    List<Cuenta> findByClienteIdAndActivaTrue(Long clienteId);

    //Buscar por cuentas activas
    Optional<Cuenta> findByIdAndActivaTrue(Long id);
}