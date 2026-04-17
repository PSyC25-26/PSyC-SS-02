package es.deusto.banca_online.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.deusto.banca_online.dto.*;
import es.deusto.banca_online.services.CuentaService;
import es.deusto.banca_online.services.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integracion: verifican que el controller HTTP, la seguridad
 * y la capa de servicio se integran correctamente.
 * Test de remoteness.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CuentaControllerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(CuentaControllerIntegrationTest.class);

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CuentaService cuentaService;
    @MockitoBean private TransferService transferService;

    private CuentaResponse cuentaResponse;

    @BeforeEach
    void setUp() {
        cuentaResponse = new CuentaResponse();
        cuentaResponse.setId(1L);
        cuentaResponse.setNumeroCuenta("ES1234567890ABCDEF12");
        cuentaResponse.setSaldo(500.0);
        cuentaResponse.setTipoCuenta("CORRIENTE");
        cuentaResponse.setClienteId(1L);
        cuentaResponse.setFechaCreacion(LocalDateTime.now());
    }

    // ===================== POST /api/cuentas =====================

    /** TEST DE REMOTENESS: llama al servidor real a traves de HTTP (MockMvc) */
    @Test
    @WithMockUser(roles = "ADMIN")
    void crearCuenta_adminValido_retorna201() throws Exception {
        log.info("Test integracion (remoteness): POST /api/cuentas como ADMIN");
        when(cuentaService.crearCuenta(any())).thenReturn(cuentaResponse);

        CuentaRequest request = new CuentaRequest();
        request.setClienteId(1L);
        request.setTipoCuenta("CORRIENTE");
        request.setSaldoInicial(500.0);

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCuenta").value("ES1234567890ABCDEF12"))
                .andExpect(jsonPath("$.saldo").value(500.0))
                .andExpect(jsonPath("$.tipoCuenta").value("CORRIENTE"));

        log.info("Test integracion pasado: cuenta creada via HTTP");
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void crearCuenta_rolCliente_retorna403() throws Exception {
        log.info("Test integracion: POST /api/cuentas como CLIENTE debe retornar 403");
        CuentaRequest request = new CuentaRequest();
        request.setClienteId(1L);
        request.setTipoCuenta("CORRIENTE");

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        log.info("Test integracion pasado: 403 para CLIENTE");
    }

    @Test
    void crearCuenta_sinAutenticacion_retorna401() throws Exception {
        log.info("Test integracion: POST /api/cuentas sin autenticacion debe retornar 401");
        CuentaRequest request = new CuentaRequest();
        request.setClienteId(1L);
        request.setTipoCuenta("CORRIENTE");

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        log.info("Test integracion pasado: 401 sin autenticacion");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearCuenta_clienteNoExiste_retorna404() throws Exception {
        log.info("Test integracion: POST /api/cuentas cliente inexistente retorna 404");
        when(cuentaService.crearCuenta(any()))
                .thenThrow(new RuntimeException("Cliente no encontrado con id: 99"));

        CuentaRequest request = new CuentaRequest();
        request.setClienteId(99L);
        request.setTipoCuenta("CORRIENTE");

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        log.info("Test integracion pasado: 404 para cliente inexistente");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearCuenta_tipoInvalido_retorna400() throws Exception {
        log.info("Test integracion: POST /api/cuentas tipo invalido retorna 400");
        when(cuentaService.crearCuenta(any()))
                .thenThrow(new IllegalArgumentException("Tipo invalido"));

        CuentaRequest request = new CuentaRequest();
        request.setClienteId(1L);
        request.setTipoCuenta("TIPO_MALO");

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        log.info("Test integracion pasado: 400 para tipo invalido");
    }

    // ===================== GET /api/cuentas?clienteId=X =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerCuentas_adminConClienteId_retornaLista() throws Exception {
        log.info("Test integracion: GET /api/cuentas?clienteId=1 como ADMIN");
        when(cuentaService.obtenerCuentasPorCliente(1L)).thenReturn(List.of(cuentaResponse));

        mockMvc.perform(get("/api/cuentas")
                        .param("clienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroCuenta").value("ES1234567890ABCDEF12"));

        log.info("Test integracion pasado: lista de cuentas retornada");
    }

    // ===================== POST /api/cuentas/depositar =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void depositar_montoValido_retorna200() throws Exception {
        log.info("Test integracion: POST /api/cuentas/depositar monto valido");
        when(cuentaService.depositarDinero(eq(1L), eq(200.0), any())).thenReturn(cuentaResponse);

        DepositoRequest req = new DepositoRequest();
        req.setCuentaId(1L);
        req.setMonto(200.0);

        mockMvc.perform(post("/api/cuentas/depositar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        log.info("Test integracion pasado: deposito realizado");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void depositar_montoInvalido_retorna400() throws Exception {
        log.info("Test integracion: POST /api/cuentas/depositar monto invalido");
        when(cuentaService.depositarDinero(anyLong(), anyDouble(), any()))
                .thenThrow(new IllegalArgumentException("Monto invalido"));

        DepositoRequest req = new DepositoRequest();
        req.setCuentaId(1L);
        req.setMonto(100.0);

        mockMvc.perform(post("/api/cuentas/depositar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        log.info("Test integracion pasado: 400 por monto invalido");
    }

    // ===================== POST /api/cuentas/retirar =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void retirar_saldoSuficiente_retorna200() throws Exception {
        log.info("Test integracion: POST /api/cuentas/retirar con saldo suficiente");
        when(cuentaService.retirarDinero(eq(1L), eq(100.0), any())).thenReturn(cuentaResponse);

        RetiroRequest req = new RetiroRequest();
        req.setCuentaId(1L);
        req.setMonto(100.0);

        mockMvc.perform(post("/api/cuentas/retirar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        log.info("Test integracion pasado: retiro realizado");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void retirar_saldoInsuficiente_retorna400() throws Exception {
        log.info("Test integracion: POST /api/cuentas/retirar saldo insuficiente");
        when(cuentaService.retirarDinero(anyLong(), anyDouble(), any()))
                .thenThrow(new IllegalArgumentException("Saldo insuficiente"));

        RetiroRequest req = new RetiroRequest();
        req.setCuentaId(1L);
        req.setMonto(99999.0);

        mockMvc.perform(post("/api/cuentas/retirar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        log.info("Test integracion pasado: 400 por saldo insuficiente");
    }

    // ===================== POST /api/cuentas/transferir =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void transferir_datosValidos_retorna200() throws Exception {
        log.info("Test integracion: POST /api/cuentas/transferir datos validos");
        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigen("ES-ORIGEN-001");
        dto.setCuentaDestino("ES-DESTINO-002");
        dto.setCantidad(100.0);

        when(transferService.transferirDinero(any(), any())).thenReturn(dto);

        mockMvc.perform(post("/api/cuentas/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(100.0));

        log.info("Test integracion pasado: transferencia realizada via HTTP");
    }

    // ===================== GET /api/cuentas/saldo/{id} =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void verSaldo_cuentaExistente_retorna200() throws Exception {
        log.info("Test integracion: GET /api/cuentas/saldo/1");
        when(cuentaService.obtenerSaldo(eq(1L), any())).thenReturn(500.0);

        mockMvc.perform(get("/api/cuentas/saldo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(500.0));

        log.info("Test integracion pasado: saldo retornado via HTTP");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void verSaldo_cuentaNoExistente_retorna404() throws Exception {
        log.info("Test integracion: GET /api/cuentas/saldo/99 cuenta no encontrada");
        when(cuentaService.obtenerSaldo(eq(99L), any()))
                .thenThrow(new RuntimeException("Cuenta no encontrada"));

        mockMvc.perform(get("/api/cuentas/saldo/99"))
                .andExpect(status().isNotFound());

        log.info("Test integracion pasado: 404 para cuenta inexistente");
    }
}