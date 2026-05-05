package es.deusto.banca_online.security;

import es.deusto.banca_online.entity.Cuenta;
import es.deusto.banca_online.entity.Usuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthChecks {

    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public Long clienteIdOrNull(Authentication authentication) {
        Usuario principal = (Usuario) authentication.getPrincipal();
        return principal.getCliente() != null ? principal.getCliente().getId() : null;
    }

    public void assertOwnsCliente(Authentication authentication, Long clienteId) {
        if (isAdmin(authentication)) {
            return;
        }
        Long idClienteAutenticado = clienteIdOrNull(authentication);
        if (idClienteAutenticado == null || !idClienteAutenticado.equals(clienteId)) {
            throw new AccessDeniedException("No tiene permiso para acceder a este recurso");
        }
    }

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
