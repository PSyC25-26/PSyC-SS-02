package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.entity.*;
import es.deusto.banca_online.repository.IClienteRepository;
import es.deusto.banca_online.repository.ICuentaRepository;
import es.deusto.banca_online.repository.ITransaccionRepository;
import es.deusto.banca_online.security.AuthChecks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.lenient;

/**
 * Tests de aceptacion: verifican que el sistema cumple los requisitos funcionales desde el punto de vista del usuario/negocio.
 * Cada test describe un criterio de aceptacion de una historia de usuario.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("Tests de Aceptacion - Gestion de Cuentas Bancarias")
class CuentaServiceAcceptanceTest {

    private static final Logger log = LoggerFactory.getLogger(CuentaServiceAcceptanceTest.class);

    @Mock private ICuentaRepository cuentaRepository;
    @Mock private IClienteRepository clienteRepository;
    @Mock private ITransaccionRepository transaccionRepository;
    @Mock private AuthChecks authChecks;
    @Mock private Authentication authentication;

    @InjectMocks private CuentaService cuentaService;

    private Cliente cliente;
    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setDni("12345678A");
        cliente.setNombre("Maria Garcia");
        cliente.setEmail("maria@banco.com");
        cliente.setFechaNacimiento(LocalDate.of(1985, 5, 15));
        cliente.setFechaCreacion(LocalDateTime.now());

        cuenta = new Cuenta();
        cuenta.setId(10L);
        cuenta.setNumeroCuenta("ES9820385778983000760236");
        cuenta.setSaldo(1500.0);
        cuenta.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuenta.setCliente(cliente);
        cuenta.setFechaCreacion(LocalDateTime.now());

        mockAdmin();
    }

    // ===================== HU2.1: Crear Cuenta =====================

    @Test
    @DisplayName("HU2.1 - AC1: Como administrador, puedo crear una cuenta CORRIENTE para un cliente existente")
    void aceptacion_crearCuentaCorriente_paraClienteExistente() {
        log.info("Aceptacion HU2.1-AC1: crear cuenta CORRIENTE");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.save(any())).thenAnswer(inv -> {
            Cuenta c = inv.getArgument(0);
            c.setId(10L);
            c.setFechaCreacion(LocalDateTime.now());
            return c;
        });

        CuentaRequest req = new CuentaRequest();
        req.setClienteId(1L);
        req.setTipoCuenta("CORRIENTE");
        req.setSaldoInicial(1000.0);

        CuentaResponse response = cuentaService.crearCuenta(req);

        assertNotNull(response.getNumeroCuenta(), "La cuenta debe tener un numero asignado");
        assertTrue(response.getNumeroCuenta().startsWith("ES"), "El numero debe empezar por ES");
        assertEquals("CORRIENTE", response.getTipoCuenta());
        assertEquals(1000.0, response.getSaldo());
        log.info("Aceptacion HU2.1-AC1 PASADO: cuenta {} creada", response.getNumeroCuenta());
    }

    @Test
    @DisplayName("HU2.1 - AC2: Como administrador, puedo crear una cuenta AHORRO con saldo inicial cero")
    void aceptacion_crearCuentaAhorro_sinSaldoInicial() {
        log.info("Aceptacion HU2.1-AC2: cuenta AHORRO sin saldo");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.save(any())).thenAnswer(inv -> {
            Cuenta c = inv.getArgument(0);
            c.setId(11L);
            c.setFechaCreacion(LocalDateTime.now());
            return c;
        });

        CuentaRequest req = new CuentaRequest();
        req.setClienteId(1L);
        req.setTipoCuenta("AHORRO");
        req.setSaldoInicial(null);

        CuentaResponse response = cuentaService.crearCuenta(req);

        assertEquals(0.0, response.getSaldo(), "El saldo inicial por defecto debe ser 0");
        assertEquals("AHORRO", response.getTipoCuenta());
        log.info("Aceptacion HU2.1-AC2 PASADO");
    }

    @Test
    @DisplayName("HU2.1 - AC3: No se puede crear una cuenta para un cliente que no existe")
    void aceptacion_crearCuenta_clienteInexistente_falla() {
        log.info("Aceptacion HU2.1-AC3: cliente inexistente");
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        CuentaRequest req = new CuentaRequest();
        req.setClienteId(999L);
        req.setTipoCuenta("CORRIENTE");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cuentaService.crearCuenta(req));
        assertNotNull(ex.getMessage());
        log.info("Aceptacion HU2.1-AC3 PASADO: sistema rechaza cliente inexistente");
    }

    // ===================== HU2.2: Consultar Saldo =====================

    @Test
    @DisplayName("HU2.2 - AC1: Un cliente puede consultar el saldo de su propia cuenta")
    void aceptacion_consultarSaldo_propietario_permitido() {
        log.info("Aceptacion HU2.2-AC1: cliente consulta su propio saldo");
        mockCliente(1L);
        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuenta));

        Double saldo = cuentaService.obtenerSaldo(10L, authentication);

        assertEquals(1500.0, saldo);
        log.info("Aceptacion HU2.2-AC1 PASADO: saldo={}", saldo);
    }

    @Test
    @DisplayName("HU2.2 - AC2: Un cliente NO puede consultar el saldo de la cuenta de otro cliente")
    void aceptacion_consultarSaldo_otroPropietario_bloqueado() {
        log.info("Aceptacion HU2.2-AC2: cliente intenta ver cuenta ajena");
        mockCliente(99L); // cliente 99 intenta ver cuenta del cliente 1
        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuenta));
        doThrow(new org.springframework.security.access.AccessDeniedException("No tiene permiso"))
                .when(authChecks).assertOwnsCuenta(authentication, cuenta);

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> cuentaService.obtenerSaldo(10L, authentication));
        log.info("Aceptacion HU2.2-AC2 PASADO: acceso denegado correctamente");
    }

    // ===================== HU2.3: Depositar Dinero =====================

    @Test
    @DisplayName("HU2.3 - AC1: Un deposito valido incrementa el saldo correctamente")
    void aceptacion_depositar_incrementaSaldo() {
        log.info("Aceptacion HU2.3-AC1: deposito incrementa saldo");
        mockAdmin();
        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CuentaResponse response = cuentaService.depositarDinero(10L, 500.0, authentication);

        assertEquals(2000.0, response.getSaldo(), "El saldo debe incrementarse en 500");
        log.info("Aceptacion HU2.3-AC1 PASADO: nuevo saldo={}", response.getSaldo());
    }

    @Test
    @DisplayName("HU2.3 - AC2: No se puede depositar un monto de cero euros")
    void aceptacion_depositar_montoCero_rechazado() {
        log.info("Aceptacion HU2.3-AC2: deposito de cero rechazado");
        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.depositarDinero(10L, 0.0, authentication));
        log.info("Aceptacion HU2.3-AC2 PASADO");
    }

    @Test
    @DisplayName("HU2.3 - AC3: No se puede depositar un monto negativo")
    void aceptacion_depositar_montoNegativo_rechazado() {
        log.info("Aceptacion HU2.3-AC3: deposito negativo rechazado");
        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.depositarDinero(10L, -100.0, authentication));
        log.info("Aceptacion HU2.3-AC3 PASADO");
    }

    // ===================== HU2.4: Retirar Dinero =====================

    @Test
    @DisplayName("HU2.4 - AC1: Un retiro valido decrementa el saldo correctamente")
    void aceptacion_retirar_decrementaSaldo() {
        log.info("Aceptacion HU2.4-AC1: retiro decrementa saldo");
        mockAdmin();
        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CuentaResponse response = cuentaService.retirarDinero(10L, 500.0, authentication);

        assertEquals(1000.0, response.getSaldo(), "El saldo debe decrementarse en 500");
        log.info("Aceptacion HU2.4-AC1 PASADO: nuevo saldo={}", response.getSaldo());
    }

    @Test
    @DisplayName("HU2.4 - AC2: No se puede retirar mas dinero del disponible en la cuenta")
    void aceptacion_retirar_saldoInsuficiente_rechazado() {
        log.info("Aceptacion HU2.4-AC2: retiro con saldo insuficiente rechazado");
        mockAdmin();
        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuenta));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cuentaService.retirarDinero(10L, 99999.0, authentication));
        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
        log.info("Aceptacion HU2.4-AC2 PASADO: saldo insuficiente rechazado");
    }

    @Test
    @DisplayName("HU2.4 - AC3: No se puede retirar exactamente el saldo disponible y quedar a cero")
    void aceptacion_retirar_todoElSaldo_permitido() {
        log.info("Aceptacion HU2.4-AC3: retirar todo el saldo");
        mockAdmin();
        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CuentaResponse response = cuentaService.retirarDinero(10L, 1500.0, authentication);

        assertEquals(0.0, response.getSaldo(), "El saldo puede quedar a cero");
        log.info("Aceptacion HU2.4-AC3 PASADO: saldo={}", response.getSaldo());
    }

    // ===================== HU2.5: Listar Cuentas =====================

    @Test
    @DisplayName("HU2.5 - AC1: El administrador puede listar todas las cuentas de un cliente")
    void aceptacion_listarCuentas_admin_obtieneLista() {
        log.info("Aceptacion HU2.5-AC1: admin lista cuentas de cliente");
        Cuenta cuenta2 = new Cuenta();
        cuenta2.setId(11L);
        cuenta2.setNumeroCuenta("ES9999999999999999");
        cuenta2.setSaldo(200.0);
        cuenta2.setTipoCuenta(ETipoCuenta.AHORRO);
        cuenta2.setCliente(cliente);
        cuenta2.setFechaCreacion(LocalDateTime.now());

        when(cuentaRepository.findByClienteId(1L)).thenReturn(List.of(cuenta, cuenta2));

        List<CuentaResponse> lista = cuentaService.obtenerCuentasPorCliente(1L);

        assertEquals(2, lista.size(), "El cliente debe tener 2 cuentas");
        log.info("Aceptacion HU2.5-AC1 PASADO: {} cuentas listadas", lista.size());
    }

    // ===================== helpers =====================

    private void mockAdmin() {
        var auth = new SimpleGrantedAuthority("ROLE_ADMIN");
        lenient().doReturn(List.of(auth)).when(authentication).getAuthorities();
        lenient().when(authChecks.isAdmin(authentication)).thenReturn(true);
    }

    private void mockCliente(Long clienteId) {
        var auth = new SimpleGrantedAuthority("ROLE_CLIENTE");
        lenient().doReturn(List.of(auth)).when(authentication).getAuthorities();
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        Usuario usuario = new Usuario();
        usuario.setCliente(cliente);
        lenient().when(authentication.getPrincipal()).thenReturn(usuario);
        lenient().when(authChecks.isAdmin(authentication)).thenReturn(false);
        lenient().when(authChecks.clienteIdOrNull(authentication)).thenReturn(clienteId);
    }
}