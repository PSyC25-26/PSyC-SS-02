package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.ClienteRequest;
import es.deusto.banca_online.entity.*;
import es.deusto.banca_online.repository.*;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.Ignore;

@RunWith(MockitoJUnitRunner.class)
public class ClienteServicePerformanceTest {

    @Rule public ContiPerfRule contiPerfRule = new ContiPerfRule();

    @Mock private IClienteRepository clienteRepository;
    @Mock private IUsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private ClienteService clienteService;

    private ClienteRequest requestValido;

    @Before
    public void setUp() {
        requestValido = new ClienteRequest();
        requestValido.setDni("12345678A");
        requestValido.setNombre("Juan");
        requestValido.setEmail("juan@test.com");
        requestValido.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        requestValido.setPassword("test123");

        // Usamos doReturn para evitar errores de casting accidentales
        doReturn(false).when(clienteRepository).existsByEmail(anyString());
        doReturn(false).when(clienteRepository).existsByDni(anyString());

        // Aseguramos que save devuelva un objeto Cliente real
        Cliente clienteSimulado = new Cliente();
        clienteSimulado.setId(1L);
        clienteSimulado.setNombre("Juan");
        doReturn(clienteSimulado).when(clienteRepository).save(any(Cliente.class));

        // Aseguramos que el guardado de usuario devuelva un Usuario
        doReturn(new Usuario()).when(usuarioRepository).save(any(Usuario.class));

        // El encoder devuelve un String
        doReturn("password_encriptada").when(passwordEncoder).encode(anyString());
    }

    // EL BASELINE
    @Test
    @Ignore("Test de rendimiento para ejecución manual")
    @PerfTest(invocations = 1, threads = 1)
    public void baseline_crearCliente() {
        clienteService.crearCliente(requestValido);
    }

    // saturacion 1
    @Test
    @Ignore("Test de rendimiento para ejecución manual")
    @PerfTest(duration = 60000, threads = 20) // 60 segundos para que te dé tiempo a ver VisualVM
    public void saturar_crearCliente_20threads() {
        try {
            // Ejecutamos la lógica para que la CPU trabaje
            clienteService.crearCliente(requestValido);
        } catch (Exception e) {
            // Ignoramos errores para que el test siga corriendo los 60 segundos
        }
    }

    @Test
    @Ignore("Test de rendimiento para ejecución manual")
    @PerfTest(duration = 60000, threads = 40)
    public void saturar_crearCliente_40threads() {
        try {
            // Ejecutamos la lógica para que la CPU trabaje
            clienteService.crearCliente(requestValido);
        } catch (Exception e) {
            // Ignoramos errores para que el test siga corriendo los 60 segundos
        }
    }

    @Test
    @Ignore("Test de rendimiento para ejecución manual")
    @PerfTest(duration = 60000, threads = 80)
    public void saturar_crearCliente_80threads() {
        try {
            // Ejecutamos la lógica para que la CPU trabaje
            clienteService.crearCliente(requestValido);
        } catch (Exception e) {
            // Ignoramos errores para que el test siga corriendo los 60 segundos
        }
    }
}