package es.deusto.banca_online;

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.repository.IClienteRepository;
import es.deusto.banca_online.services.CuentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CuentaControllerTest {

    @Autowired
    private CuentaService cuentaService;

    @Autowired
    private IClienteRepository clienteRepository;

    @Test
    void crearCuenta_debeGuardarCorrectamente() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Test");
        cliente.setDni("TEST-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        cliente.setEmail("test@test.com");
        cliente.setFechaNacimiento(java.time.LocalDate.of(1990, 1, 1));
        cliente.setFechaCreacion(java.time.LocalDateTime.now());
        clienteRepository.save(cliente);

        CuentaRequest request = new CuentaRequest();
        request.setClienteId(cliente.getId());
        request.setTipoCuenta("CORRIENTE");
        request.setSaldoInicial(500.0);

        CuentaResponse response = cuentaService.crearCuenta(request);

        assertNotNull(response);
        assertNotNull(response.getNumeroCuenta());
        assertEquals("CORRIENTE", response.getTipoCuenta());
        assertEquals(500.0, response.getSaldo());
        assertEquals(cliente.getId(), response.getClienteId());
    }
}