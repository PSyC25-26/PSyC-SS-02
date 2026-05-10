package es.deusto.banca_online.security;

/**
 * @file UserDetailsServiceImpl.java
 * @brief Implementación de UserDetailsService que carga usuarios desde la base de datos por email.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.entity.Usuario;
import es.deusto.banca_online.repository.IUsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación personalizada del servicio de detalles de usuario de Spring Security.
 * Se encarga de recuperar los datos de autenticación desde la base de datos 
 * utilizando el email como identificador principal.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IUsuarioRepository usuarioRepository;

    /**
     * Constructor para la inyección de dependencias del repositorio de usuarios.
     * @param usuarioRepository Repositorio que gestiona la persistencia de Usuario.
     */
    public UserDetailsServiceImpl(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Localiza un usuario en la base de datos basándose en el nombre de usuario (email).
     * @param email El correo electrónico del usuario a autenticar.
     * @return UserDetails Objeto que Spring Security utiliza para la validación.
     * @throws UsernameNotFoundException Si no existe ningún usuario con el email proporcionado.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}
