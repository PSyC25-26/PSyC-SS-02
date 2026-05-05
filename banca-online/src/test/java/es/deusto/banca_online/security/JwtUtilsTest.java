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

    @Autowired
    private JwtUtils jwtUtils;

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

    @Test
    void esValido_tokenValido_retornaTrue() {
        Usuario usuario = new Usuario();
        usuario.setEmail("test@test.com");
        usuario.setRol(ERol.CLIENTE);

        String token = jwtUtils.generarToken(usuario);

        assertTrue(jwtUtils.esValido(token));
    }

    @Test
    void esValido_tokenInvalido_retornaFalse() {
        assertFalse(jwtUtils.esValido("token.invalido"));
    }
}
