package es.deusto.banca_online.security;

import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.ERol;
import es.deusto.banca_online.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "app.jwt.secret=dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLWp3dC1oczI1NQo=",
    "app.jwt.expiration-ms=86400000"
})
class JwtUtilsTest {

    private static final Logger log = LoggerFactory.getLogger(JwtUtilsTest.class);

    /**
     * Utilidad de JWT inyectada para pruebas.
     * Se configura mediante {@link TestPropertySource} con una clave secreta 
     * y tiempo de expiración específicos para el entorno de test.
     */
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Test de generación de token para perfil Administrador.
     * Verifica que cuando un administrador inicia sesión, el token generado
     * contenga el rol 'ADMIN' y que el claim 'clienteId' sea nulo, ya que 
     * los administradores no tienen un perfil de cliente asociado.
     */
    @Test
    void generarToken_admin_clienteIdClaimEsNull() {
        log.info("Test: generarToken con ADMIN deveolver clienteId null");

        Usuario admin = new Usuario();
        admin.setEmail("admin@test.com");
        admin.setRol(ERol.ADMIN);
        admin.setCliente(null); // ADMIN no tiene cliente

        String token = jwtUtils.generarToken(admin);

        assertNotNull(token);
        assertEquals("admin@test.com", jwtUtils.extraerEmail(token));
        assertEquals("ADMIN", jwtUtils.extraerRol(token));
        assertNull(jwtUtils.extraerClienteId(token), "ADMIN debe tener clienteId null en el token");

        log.info("Test PASADO: ADMIN token tiene clienteId=null");
    }

    /**
     * Test de generación de token para perfil Cliente.
     * Valida que el token incluya correctamente el 'clienteId' en los claims.
     * Esto es crítico para que el sistema identifique qué cuentas pertenecen 
     * al usuario autenticado en peticiones posteriores.
     */
    @Test
    void generarToken_cliente_clienteIdClaimCorrecto() {
        log.info("Test: generarToken con CLIENTE deveolver clienteId correcto");

        Cliente cliente = new Cliente();
        cliente.setId(42L);

        Usuario usuario = new Usuario();
        usuario.setEmail("cliente@test.com");
        usuario.setRol(ERol.CLIENTE);
        usuario.setCliente(cliente);

        String token = jwtUtils.generarToken(usuario);

        assertNotNull(token);
        assertEquals("cliente@test.com", jwtUtils.extraerEmail(token));
        assertEquals("CLIENTE", jwtUtils.extraerRol(token));
        assertEquals(42L, jwtUtils.extraerClienteId(token), "CLIENTE debe tener clienteId correcto en el token");

        log.info("Test PASADO: CLIENTE token tiene clienteId=42");
    }

    /**
     * Test de integridad: Validación de token correcto.
     * Asegura que un token recién generado con la clave secreta del sistema
     * sea reconocido como válido por los filtros de seguridad.
     */
    @Test
    void esValido_tokenValido_retornaTrue() {
        Usuario usuario = new Usuario();
        usuario.setEmail("test@test.com");
        usuario.setRol(ERol.CLIENTE);

        String token = jwtUtils.generarToken(usuario);

        assertTrue(jwtUtils.esValido(token));
    }

    /**
     * Test de seguridad: Rechazo de tokens malformados o falsificados.
     * Verifica que cualquier cadena que no cumpla el estándar JWT o que no
     * esté firmada por nuestra clave secreta sea rechazada (false).
     */
    @Test
    void esValido_tokenInvalido_retornaFalse() {
        assertFalse(jwtUtils.esValido("token.invalido"));
    }
}
