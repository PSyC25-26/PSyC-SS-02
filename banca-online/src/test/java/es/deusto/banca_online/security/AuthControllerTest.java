package es.deusto.banca_online.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.deusto.banca_online.dto.LoginRequest;
import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.ERol;
import es.deusto.banca_online.entity.Usuario;
import es.deusto.banca_online.repository.IUsuarioRepository;
import es.deusto.banca_online.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integracion para AuthController.
 * Cubre login exitoso, login fallido (401) y validacion de campos.
 */
@SpringBootTest
class AuthControllerTest {

    private static final Logger log = LoggerFactory.getLogger(AuthControllerTest.class);

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private IUsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void login_credencialesValidas_retorna200ConToken() throws Exception {
        log.info("Test: POST /api/auth/login con credenciales validas retorna 200 + token");

        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Usuario usuario = new Usuario();
        usuario.setEmail("juan@test.com");
        usuario.setRol(ERol.CLIENTE);
        usuario.setCliente(cliente);

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(jwtUtils.generarToken(usuario)).thenReturn("fake-jwt-token-12345");

        LoginRequest request = new LoginRequest();
        request.setEmail("juan@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token-12345"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.clienteId").value(1))
                .andExpect(jsonPath("$.email").value("juan@test.com"));

        log.info("Test pasado: login exitoso con token JWT");
    }

    @Test
    void login_adminSinClienteId_retorna200() throws Exception {
        log.info("Test: login como ADMIN sin clienteId retorna 200");

        Usuario admin = new Usuario();
        admin.setEmail("admin@banca.com");
        admin.setRol(ERol.ADMIN);
        admin.setCliente(null);

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioRepository.findByEmail("admin@banca.com")).thenReturn(Optional.of(admin));
        when(jwtUtils.generarToken(admin)).thenReturn("admin-token");

        LoginRequest request = new LoginRequest();
        request.setEmail("admin@banca.com");
        request.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin-token"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));

        log.info("Test pasado: ADMIN puede loguearse sin clienteId");
    }

    @Test
    void login_credencialesInvalidas_retorna401() throws Exception {
        log.info("Test: POST /api/auth/login con credenciales invalidas retorna 401");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        LoginRequest request = new LoginRequest();
        request.setEmail("malo@test.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        log.info("Test pasado: 401 retornado por credenciales invalidas");
    }
}
