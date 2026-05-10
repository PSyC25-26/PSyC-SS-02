package es.deusto.banca_online.repository;

/**
 * @file ICuentaRepository.java
 * @brief Repositorio JPA para la entidad Cuenta, con filtros por cliente y estado activo.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de Cuentas bancarias.
 * Gestiona la persistencia de las cuentas y permite filtrar por estado de actividad.
 */
@Repository
public interface ICuentaRepository extends JpaRepository<Cuenta, Long> {
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);
    List<Cuenta> findByClienteId(Long clienteId);
    boolean existsByNumeroCuenta(String numeroCuenta);

    // Filtro para buscar por cliente y coger sus cuentas activas
    /**
     * Recupera las cuentas activas asociadas a un cliente.
     * @param clienteId ID del cliente titular.
     * @return Lista de cuentas con estado activa = true.
     */
    List<Cuenta> findByClienteIdAndActivaTrue(Long clienteId);

    //Buscar por cuentas activas
    /**
     * Busca una cuenta específica solo si se encuentra en estado activo.
     * @param id Identificador de la cuenta.
     * @return Un Optional con la cuenta activa encontrada.
     */
    Optional<Cuenta> findByIdAndActivaTrue(Long id);
}