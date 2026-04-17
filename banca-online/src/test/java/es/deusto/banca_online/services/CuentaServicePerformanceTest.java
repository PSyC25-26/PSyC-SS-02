package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.entity.*;
import es.deusto.banca_online.repository.IClienteRepository;
import es.deusto.banca_online.repository.ICuentaRepository;
import es.deusto.banca_online.repository.ITransaccionRepository;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests de rendimiento con ContiPerf.
 * Medimos: invocaciones, hilos, tiempo medio y maximo.
 *
 * COMO EJECUTAR:
 *   mvnw.cmd test -pl banca-online -Dtest=CuentaServicePerformanceTest
 *
 * REPORT: target/contiperf-report/index.html
 */
@ExtendWith(MockitoExtension.class)
class CuentaServicePerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(CuentaServicePerformanceTest.class);

    @Mock private ICuentaRepository cuentaRepository;
    @Mock private IClienteRepository clienteRepository;
    @Mock private ITransaccionRepository transaccionRepository;
    @Mock private Authentication authentication;

    @InjectMocks private CuentaService cuentaService;

    private Cliente cliente;
    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setDni("12345678A");
        cliente.setNombre("Perf Test User");
        cliente.setEmail("perf@test.com");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setFechaCreacion(LocalDateTime.now());

        cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setNumeroCuenta("ES-PERF-001");
        cuenta.setSaldo(10000.0);
        cuenta.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuenta.setCliente(cliente);
        cuenta.setFechaCreacion(LocalDateTime.now());

        // Setup authentication as ADMIN
        var auth = new SimpleGrantedAuthority("ROLE_ADMIN");
        lenient().doReturn(List.of(auth)).when(authentication).getAuthorities();
    }

    // ===================== TEST 1: invocaciones — EXITOSO =====================

    /**
     * Test exitoso de rendimiento:
     * 100 invocaciones en 4 hilos, maximo 500ms por llamada.
     * Verifica que crearCuenta es estable bajo carga.
     */
    @Test
    @PerfTest(invocations = 100, threads = 4)
    @Required(max = 500, average = 100)
    void rendimiento_crearCuenta_100invocaciones_4hilos() {
        log.info("PerfTest: crearCuenta 100x4 hilos");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(inv -> {
            Cuenta c = inv.getArgument(0);
            c.setId((long) (Math.random() * 1000));
            c.setFechaCreacion(LocalDateTime.now());
            return c;
        });

        CuentaRequest request = new CuentaRequest();
        request.setClienteId(1L);
        request.setTipoCuenta("CORRIENTE");
        request.setSaldoInicial(100.0);

        assertDoesNotThrow(() -> cuentaService.crearCuenta(request));
    }

    // ===================== TEST 2: throughput — EXITOSO =====================

    /**
     * Test de throughput (operaciones por segundo):
     * Ejecuta obtenerCuentasPorCliente durante 2 segundos continuamente.
     * Mide cuantas operaciones por segundo se pueden realizar.
     */
    @Test
    @PerfTest(duration = 2000, threads = 2)
    @Required(throughput = 50)
    void rendimiento_obtenerCuentas_throughput() {
        log.info("PerfTest throughput: obtenerCuentasPorCliente durante 2s");
        when(cuentaRepository.findByClienteId(1L)).thenReturn(List.of(cuenta));

        assertDoesNotThrow(() -> cuentaService.obtenerCuentasPorCliente(1L));
    }

    // ===================== TEST 3: depositar — EXITOSO =====================

    /**
     * Test de rendimiento del deposito:
     * 50 invocaciones, 2 hilos, maximo 300ms.
     */
    @Test
    @PerfTest(invocations = 50, threads = 2)
    @Required(max = 300, average = 50)
    void rendimiento_depositarDinero_50invocaciones() {
        log.info("PerfTest: depositarDinero 50 invocaciones");
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Reset saldo before each test iteration to avoid going negative
        cuenta.setSaldo(10000.0);

        assertDoesNotThrow(() -> cuentaService.depositarDinero(1L, 10.0, authentication));
    }

    // ===================== TEST 4: duracion — FALLIDO (para demostrar) =====================

    /**
     * Test FALLIDO intencionado:
     * Exige que obtenerSaldo se complete en menos de 1ms de promedio.
     * descomentar @PerfTest y @Required para activarlo.
     *
     * Nota: dejamos este metodo comentado para no romper el build.
     */
    @Test
    // @PerfTest(invocations = 200, threads = 8)
    // @Required(max = 1, average = 1)  // <- imposible de cumplir: demostrara fallo
    void rendimiento_FALLIDO_exigenciasImposibles() {
        log.warn("PerfTest FALLIDO (demostrativo): este test falla si se activa con @Required(max=1)");
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        // Sin anotaciones PerfTest activas, simplemente verifica que funciona
        Double saldo = cuentaService.obtenerSaldo(1L, authentication);
        assertNotNull(saldo);
        log.info("Sin restricciones de rendimiento: saldo={}", saldo);
    }

    // ===================== TEST 5: retiro — duracion =====================

    /**
     * Test de duracion para retiro:
     * Ejecuta retirarDinero durante 1 segundo en 1 hilo.
     */
    @Test
    @PerfTest(duration = 1000, threads = 1)
    @Required(throughput = 20)
    void rendimiento_retirarDinero_duracion1segundo() {
        log.info("PerfTest duracion: retirarDinero durante 1s");
        cuenta.setSaldo(100000.0); // saldo alto para no agotar
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> cuentaService.retirarDinero(1L, 1.0, authentication));
    }
}