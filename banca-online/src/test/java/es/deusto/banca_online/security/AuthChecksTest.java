package es.deusto.banca_online.security;

import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.Cuenta;
import es.deusto.banca_online.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthChecks.
 * Cubre las ramas de validacion de permisos: admin, propietario, no propietario.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthChecksTest {

    private static final Logger log = LoggerFactory.getLogger(AuthChecksTest.class);

    @Mock
    private Authentication authentication;

    private AuthChecks authChecks;
    private Usuario usuarioCliente;
    private Cliente cliente;
    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        authChecks = new AuthChecks();

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan");

        usuarioCliente = new Usuario();
        usuarioCliente.setEmail("juan@test.com");
        usuarioCliente.setCliente(cliente);

        cuenta = new Cuenta();
        cuenta.setId(10L);
        cuenta.setNumeroCuenta("ES-TEST-001");
        cuenta.setCliente(cliente);
    }

    // ===================== isAdmin =====================

    @Test
    void isAdmin_userIsAdmin_retornaTrue() {
        log.info("Test: isAdmin con usuario ADMIN debe retornar true");
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(authentication).getAuthorities();

        assertTrue(authChecks.isAdmin(authentication));
        log.info("Test pasado: ADMIN reconocido correctamente");
    }

    @Test
    void isAdmin_userIsCliente_retornaFalse() {
        log.info("Test: isAdmin con usuario CLIENTE debe retornar false");
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        doReturn(authorities).when(authentication).getAuthorities();

        assertFalse(authChecks.isAdmin(authentication));
        log.info("Test pasado: CLIENTE no es ADMIN");
    }

    @Test
    void isAdmin_userSinRoles_retornaFalse() {
        log.info("Test: isAdmin sin roles debe retornar false");
        doReturn(List.of()).when(authentication).getAuthorities();

        assertFalse(authChecks.isAdmin(authentication));
        log.info("Test pasado: sin roles no es ADMIN");
    }

    // ===================== clienteIdOrNull =====================

    @Test
    void clienteIdOrNull_conClienteVinculado_retornaId() {
        log.info("Test: clienteIdOrNull con cliente vinculado retorna su id");
        when(authentication.getPrincipal()).thenReturn(usuarioCliente);

        Long id = authChecks.clienteIdOrNull(authentication);

        assertEquals(1L, id);
        log.info("Test pasado: id={} retornado", id);
    }

    @Test
    void clienteIdOrNull_sinCliente_retornaNull() {
        log.info("Test: clienteIdOrNull sin cliente vinculado retorna null");
        Usuario adminPuro = new Usuario();
        adminPuro.setEmail("admin@banca.com");
        adminPuro.setCliente(null);
        when(authentication.getPrincipal()).thenReturn(adminPuro);

        assertNull(authChecks.clienteIdOrNull(authentication));
        log.info("Test pasado: null retornado para admin puro");
    }

    // ===================== assertOwnsCliente =====================

    @Test
    void assertOwnsCliente_userEsAdmin_permitido() {
        log.info("Test: assertOwnsCliente con ADMIN no lanza excepcion");
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(authentication).getAuthorities();

        assertDoesNotThrow(() -> authChecks.assertOwnsCliente(authentication, 99L));
        log.info("Test pasado: ADMIN puede acceder a cualquier cliente");
    }

    @Test
    void assertOwnsCliente_userEsPropietario_permitido() {
        log.info("Test: assertOwnsCliente con cliente propietario no lanza excepcion");
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(usuarioCliente);

        assertDoesNotThrow(() -> authChecks.assertOwnsCliente(authentication, 1L));
        log.info("Test pasado: cliente puede acceder a sus propios datos");
    }

    @Test
    void assertOwnsCliente_userNoEsPropietario_lanzaAccessDenied() {
        log.info("Test: assertOwnsCliente con cliente no propietario lanza AccessDenied");
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(usuarioCliente);

        assertThrows(AccessDeniedException.class,
                () -> authChecks.assertOwnsCliente(authentication, 99L));
        log.info("Test pasado: AccessDenied al intentar acceder a cliente ajeno");
    }

    @Test
    void assertOwnsCliente_userSinClienteId_lanzaAccessDenied() {
        log.info("Test: assertOwnsCliente con usuario sin clienteId lanza AccessDenied");
        Usuario adminPuro = new Usuario();
        adminPuro.setCliente(null);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(adminPuro);

        assertThrows(AccessDeniedException.class,
                () -> authChecks.assertOwnsCliente(authentication, 1L));
        log.info("Test pasado: AccessDenied si clienteId es null");
    }

    // ===================== assertOwnsCuenta =====================

    @Test
    void assertOwnsCuenta_userEsAdmin_permitido() {
        log.info("Test: assertOwnsCuenta con ADMIN no lanza excepcion");
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(authentication).getAuthorities();

        assertDoesNotThrow(() -> authChecks.assertOwnsCuenta(authentication, cuenta));
        log.info("Test pasado: ADMIN puede acceder a cualquier cuenta");
    }

    @Test
    void assertOwnsCuenta_userEsPropietarioDeLaCuenta_permitido() {
        log.info("Test: assertOwnsCuenta con propietario no lanza excepcion");
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(usuarioCliente);

        assertDoesNotThrow(() -> authChecks.assertOwnsCuenta(authentication, cuenta));
        log.info("Test pasado: cliente puede acceder a su cuenta");
    }

    @Test
    void assertOwnsCuenta_userNoEsPropietario_lanzaAccessDenied() {
        log.info("Test: assertOwnsCuenta con cliente no propietario lanza AccessDenied");
        Cliente otroCliente = new Cliente();
        otroCliente.setId(2L);
        Usuario otroUsuario = new Usuario();
        otroUsuario.setCliente(otroCliente);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(otroUsuario);

        assertThrows(AccessDeniedException.class,
                () -> authChecks.assertOwnsCuenta(authentication, cuenta));
        log.info("Test pasado: AccessDenied al intentar acceder a cuenta ajena");
    }

    @Test
    void assertOwnsCuenta_userSinClienteId_lanzaAccessDenied() {
        log.info("Test: assertOwnsCuenta con usuario sin clienteId lanza AccessDenied");
        Usuario adminPuro = new Usuario();
        adminPuro.setCliente(null);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(adminPuro);

        assertThrows(AccessDeniedException.class,
                () -> authChecks.assertOwnsCuenta(authentication, cuenta));
        log.info("Test pasado: AccessDenied si clienteId es null sobre cuenta");
    }
}
