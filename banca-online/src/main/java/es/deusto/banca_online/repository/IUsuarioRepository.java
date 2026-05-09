package es.deusto.banca_online.repository;

import es.deusto.banca_online.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/**
 * Repositorio para la entidad Usuario.
 * Administra las credenciales de acceso y la relación entre las cuentas 
 * de usuario y sus perfiles de cliente. Es fundamental para el proceso de login.
 */
@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {
    /**
     * Busca un usuario por su dirección de correo electrónico.
     * @param email Email único registrado en el sistema.
     * @return Un Optional con el Usuario si existe.
     */
    Optional<Usuario> findByEmail(String email);
    /**
     * Comprueba si un correo electrónico ya está registrado en el sistema.
     * @param email Email a verificar.
     * @return true si el email ya está en uso, false de lo contrario.
     */
    boolean existsByEmail(String email);

    // Buscar usuario por el ID del cliente asociado
    /**
     * Localiza la cuenta de usuario vinculada a un perfil de cliente específico.
     * @param clienteId Identificador único del cliente.
     * @return Un Optional con el Usuario asociado a dicho cliente.
     */
    Optional<Usuario> findByClienteId(Long clienteId);
}
