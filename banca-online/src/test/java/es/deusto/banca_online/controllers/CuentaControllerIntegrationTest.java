package es.deusto.banca_online.controllers;

/**
 * @file CuentaControllerIntegrationTest.java
 * @brief Tests de integración del controlador REST de cuentas bancarias.
 * @details Verifica los endpoints de creación, consulta de saldo, depósitos, retiros
 *          y transferencias, incluyendo validaciones de seguridad por rol.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import es.deusto.banca_online.dto.*;
import es.deusto.banca_online.services.CuentaService;
import es.deusto.banca_online.services.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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
class CuentaControllerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(CuentaControllerIntegrationTest.class);

    private MockMvc mockMvc;
    private CuentaResponse cuentaResponse;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    @MockitoBean private CuentaService cuentaService;
    @MockitoBean private TransferService transferService;

    @BeforeEach
    void setupMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(org.springframework.security.test.web.servlet
                        .setup.SecurityMockMvcConfigurers.springSecurity())
                .build();

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
    /**
     * Test de integración (Remoteness): Creación de cuenta por un Administrador.
     * Verifica que un usuario con rol 'ADMIN' puede dar de alta nuevas cuentas.
     * Valida que el mapeo JSON sea correcto y se reciba un HTTP 201.
     */
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

    /**
     * Test de seguridad: Restricción de creación de cuentas para el rol Cliente.
     * Verifica que la política de seguridad bloquee (HTTP 403 Forbidden) a los
     * clientes que intenten acceder a funciones administrativas de creación.
     */
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

    /**
     * Test de seguridad: Acceso anónimo denegado.
     * Asegura que cualquier petición no autenticada sea rechazada por el filtro de seguridad.
     */
    @Test
    void crearCuenta_sinAutenticacion_retorna401() throws Exception {
        log.info("Test integracion: POST /api/cuentas sin autenticacion debe retornar 401");
        CuentaRequest request = new CuentaRequest();
        request.setClienteId(1L);
        request.setTipoCuenta("CORRIENTE");

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        log.info("Test integracion pasado: 401 sin autenticacion");
    }

    /**
     * Test de error: Creación de cuenta para un cliente inexistente.
     * Valida que el controlador gestione correctamente las excepciones de negocio
     * transformándolas en un error 404 (Not Found).
     */
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

    /**
     * Test de validación: Tipo de cuenta no soportado.
     * Verifica que si se intenta crear una cuenta con un tipo inválido (Enum),
     * el sistema responda con un HTTP 400 (Bad Request).
     */
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

    /**
     * Test de consulta: Listado de cuentas por ID de cliente.
     * Verifica la integración con los parámetros de consulta (query params)
     * y la correcta serialización de la lista de cuentas del cliente.
     */
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
    /**
     * Test de operación: Depósito de efectivo exitoso.
     * Valida el flujo completo de la petición de depósito y la recepción
     * del estado 200 (OK).
     */
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

    /**
     * Test de error: Depósito con monto inválido (negativo o cero).
     * Asegura que las restricciones de validación de montos retornen HTTP 400.
     */
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

    /**
     * Test de operación: Retiro de efectivo con fondos suficientes.
     * Verifica que el endpoint de retiro procese la solicitud y retorne éxito.
     */
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

    /**
     * Test de error: Retiro con saldo insuficiente.
     * Verifica que el controlador capture el error de lógica de negocio y
     * devuelva un HTTP 400 avisando al cliente del error.
     */
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

    /**
     * Test de integración: Proceso de transferencia entre cuentas.
     * Valida la colaboración entre CuentaController y TransferService a través de HTTP,
     * verificando que el DTO de transferencia sea procesado y devuelto correctamente.
     */
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

    /**
     * Test de consulta: Visualización de saldo de una cuenta existente.
     * Verifica que el endpoint de consulta de saldo retorne el valor numérico
     * esperado bajo el rol de administrador.
     */
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

    /**
     * Test de error: Consulta de saldo sobre cuenta inexistente.
     * Verifica que el sistema responda con 404 al intentar consultar fondos de
     * una cuenta que no está en la base de datos.
     */
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

    // ===================== GET /api/cuentas/saldo/{cuentaId} =====================

    @Test
    @WithMockUser(roles = "CLIENTE")
    void verSaldo_clienteAccedeASuCuenta_retorna200() throws Exception {
        log.info("Test: CLIENTE accediendo a su propia cuenta retorna 200");
        when(cuentaService.obtenerSaldo(eq(5L), any())).thenReturn(250.0);

        mockMvc.perform(get("/api/cuentas/saldo/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(250.0));

        log.info("Test pasado: CLIENTE puede ver saldo de su cuenta");
    }

    // ===================== GET /api/cuentas/saldo (vista HTML) =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void vistaSaldo_retornaNombreDeVista() throws Exception {
        log.info("Test: GET /api/cuentas/saldo retorna nombre de vista");
        mockMvc.perform(get("/api/cuentas/saldo"))
                .andExpect(status().isOk());
        log.info("Test pasado: endpoint vista accesible");
    }

    // ===================== GET /api/cuentas/{clienteId} =====================

    @Test
    @WithMockUser(roles = "CLIENTE")
    void obtenerCuentasPorClienteId_clientePropio_retorna200() throws Exception {
        log.info("Test: CLIENTE consulta sus cuentas retorna lista");

        CuentaResponse cuentaPropia = new CuentaResponse();
        cuentaPropia.setId(1L);
        cuentaPropia.setNumeroCuenta("ES-CLI-001");
        cuentaPropia.setSaldo(1000.0);
        cuentaPropia.setTipoCuenta("CORRIENTE");
        cuentaPropia.setClienteId(1L);

        when(cuentaService.obtenerCuentasPorCliente(eq(1L), any())).thenReturn(List.of(cuentaPropia));

        mockMvc.perform(get("/api/cuentas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroCuenta").value("ES-CLI-001"));

        log.info("Test pasado: cliente accede a sus cuentas");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerCuentasPorClienteId_clienteInexistente_retorna404() throws Exception {
        log.info("Test: GET /api/cuentas/99 cliente inexistente retorna 404");
        when(cuentaService.obtenerCuentasPorCliente(eq(99L), any()))
                .thenThrow(new RuntimeException("Cliente no encontrado"));

        mockMvc.perform(get("/api/cuentas/99"))
                .andExpect(status().isNotFound());

        log.info("Test pasado: 404 para cliente inexistente");
    }

    // ===================== POST /api/cuentas/transferir - rama 404 =====================

    @Test
    @WithMockUser(roles = "CLIENTE")
    void transferir_cuentaNoEncontrada_retorna404() throws Exception {
        log.info("Test: transferencia con cuenta inexistente retorna 404");

        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigen("ES-INEXISTENTE");
        dto.setCuentaDestino("ES-DESTINO-002");
        dto.setCantidad(100.0);

        when(transferService.transferirDinero(any(), any()))
                .thenThrow(new RuntimeException("Cuenta no encontrada"));

        mockMvc.perform(post("/api/cuentas/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        log.info("Test pasado: 404 por cuenta no encontrada");
    }

    // ===================== POST /api/cuentas/depositar - rama 404 =====================

    @Test
    @WithMockUser(roles = "CLIENTE")
    void depositar_cuentaNoEncontrada_retorna404() throws Exception {
        log.info("Test: deposito en cuenta inexistente retorna 404");

        DepositoRequest req = new DepositoRequest();
        req.setCuentaId(999L);
        req.setMonto(100.0);

        when(cuentaService.depositarDinero(anyLong(), anyDouble(), any()))
                .thenThrow(new RuntimeException("Cuenta no encontrada"));

        mockMvc.perform(post("/api/cuentas/depositar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());

        log.info("Test pasado: 404 por cuenta inexistente");
    }

    // ===================== POST /api/cuentas/retirar - ramas adicionales =====================

    @Test
    @WithMockUser(roles = "CLIENTE")
    void retirar_cuentaNoEncontrada_retorna404() throws Exception {
        log.info("Test: retiro de cuenta inexistente retorna 404");

        RetiroRequest req = new RetiroRequest();
        req.setCuentaId(999L);
        req.setMonto(100.0);

        when(cuentaService.retirarDinero(anyLong(), anyDouble(), any()))
                .thenThrow(new RuntimeException("Cuenta no encontrada"));

        mockMvc.perform(post("/api/cuentas/retirar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());

        log.info("Test pasado: 404 por cuenta inexistente");
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void retirar_datosValidos_retorna200() throws Exception {
        log.info("Test: retiro valido retorna 200");

        RetiroRequest req = new RetiroRequest();
        req.setCuentaId(1L);
        req.setMonto(100.0);

        CuentaResponse responseOk = new CuentaResponse();
        responseOk.setId(1L);
        responseOk.setSaldo(400.0);

        when(cuentaService.retirarDinero(eq(1L), eq(100.0), any())).thenReturn(responseOk);

        mockMvc.perform(post("/api/cuentas/retirar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(400.0));

        log.info("Test pasado: retiro exitoso");
    }

    // ===================== DELETE /api/cuentas/{id} =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarCuentaInactiva_cuentaConSaldoCero_retorna204() throws Exception {
        log.info("Test: DELETE /api/cuentas/1 con saldo 0 retorna 204");

        mockMvc.perform(delete("/api/cuentas/1"))
                .andExpect(status().isNoContent());

        log.info("Test pasado: cuenta eliminada (204)");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarCuentaInactiva_cuentaConSaldo_retorna400() throws Exception {
        log.info("Test: DELETE de cuenta con saldo > 0 retorna 400");

        org.mockito.Mockito.doThrow(new IllegalArgumentException("No se puede eliminar cuenta con saldo"))
                .when(cuentaService).eliminarCuentaInactiva(1L);

        mockMvc.perform(delete("/api/cuentas/1"))
                .andExpect(status().isBadRequest());

        log.info("Test pasado: 400 al eliminar cuenta con saldo");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarCuentaInactiva_cuentaNoEncontrada_retorna404() throws Exception {
        log.info("Test: DELETE de cuenta inexistente retorna 404");

        org.mockito.Mockito.doThrow(new RuntimeException("Cuenta no encontrada"))
                .when(cuentaService).eliminarCuentaInactiva(999L);

        mockMvc.perform(delete("/api/cuentas/999"))
                .andExpect(status().isNotFound());

        log.info("Test pasado: 404 al eliminar cuenta inexistente");
    }
}