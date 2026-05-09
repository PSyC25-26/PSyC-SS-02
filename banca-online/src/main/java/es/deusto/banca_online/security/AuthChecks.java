package es.deusto.banca_online.security;

import es.deusto.banca_online.entity.Cuenta;
import es.deusto.banca_online.entity.Usuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Componente de soporte para validaciones de propiedad y permisos.
 * Proporciona métodos para verificar si el usuario autenticado es dueño
 * de los recursos (cuentas/perfiles) que intenta manipular.
 */
@Component
public class AuthChecks {

    /**
     * Verifica si el usuario autenticado posee el rol de administrador.
     * Evalúa la lista de autoridades (roles) concedidas en el objeto de autenticación.
     * * @param authentication Objeto que contiene los detalles de seguridad del usuario actual.
     * @return true si el usuario tiene el rol 'ROLE_ADMIN', false en caso contrario.
     */
    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Recupera el identificador del cliente asociado al usuario autenticado.
     * Realiza un casting del principal de Spring Security a la entidad Usuario del sistema.
     * * @param authentication Objeto que contiene el principal del usuario logueado.
     * @return El ID del cliente si el usuario tiene un perfil de cliente vinculado, 
     * o null si es un usuario sin perfil (como un administrador puro).
     */
    public Long clienteIdOrNull(Authentication authentication) {
        Usuario principal = (Usuario) authentication.getPrincipal();
        return principal.getCliente() != null ? principal.getCliente().getId() : null;
    }

    /**
     * Valida que el usuario autenticado tenga derecho a acceder a los datos de un cliente.
     * Permite el acceso si:
     * 1. El usuario es administrador.
     * 2. El ID del cliente solicitado coincide con el ID del propio usuario autenticado.
     * * @param authentication Datos del usuario que realiza la petición.
     * @param clienteId Identificador del cliente al que se intenta acceder.
     * @throws AccessDeniedException Si el usuario no es admin y el ID no coincide, 
     * bloqueando el acceso al recurso.
     */
    public void assertOwnsCliente(Authentication authentication, Long clienteId) {
        if (isAdmin(authentication)) {
            return;
        }
        Long idClienteAutenticado = clienteIdOrNull(authentication);
        if (idClienteAutenticado == null || !idClienteAutenticado.equals(clienteId)) {
            throw new AccessDeniedException("No tiene permiso para acceder a este recurso");
        }
    }

    /**
     * Lanza una excepción si el usuario autenticado no es administrador
     * ni es el propietario de la cuenta especificada.
     * @param authentication Datos del usuario logueado.
     * @param cuenta Entidad de la cuenta a verificar.
     * @throws AccessDeniedException Si no se cumplen los permisos de propiedad.
     */
    public void assertOwnsCuenta(Authentication authentication, Cuenta cuenta) {
        if (isAdmin(authentication)) {
            return;
        }
        Long idClienteAutenticado = clienteIdOrNull(authentication);
        if (idClienteAutenticado == null ||
                !idClienteAutenticado.equals(cuenta.getCliente().getId())) {
            throw new AccessDeniedException("No tiene permiso sobre esta cuenta");
        }
    }
}
