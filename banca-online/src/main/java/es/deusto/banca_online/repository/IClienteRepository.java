package es.deusto.banca_online.repository;

import es.deusto.banca_online.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// Repositorio: Se encarga de acceder a la BD
@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Long> {

    // NOTA: Sabe que hacer por el nombre del metodo. Es por
    // ello que debe coincidir los nombres de los campos tambien.


    // Buscar cliente por email (util para validar que no exista)
    Optional<Cliente> findByEmail(String email);

    // Buscar cliente por DNI (util para validar que no exista)
    Optional<Cliente> findByDni(String dni);

    // Verificar si ya existe un email (más eficiente que findByEmail)
    boolean existsByEmail(String email);

    // Verificar si ya existe un dni (más eficiente que findByDni)
    boolean existsByDni(String dni);

}
