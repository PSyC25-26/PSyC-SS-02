package es.deusto.banca_online.services;

/**
 * @file ClienteServicePerformanceTest.java
 * @brief Tests de rendimiento del servicio de clientes con ContiPerf.
 * @details Mide la latencia y el comportamiento bajo carga concurrente (20, 40 y 80 hilos)
 *          de la operación crearCliente. Los tests están marcados con @Ignore y se
 *          ejecutan manualmente para no penalizar el build habitual.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.junit.Ignore;

@RunWith(MockitoJUnitRunner.class)
public class ClienteServicePerformanceTest {

    /**
     * Regla de ContiPerf que habilita la ejecución de tests de rendimiento y 
     * la generación de informes estadísticos (latencia, rendimiento, percentiles).
     */
    @Rule public ContiPerfRule contiPerfRule = new ContiPerfRule();

    /**
     * Mock del repositorio de clientes para evitar latencia de E/S de red o disco.
     */
    @Mock private IClienteRepository clienteRepository;
    /**
     * Mock del repositorio de usuarios.
     */
    @Mock private IUsuarioRepository usuarioRepository;
    /**
     * Mock del codificador de contraseñas, permitiendo medir el impacto del 
     * procesamiento de CPU del algoritmo de hashing (Bcrypt).
     */
    @Mock private PasswordEncoder passwordEncoder;

    /**
     * Servicio donde se inyectan los mocks para medir el rendimiento de la lógica de negocio pura.
     */
    @InjectMocks private ClienteService clienteService;

    private ClienteRequest requestValido;

    /**
     * Configuración de los mocks y datos de prueba.
     * Define comportamientos instantáneos para que los resultados de rendimiento
     * se centren en la eficiencia del código del servicio y no en la base de datos.
     */
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
    /**
     * Test de Línea Base (Baseline).
     * Ejecuta una única operación para establecer el tiempo de respuesta de referencia
     * en condiciones óptimas de un solo hilo y una sola invocación.
     */
    @Test
    @Ignore("Test de rendimiento para ejecución manual")
    @PerfTest(invocations = 1, threads = 1)
    public void baseline_crearCliente() {
        clienteService.crearCliente(requestValido);
    }

    // saturacion 1
    /**
     * Test de estrés - Nivel Bajo (20 hilos).
     * Mantiene una carga constante de 20 usuarios concurrentes durante 60 segundos.
     * Útil para observar el comportamiento inicial del recolector de basura (GC) 
     * y el uso de CPU a través de herramientas como VisualVM.
     */
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

    /**
     * Test de estrés - Nivel Medio (40 hilos).
     * Duplica la carga concurrente para identificar posibles cuellos de botella 
     * en la gestión de hilos y la contención de recursos.
     */
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

    /**
     * Test de estrés - Nivel Alto (80 hilos).
     * Somete al servicio a una carga pesada de 80 hilos concurrentes.
     * El objetivo es determinar el punto de ruptura o la degradación máxima 
     * admisible de los tiempos de respuesta bajo máxima demanda.
     */
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