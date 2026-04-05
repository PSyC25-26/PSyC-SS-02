package es.deusto.banca_online.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.dto.TransferenciaDTO;
import es.deusto.banca_online.services.TransferService;
import es.deusto.banca_online.entity.ETipoCuenta;
import es.deusto.banca_online.services.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import es.deusto.banca_online.dto.DepositoRequest;
import es.deusto.banca_online.dto.RetiroRequest;

// NUEVA IMPORTACIÓN - Reemplaza a MockBean
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//Indicamos que vamos a arrancar la capa web
@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    /*---------------
        ATRIBUTOS
    ---------------*/
    @Autowired
    private MockMvc mockMvc;  // Simulamos peticiones HTTP.

    @MockitoBean
    private CuentaService cuentaService;  // Mock del servicio

    @MockitoBean
    private TransferService transferService;  // Mock del servicio

    private ObjectMapper objectMapper; // Convierte objetos a JSON

    private CuentaRequest cuentaRequest;
    private CuentaResponse cuentaResponse;
    private List<CuentaResponse> cuentasMock;


    /*
    FLUJO QUE VA A TENER:
    cuentaRequest --> ObjectMapper --> JSON --> MockMvc --> Controller --> CuentaService --> Respuesta JSON
     */


    // Preparacion antes de cada test. Así no repetimos código en cada test.
    @BeforeEach
    void setUp() {
        // Hay que configurar manualmente el mapper
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Preparar CuentaRequest (lo que envia el usuario)
        cuentaRequest = new CuentaRequest();
        cuentaRequest.setClienteId(1L);
        cuentaRequest.setTipoCuenta(ETipoCuenta.CORRIENTE.name());
        cuentaRequest.setSaldoInicial(1000.0);

        // Preparar CuentaResponse (lo que devuelve el servicio)
        cuentaResponse = new CuentaResponse();
        cuentaResponse.setId(1L);
        cuentaResponse.setNumeroCuenta("ES123456789012345678");
        cuentaResponse.setSaldo(1000.0);
        cuentaResponse.setTipoCuenta(ETipoCuenta.CORRIENTE.name());
        cuentaResponse.setClienteId(1L);

        // Crear segunda cuenta para la lista
        CuentaResponse cuenta2 = new CuentaResponse();
        cuenta2.setId(2L);
        cuenta2.setNumeroCuenta("ES876543210987654321");
        cuenta2.setSaldo(500.0);
        cuenta2.setTipoCuenta(ETipoCuenta.AHORRO.name());
        cuenta2.setClienteId(1L);

        // Inicializar lista de cuentasMock para los tests
        cuentasMock = Arrays.asList(cuentaResponse, cuenta2);
    }


    /*---------------
        TESTS
    ---------------*/

    // DATOS CORRECTOS - CREAR
    @Test
    void crearCuenta_DatosValidos_Retorna201() throws Exception {
        // Cuando se cree una cuenta, devolvemos la que crearia la BD
        when(cuentaService.crearCuenta(any(CuentaRequest.class))).thenReturn(cuentaResponse);

        // Simulamos la peticion a /cuentas
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el body de la petición es JSON
                        .content(objectMapper.writeValueAsString(cuentaRequest))) // Escribimos el body en modo JSON
                .andExpect(status().isCreated())  // Esperamos un 201 Created
                .andExpect(jsonPath("$.id").value(1)) // Que la cuenta tenga ID 1
                .andExpect(jsonPath("$.numeroCuenta").value("ES123456789012345678")) // Que tenga el número de cuenta
                .andExpect(jsonPath("$.saldo").value(1000.0)) // Que tenga saldo 1000
                .andExpect(jsonPath("$.tipoCuenta").value("CORRIENTE")) // Que sea CORRIENTE
                .andExpect(jsonPath("$.clienteId").value(1)); // Que pertenezca al cliente con ID 1

        // Verificamos que se ha llamado a crearCuenta 1 vez
        verify(cuentaService, times(1)).crearCuenta(any(CuentaRequest.class));
    }


    // DATOS INCORRECTOS - CREAR (sin clienteId)
    @Test
    void crearCuenta_SinClienteId_Retorna400() throws Exception {
        // Request sin clienteId
        cuentaRequest.setClienteId(null);

        // Llamamos al endpoint
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuentaRequest)))
                .andExpect(status().isBadRequest());  // Esperamos un 400 Bad Request

        // Verificar que NO se haya llamado al servicio
        verify(cuentaService, never()).crearCuenta(any(CuentaRequest.class));
    }


    // DATOS INCORRECTOS - CREAR (sin tipoCuenta)
    @Test
    void crearCuenta_SinTipoCuenta_Retorna400() throws Exception {
        // Request sin tipoCuenta
        cuentaRequest.setTipoCuenta(null);

        // Llamamos al endpoint
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuentaRequest)))
                .andExpect(status().isBadRequest());  // Esperamos un 400 Bad Request

        // Verificar que NO se haya llamado al servicio
        verify(cuentaService, never()).crearCuenta(any(CuentaRequest.class));
    }


    // DATOS INCORRECTOS - CREAR (tipoCuenta vacío)
    @Test
    void crearCuenta_TipoCuentaVacio_Retorna400() throws Exception {
        // Request con tipoCuenta vacío
        cuentaRequest.setTipoCuenta("");

        // Llamamos al endpoint
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuentaRequest)))
                .andExpect(status().isBadRequest());  // Esperamos un 400 Bad Request

        // Verificar que NO se haya llamado al servicio
        verify(cuentaService, never()).crearCuenta(any(CuentaRequest.class));
    }


    // DATOS INCORRECTOS - CREAR (tipoCuenta inválido - no es un enum válido)
    @Test
    void crearCuenta_TipoCuentaInvalido_Retorna400() throws Exception {
        // Request con tipoCuenta inválido
        cuentaRequest.setTipoCuenta("INVALIDO");

        // Configuramos el mock para que cuando se llame al servicio, lance IllegalArgumentException
        when(cuentaService.crearCuenta(any(CuentaRequest.class)))
                .thenThrow(new IllegalArgumentException("No enum constant"));

        // Llamamos al endpoint
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuentaRequest)))
                .andExpect(status().isBadRequest());  // Esperamos un 400 Bad Request

        // Verificamos que se haya llamado al servicio 1 vez (porque pasa la validación del DTO pero falla en el servicio)
        verify(cuentaService, times(1)).crearCuenta(any(CuentaRequest.class));
    }


    // DATOS INCORRECTOS - CREAR (saldoInicial negativo)
    @Test
    void crearCuenta_SaldoNegativo_Retorna400() throws Exception {
        // Request con saldo negativo
        cuentaRequest.setSaldoInicial(-100.0);

        // Llamamos al endpoint
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuentaRequest)))
                .andExpect(status().isBadRequest());  // Esperamos un 400 Bad Request

        // Verificar que NO se haya llamado al servicio
        verify(cuentaService, never()).crearCuenta(any(CuentaRequest.class));
    }


    // DATOS CORRECTOS - Obtener cuentas por cliente
    @Test
    void obtenerCuentas_CuandoHayCuentas_DeberiaRetornarLista() throws Exception {
        // Cuando se llame al metodo obtenerCuentasPorCliente, utilizamos el cuentasMock
        when(cuentaService.obtenerCuentasPorCliente(1L)).thenReturn(cuentasMock);

        // Llamamos al endpoint y verificamos los datos
        mockMvc.perform(get("/api/cuentas")
                        .param("clienteId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].numeroCuenta").value("ES123456789012345678"))
                .andExpect(jsonPath("$[0].saldo").value(1000.0))
                .andExpect(jsonPath("$[0].tipoCuenta").value("CORRIENTE"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].numeroCuenta").value("ES876543210987654321"))
                .andExpect(jsonPath("$[1].saldo").value(500.0))
                .andExpect(jsonPath("$[1].tipoCuenta").value("AHORRO"));

        // Verificamos que se haya llamado al servicio obtenerCuentasPorCliente 1 vez
        verify(cuentaService, times(1)).obtenerCuentasPorCliente(1L);
    }


    // DATOS INCORRECTOS - Obtener cuentas por cliente sin parametro
    @Test
    void obtenerCuentas_SinClienteId_Retorna400() throws Exception {
        // Llamamos al endpoint sin el parametro clienteId
        mockMvc.perform(get("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // Esperamos un 400 Bad Request

        // Verificamos que NO se haya llamado al servicio
        verify(cuentaService, never()).obtenerCuentasPorCliente(anyLong());
    }


    // DATOS CORRECTOS - Obtener cuentas por cliente sin cuentas
    @Test
    void obtenerCuentas_CuandoNoHayCuentas_DeberiaRetornarListaVacia() throws Exception {
        // Cuando se llame al metodo obtenerCuentasPorCliente, utilizamos un Array vacío
        when(cuentaService.obtenerCuentasPorCliente(1L)).thenReturn(Arrays.asList());

        // Llamamos al endpoint y verificamos los datos
        mockMvc.perform(get("/api/cuentas")
                        .param("clienteId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Que ha devuelto el status 200
                .andExpect(jsonPath("$", hasSize(0))); // Que el array tiene size 0

        // Verificamos que se haya llamado al servicio obtenerCuentasPorCliente 1 vez
        verify(cuentaService, times(1)).obtenerCuentasPorCliente(1L);
    }


    // DATOS INCORRECTOS - Obtener cuentas por cliente con clienteId inválido
    @Test
    void obtenerCuentas_ClienteNoExiste_Retorna404() throws Exception {
        // Cuando se llame al metodo obtenerCuentasPorCliente, lanzamos excepción
        when(cuentaService.obtenerCuentasPorCliente(999L))
                .thenThrow(new RuntimeException("Cliente no encontrado con id: 999"));

        // Llamamos al endpoint
        mockMvc.perform(get("/api/cuentas")
                        .param("clienteId", "999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Esperamos un 404 Not Found

        // Verificamos que se haya llamado al servicio obtenerCuentasPorCliente 1 vez
        verify(cuentaService, times(1)).obtenerCuentasPorCliente(999L);
    }


    // DATOS INCORRECTOS - CREAR con cliente no existente
    @Test
    void crearCuenta_ClienteNoExiste_Retorna404() throws Exception {
        // Cuando se cree una cuenta con cliente inexistente, lanzamos excepción
        when(cuentaService.crearCuenta(any(CuentaRequest.class)))
                .thenThrow(new RuntimeException("Cliente no encontrado con id: 999"));

        // Modificamos el request para un cliente que no existe
        cuentaRequest.setClienteId(999L);

        // Llamamos al endpoint
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuentaRequest)))
                .andExpect(status().isNotFound()); // Esperamos un 404 Not Found

        // Verificamos que se haya llamado al servicio crearCuenta 1 vez
        verify(cuentaService, times(1)).crearCuenta(any(CuentaRequest.class));
    }



    // DATOS CORRECTOS - Con cuentas
    @Test
    void obtenerCuentasPorClienteId_CuandoHayCuentas_DeberiaRetornarLista() throws Exception {
        // Configuramos el mock para devolver la lista de cuentas simuladas
        when(cuentaService.obtenerCuentasPorCliente(1L)).thenReturn(cuentasMock);

        // Llamamos al endpoint con el nuevo formato path variable
        mockMvc.perform(get("/api/cuentas/1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Esperamos un 200 OK
                .andExpect(status().isOk())
                // Verificamos que devuelve un array con 2 elementos
                .andExpect(jsonPath("$", hasSize(2)))
                // Validamos primer cuenta
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].numeroCuenta").value("ES123456789012345678"))
                .andExpect(jsonPath("$[0].saldo").value(1000.0))
                // Validamos segunda cuenta
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].numeroCuenta").value("ES876543210987654321"))
                .andExpect(jsonPath("$[1].saldo").value(500.0));

        // Verificamos que se ha llamado al servicio 1 vez
        verify(cuentaService, times(1)).obtenerCuentasPorCliente(1L);
    }

    // DATOS INCORRECTOS - Cliente no existe
    @Test
    void obtenerCuentasPorClienteId_ClienteNoExiste_Retorna404() throws Exception {
        // Cuando se consulta un cliente que no existe, el servicio lanza excepción
        when(cuentaService.obtenerCuentasPorCliente(999L))
                .thenThrow(new RuntimeException("Cliente no encontrado con id: 999"));

        // Llamamos al endpoint con un ID de cliente inexistente
        mockMvc.perform(get("/api/cuentas/999")
                        .contentType(MediaType.APPLICATION_JSON))
                // Esperamos un 404 Not Found porque el cliente no existe
                .andExpect(status().isNotFound());

        // Verificamos que se ha llamado al servicio obtenerCuentasPorCliente 1 vez
        verify(cuentaService, times(1)).obtenerCuentasPorCliente(999L);
    }

    // DATOS CORRECTOS - Cliente sin cuentas
    @Test
    void obtenerCuentasPorClienteId_CuandoNoHayCuentas_DeberiaRetornarListaVacia() throws Exception {
        // Configuramos el mock para devolver una lista vacía
        when(cuentaService.obtenerCuentasPorCliente(1L)).thenReturn(Arrays.asList());

        // Llamamos al endpoint con un cliente que no tiene cuentas
        mockMvc.perform(get("/api/cuentas/1")
                        .contentType(MediaType.APPLICATION_JSON))
                // Esperamos un 200 OK (no es error, solo lista vacía)
                .andExpect(status().isOk())
                // Verificamos que devuelve un array con 0 elementos
                .andExpect(jsonPath("$", hasSize(0)));

        // Verificamos que se ha llamado al servicio obtenerCuentasPorCliente 1 vez
        verify(cuentaService, times(1)).obtenerCuentasPorCliente(1L);
    }



    //SALDO CORRECTO (200 OK)
    @Test
    void verSaldo_CuandoCuentaExiste_RetornaSaldo() throws Exception {
        // Simulamos saldo
        when(cuentaService.obtenerSaldo(1L)).thenReturn(1000.0);

        // Llamamos al endpoint
        mockMvc.perform(get("/api/cuentas/saldo/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(1000.0));

        // Verificamos llamada al servicio
        verify(cuentaService, times(1)).obtenerSaldo(1L);
    }

    //CUENTA NO ENCONTRADA (404)
    @Test
    void verSaldo_CuentaNoExiste_Retorna404() throws Exception {
        // Simulamos excepción
        when(cuentaService.obtenerSaldo(999L))
                .thenThrow(new RuntimeException("Cuenta no encontrada con id: 999"));

        // Llamamos al endpoint
        mockMvc.perform(get("/api/cuentas/saldo/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Verificamos llamada al servicio
        verify(cuentaService, times(1)).obtenerSaldo(999L);
    }

    //EXCEPTION NO CONTROLADA
    @Test
    void verSaldo_ErrorInterno_LanzaExcepcion() throws Exception {
        // Simulamos error inesperado
        when(cuentaService.obtenerSaldo(1L))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Esperamos excepción (diferente a 404)
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> 
            mockMvc.perform(get("/api/cuentas/saldo/1")
                   .contentType(MediaType.APPLICATION_JSON))
        ).hasCauseInstanceOf(RuntimeException.class)
         .hasMessageContaining("Error de base de datos");
    }

    // --- TESTS PARA TRANSFERIR DINERO ---

    @Test
    void transferirDinero_DatosValidos_Retorna200() throws Exception {
        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigen("ES123456789012345678");
        dto.setCuentaDestino("ES876543210987654321");
        dto.setCantidad(100.0);

        when(transferService.transferirDinero(any(TransferenciaDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/cuentas/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuentaOrigen").value("ES123456789012345678"))
                .andExpect(jsonPath("$.cuentaDestino").value("ES876543210987654321"))
                .andExpect(jsonPath("$.cantidad").value(100.0));

        verify(transferService, times(1)).transferirDinero(any(TransferenciaDTO.class));
    }

    @Test
    void transferirDinero_CuentaNoEncontrada_Retorna404() throws Exception {
        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigen("ES999");
        dto.setCuentaDestino("ES876543210987654321");
        dto.setCantidad(100.0);

        when(transferService.transferirDinero(any(TransferenciaDTO.class)))
                .thenThrow(new RuntimeException("Cuenta no encontrada"));

        mockMvc.perform(post("/api/cuentas/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(transferService, times(1)).transferirDinero(any(TransferenciaDTO.class));
    }

    @Test
    void transferirDinero_SaldoInsuficiente_RetornaError() throws Exception {
        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigen("ES123456789012345678");
        dto.setCuentaDestino("ES876543210987654321");
        dto.setCantidad(10000.0);

        when(transferService.transferirDinero(any(TransferenciaDTO.class)))
                .thenThrow(new RuntimeException("Saldo insuficiente en la cuenta de origen"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                mockMvc.perform(post("/api/cuentas/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
        ).hasCauseInstanceOf(RuntimeException.class)
         .hasMessageContaining("Saldo insuficiente");

        verify(transferService, times(1)).transferirDinero(any(TransferenciaDTO.class));
    }

    // ==========================================
    // TESTS PARA ENDPOINT POST /cuentas/deposito
    // ==========================================

    @Test
    void depositarDinero_DatosValidos_Retorna200() throws Exception {
        // Preparamos el Request
        DepositoRequest depositoRequest = new DepositoRequest();
        depositoRequest.setCuentaId(1L);
        depositoRequest.setMonto(150.0);

        // Preparamos el Response esperado
        cuentaResponse.setSaldo(1150.0); // 1000 originales + 150

        when(cuentaService.depositarDinero(1L, 150.0)).thenReturn(cuentaResponse);

        // Ejecutamos la petición POST
        mockMvc.perform(post("/api/cuentas/deposito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(1150.0));

        verify(cuentaService, times(1)).depositarDinero(1L, 150.0);
    }

    @Test
    void depositarDinero_CuentaNoExiste_Retorna404() throws Exception {
        DepositoRequest depositoRequest = new DepositoRequest();
        depositoRequest.setCuentaId(999L);
        depositoRequest.setMonto(100.0);

        when(cuentaService.depositarDinero(999L, 100.0))
                .thenThrow(new RuntimeException("Cuenta no encontrada"));

        mockMvc.perform(post("/api/cuentas/deposito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositoRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void retirarDinero_DatosValidos_Retorna200() throws Exception {
        RetiroRequest retiroRequest = new RetiroRequest();
        retiroRequest.setCuentaId(1L);
        retiroRequest.setMonto(200.0);

        cuentaResponse.setSaldo(800.0); // 1000 originales - 200

        when(cuentaService.retirarDinero(1L, 200.0)).thenReturn(cuentaResponse);

        mockMvc.perform(post("/api/cuentas/retiro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retiroRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(800.0));
    }

    @Test
    void retirarDinero_SaldoInsuficiente_Retorna400() throws Exception {
        RetiroRequest retiroRequest = new RetiroRequest();
        retiroRequest.setCuentaId(1L);
        retiroRequest.setMonto(5000.0); // Monto gigante para provocar error

        when(cuentaService.retirarDinero(1L, 5000.0))
                .thenThrow(new IllegalArgumentException("Saldo insuficiente"));

        mockMvc.perform(post("/api/cuentas/retiro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retiroRequest)))
                .andExpect(status().isBadRequest()); // Esperamos error 400
    }
}