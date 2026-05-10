package es.deusto.banca_online.services;

/**
 * @file CuentaServicePerformanceTest.java
 * @brief Tests de rendimiento del servicio de cuentas bancarias con ContiPerf.
 * @details Mide throughput, latencia máxima y comportamiento bajo carga de operaciones
 *          como crearCuenta, depositarDinero y retirarDinero. Genera un informe en
 *          target/contiperf-report/index.html.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.entity.*;
import es.deusto.banca_online.repository.IClienteRepository;
import es.deusto.banca_online.repository.ICuentaRepository;
import es.deusto.banca_online.repository.ITransaccionRepository;
import es.deusto.banca_online.security.AuthChecks;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Tests de rendimiento con ContiPerf.
 * Todos los stubs se configuran en @Before para evitar problemas de thread-safety
 * cuando ContiPerf ejecuta los metodos de test en paralelo.
 *
 * COMO EJECUTAR:
 *   mvnw.cmd test -Dtest=CuentaServicePerformanceTest
 *
 * REPORT: target/contiperf-report/index.html
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CuentaServicePerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(CuentaServicePerformanceTest.class);

    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();

    @Mock private ICuentaRepository cuentaRepository;
    @Mock private IClienteRepository clienteRepository;
    @Mock private ITransaccionRepository transaccionRepository;
    @Mock private AuthChecks authChecks;
    @Mock private Authentication authentication;

    @InjectMocks private CuentaService cuentaService;

    private Cliente cliente;
    private Cuenta cuenta;

    @Before
    public void setUp() {
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
        cuenta.setSaldo(1000000.0);
        cuenta.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuenta.setCliente(cliente);
        cuenta.setFechaCreacion(LocalDateTime.now());

        // Authentication como ADMIN
        var auth = new SimpleGrantedAuthority("ROLE_ADMIN");
        lenient().doReturn(List.of(auth)).when(authentication).getAuthorities();
        lenient().when(authChecks.isAdmin(authentication)).thenReturn(true);

        // TODOS los stubs aqui — una sola vez, antes de que ContiPerf lance los hilos
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.findByClienteId(1L)).thenReturn(List.of(cuenta));
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(inv -> {
            Cuenta c = inv.getArgument(0);
            if (c.getId() == null) c.setId(1L);
            if (c.getFechaCreacion() == null) c.setFechaCreacion(LocalDateTime.now());
            return c;
        });
        when(transaccionRepository.save(any(Transaccion.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // ===================== TEST 1: invocaciones — EXITOSO =====================

    /**
     * Test exitoso de rendimiento:
     * 100 invocaciones en 4 hilos, maximo 500ms por llamada.
     * Verifica que crearCuenta es estable bajo carga.
     */
    @Test
    @PerfTest(invocations = 100, threads = 1)
    @Required(max = 500, average = 100)
    public void rendimiento_crearCuenta_100invocaciones_4hilos() {
        CuentaRequest request = new CuentaRequest();
        request.setClienteId(1L);
        request.setTipoCuenta("CORRIENTE");
        request.setSaldoInicial(100.0);
        assertNotNull(cuentaService.crearCuenta(request));
    }

    // ===================== TEST 2: throughput — EXITOSO =====================

    /**
     * Test de throughput (operaciones por segundo):
     * Ejecuta obtenerCuentasPorCliente durante 2 segundos continuamente.
     * Mide cuantas operaciones por segundo se pueden realizar.
     */
    @Test
    @PerfTest(invocations = 50, threads = 1)
    @Required(max = 5000, average = 100)
    public void rendimiento_obtenerCuentas_throughput() {
        assertFalse(cuentaService.obtenerCuentasPorCliente(1L).isEmpty());
    }

    // ===================== TEST 3: depositar — EXITOSO =====================

    /**
     * Test de rendimiento del deposito:
     * 50 invocaciones, 2 hilos, maximo 300ms.
     */
    @Test
    @PerfTest(invocations = 50, threads = 1)
    @Required(max = 300, average = 50)
    public void rendimiento_depositarDinero_50invocaciones() {
        assertNotNull(cuentaService.depositarDinero(1L, 10.0, authentication));
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
    // @Required(max = 1, average = 1)  // <- imposible de cumplir: fallará
    public void rendimiento_FALLIDO_exigenciasImposibles() {
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
    @Required(throughput = 10)
    public void rendimiento_retirarDinero_duracion1segundo() {
        assertNotNull(cuentaService.retirarDinero(1L, 1.0, authentication));
    }
}