package es.deusto.banca_online.repository;

/**
 * @file IClienteRepository.java
 * @brief Repositorio JPA para la entidad Cliente, con consultas por email y DNI.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// Repositorio: Se encarga de acceder a la BD
/**
 * Repositorio para la entidad Cliente.
 * Proporciona métodos para realizar operaciones CRUD y consultas personalizadas 
 * sobre la tabla de clientes, incluyendo búsquedas por DNI y relaciones con Usuario.
 */
@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Long> {

    // NOTA: Sabe que hacer por el nombre del metodo. Es por
    // ello que debe coincidir los nombres de los campos tambien.


    // Buscar cliente por email via relacion Usuario (fuente de verdad)
    /**
     * Busca un cliente a través del email de su cuenta de usuario asociada.
     * @param email Dirección de correo electrónico del usuario.
     * @return Un Optional con el Cliente si se encuentra, o vacío en caso contrario.
     */
    @Query("SELECT c FROM Cliente c JOIN Usuario u ON u.cliente.id = c.id WHERE u.email = :email")
    Optional<Cliente> findByEmail(@Param("email") String email);

    // Buscar cliente por DNI (util para validar que no exista)
    /**
     * Busca un cliente a través del DNI de su cuenta de usuario asociada.
     * @param dni DNI del usuario.
     * @return Un Optional con el Cliente si se encuentra, o vacío en caso contrario.
     */
    Optional<Cliente> findByDni(String dni);

    // Verificar si ya existe un email via Usuario (más eficiente que findByEmail)
    
    /**
     * Verifica si existe un cliente asociado a un email determinado.
     * @param email Email a comprobar.
     * @return true si el cliente existe, false de lo contrario.
     */
    @Query("SELECT COUNT(c) > 0 FROM Cliente c JOIN Usuario u ON u.cliente.id = c.id WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    // Verificar si ya existe un dni (más eficiente que findByDni)
    /**
     * Verifica si existe un cliente asociado a un dni determinado.
     * @param dni dni a comprobar.
     * @return true si el cliente existe, false de lo contrario.
     */
    boolean existsByDni(String dni);

}
