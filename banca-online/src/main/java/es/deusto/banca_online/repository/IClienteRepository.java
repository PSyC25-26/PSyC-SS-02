package es.deusto.banca_online.repository;

import es.deusto.banca_online.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// Repositorio: Se encarga de acceder a la BD
@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Long> {

    // NOTA: Sabe que hacer por el nombre del metodo. Es por
    // ello que debe coincidir los nombres de los campos tambien.


    // Buscar cliente por email via relacion Usuario (fuente de verdad)
    @Query("SELECT c FROM Cliente c JOIN Usuario u ON u.cliente.id = c.id WHERE u.email = :email")
    Optional<Cliente> findByEmail(@Param("email") String email);

    // Buscar cliente por DNI (util para validar que no exista)
    Optional<Cliente> findByDni(String dni);

    // Verificar si ya existe un email via Usuario (más eficiente que findByEmail)
    @Query("SELECT COUNT(c) > 0 FROM Cliente c JOIN Usuario u ON u.cliente.id = c.id WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    // Verificar si ya existe un dni (más eficiente que findByDni)
    boolean existsByDni(String dni);

}
