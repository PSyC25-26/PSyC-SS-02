package es.deusto.banca_online.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.services.TransferService;
import es.deusto.banca_online.entity.ETipoCuenta;
import es.deusto.banca_online.services.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

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
        mockMvc.perform(post("/cuentas")
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
        mockMvc.perform(post("/cuentas")
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
        mockMvc.perform(post("/cuentas")
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
        mockMvc.perform(post("/cuentas")
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
        mockMvc.perform(post("/cuentas")
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
        mockMvc.perform(post("/cuentas")
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
        mockMvc.perform(get("/cuentas")
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
        mockMvc.perform(get("/cuentas")
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
        mockMvc.perform(get("/cuentas")
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
        mockMvc.perform(get("/cuentas")
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
        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuentaRequest)))
                .andExpect(status().isNotFound()); // Esperamos un 404 Not Found

        // Verificamos que se haya llamado al servicio crearCuenta 1 vez
        verify(cuentaService, times(1)).crearCuenta(any(CuentaRequest.class));
    }

    //SALDO CORRECTO (200 OK)
    @Test
    void verSaldo_CuandoCuentaExiste_RetornaSaldo() throws Exception {
        // Simulamos saldo
        when(cuentaService.obtenerSaldo(1L)).thenReturn(1000.0);

        // Llamamos al endpoint
        mockMvc.perform(get("/cuentas/saldo/1")
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
        mockMvc.perform(get("/cuentas/saldo/999")
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
            mockMvc.perform(get("/cuentas/saldo/1")
                   .contentType(MediaType.APPLICATION_JSON))
        ).hasCauseInstanceOf(RuntimeException.class)
         .hasMessageContaining("Error de base de datos");

        verify(cuentaService, times(1)).obtenerSaldo(1L);
    }
}