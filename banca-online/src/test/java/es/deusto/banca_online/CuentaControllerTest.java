package es.deusto.banca_online;

import es.deusto.banca_online.controllers.CuentaController;
import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.services.CuentaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuentaService cuentaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void crearCuenta_debeGuardarCorrectamente() throws Exception {
        CuentaRequest request = new CuentaRequest();
        request.setClienteId(1L);
        request.setTipoCuenta("CORRIENTE");
        request.setSaldoInicial(500.0);

        CuentaResponse response = new CuentaResponse();
        response.setId(1L);
        response.setNumeroCuenta("ES1234567890ABCDEF12");
        response.setSaldo(500.0);
        response.setTipoCuenta("CORRIENTE");
        response.setClienteId(1L);

        Mockito.when(cuentaService.crearCuenta(Mockito.any())).thenReturn(response);

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCuenta").value("ES1234567890ABCDEF12"))
                .andExpect(jsonPath("$.saldo").value(500.0))
                .andExpect(jsonPath("$.tipoCuenta").value("CORRIENTE"));
    }
}