package es.deusto.banca_online.security;

import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.ERol;
import es.deusto.banca_online.entity.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para JwtAuthFilter.
 * Cubre los caminos: sin token, token invalido, token valido.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtAuthFilterTest {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilterTest.class);

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtUtils, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_sinHeaderAuthorization_pasaAlSiguienteFiltro() throws Exception {
        log.info("Test: sin cabecera Authorization, el filtro pasa el control al siguiente");
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        log.info("Test pasado: sin token, contexto no autenticado");
    }

    @Test
    void doFilter_headerSinBearer_pasaAlSiguienteFiltro() throws Exception {
        log.info("Test: cabecera sin prefijo Bearer, el filtro pasa al siguiente");
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        log.info("Test pasado: header Basic ignorado");
    }

    @Test
    void doFilter_tokenInvalido_pasaSinAutenticar() throws Exception {
        log.info("Test: token JWT invalido no autentica al usuario");
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido");
        when(jwtUtils.esValido("token-invalido")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        log.info("Test pasado: token invalido no autentica");
    }

    @Test
    void doFilter_tokenValido_autenticaUsuario() throws Exception {
        log.info("Test: token JWT valido autentica al usuario en el contexto");

        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Usuario usuario = new Usuario();
        usuario.setEmail("juan@test.com");
        usuario.setRol(ERol.CLIENTE);
        usuario.setCliente(cliente);

        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token-xyz");
        when(jwtUtils.esValido("valid-token-xyz")).thenReturn(true);
        when(jwtUtils.extraerEmail("valid-token-xyz")).thenReturn("juan@test.com");
        when(jwtUtils.extraerRol("valid-token-xyz")).thenReturn("CLIENTE");
        when(userDetailsService.loadUserByUsername("juan@test.com")).thenReturn(usuario);

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(usuario, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain, times(1)).doFilter(request, response);
        log.info("Test pasado: contexto autenticado con usuario");
    }

    @Test
    void doFilter_tokenValidoConRolAdmin_autenticaConRolAdmin() throws Exception {
        log.info("Test: token con rol ADMIN autentica con autoridad ROLE_ADMIN");

        Usuario admin = new Usuario();
        admin.setEmail("admin@banca.com");
        admin.setRol(ERol.ADMIN);
        admin.setCliente(null);

        when(request.getHeader("Authorization")).thenReturn("Bearer admin-token");
        when(jwtUtils.esValido("admin-token")).thenReturn(true);
        when(jwtUtils.extraerEmail("admin-token")).thenReturn("admin@banca.com");
        when(jwtUtils.extraerRol("admin-token")).thenReturn("ADMIN");
        when(userDetailsService.loadUserByUsername("admin@banca.com")).thenReturn(admin);

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        log.info("Test pasado: ADMIN autenticado con ROLE_ADMIN");
    }
}
