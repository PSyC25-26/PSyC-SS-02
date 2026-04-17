package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.entity.*;
import es.deusto.banca_online.repository.IClienteRepository;
import es.deusto.banca_online.repository.ICuentaRepository;
import es.deusto.banca_online.repository.ITransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CuentaServiceTest.class);

    @Mock private ICuentaRepository cuentaRepository;
    @Mock private IClienteRepository clienteRepository;
    @Mock private ITransaccionRepository transaccionRepository;
    @Mock private Authentication authentication;

    @InjectMocks
    private CuentaService cuentaService;

    private Cliente cliente;
    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setDni("12345678A");
        cliente.setNombre("Juan");
        cliente.setEmail("juan@test.com");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setFechaCreacion(LocalDateTime.now());

        cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setNumeroCuenta("ES1234567890ABCDEF12");
        cuenta.setSaldo(500.0);
        cuenta.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuenta.setCliente(cliente);
        cuenta.setFechaCreacion(LocalDateTime.now());
    }

    // ===================== crearCuenta =====================

    @Test
    void crearCuenta_clienteExiste_creaCorrectamente() {
        log.info("Test: crearCuenta con cliente existente");
        CuentaRequest request = new CuentaRequest();
        request.setClienteId(1L);
        request.setTipoCuenta("CORRIENTE");
        request.setSaldoInicial(500.0);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(inv -> {
            Cuenta c = inv.getArgument(0);
            c.setId(1L);
            c.setFechaCreacion(LocalDateTime.now());
            return c;
        });

        CuentaResponse response = cuentaService.crearCuenta(request);

        assertNotNull(response);
        assertEquals("CORRIENTE", response.getTipoCuenta());
        assertEquals(500.0, response.getSaldo());
        assertEquals(1L, response.getClienteId());
        log.info("Test pasado: cuenta creada con tipo={}", response.getTipoCuenta());
    }

    @Test
    void crearCuenta_saldoInicialNulo_usaCero() {
        log.info("Test: crearCuenta con saldo inicial nulo");
        CuentaRequest request = new CuentaRequest();
        request.setClienteId(1L);
        request.setTipoCuenta("AHORRO");
        request.setSaldoInicial(null);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(inv -> {
            Cuenta c = inv.getArgument(0);
            c.setId(2L);
            c.setFechaCreacion(LocalDateTime.now());
            return c;
        });

        CuentaResponse response = cuentaService.crearCuenta(request);
        assertEquals(0.0, response.getSaldo());
        log.info("Test pasado: saldo por defecto es 0.0");
    }

    @Test
    void crearCuenta_clienteNoExiste_lanzaExcepcion() {
        log.info("Test: crearCuenta con cliente inexistente");
        CuentaRequest request = new CuentaRequest();
        request.setClienteId(99L);
        request.setTipoCuenta("CORRIENTE");

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cuentaService.crearCuenta(request));
        assertTrue(ex.getMessage().contains("Cliente no encontrado"));
        log.info("Test pasado: excepcion lanzada correctamente");
    }

    @Test
    void crearCuenta_tipoInvalido_lanzaExcepcion() {
        log.info("Test: crearCuenta con tipo de cuenta invalido");
        CuentaRequest request = new CuentaRequest();
        request.setClienteId(1L);
        request.setTipoCuenta("TIPO_INVALIDO");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThrows(Exception.class, () -> cuentaService.crearCuenta(request));
        log.info("Test pasado: tipo invalido lanza excepcion");
    }

    // ===================== obtenerCuentasPorCliente =====================

    @Test
    void obtenerCuentasPorCliente_retornaLista() {
        log.info("Test: obtenerCuentasPorCliente");
        when(cuentaRepository.findByClienteId(1L)).thenReturn(List.of(cuenta));

        List<CuentaResponse> lista = cuentaService.obtenerCuentasPorCliente(1L);

        assertEquals(1, lista.size());
        assertEquals("ES1234567890ABCDEF12", lista.get(0).getNumeroCuenta());
        log.info("Test pasado: {} cuentas encontradas", lista.size());
    }

    @Test
    void obtenerCuentasPorCliente_sinCuentas_retornaListaVacia() {
        log.info("Test: obtenerCuentasPorCliente sin cuentas");
        when(cuentaRepository.findByClienteId(99L)).thenReturn(List.of());

        List<CuentaResponse> lista = cuentaService.obtenerCuentasPorCliente(99L);
        assertTrue(lista.isEmpty());
        log.info("Test pasado: lista vacia correctamente");
    }

    // ===================== obtenerSaldo =====================

    @Test
    void obtenerSaldo_admin_retornaSaldo() {
        log.info("Test: obtenerSaldo como ADMIN");
        mockAdmin();
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        Double saldo = cuentaService.obtenerSaldo(1L, authentication);
        assertEquals(500.0, saldo);
        log.info("Test pasado: saldo={}", saldo);
    }

    @Test
    void obtenerSaldo_cuentaNoExiste_lanzaExcepcion() {
        log.info("Test: obtenerSaldo cuenta inexistente");
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cuentaService.obtenerSaldo(99L, authentication));
        log.info("Test pasado: excepcion lanzada");
    }

    @Test
    void obtenerSaldo_clienteSinPermiso_lanzaAccessDenied() {
        log.info("Test: obtenerSaldo sin permiso");
        mockCliente(2L); // cliente con id 2, pero la cuenta pertenece al cliente 1

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        assertThrows(AccessDeniedException.class,
                () -> cuentaService.obtenerSaldo(1L, authentication));
        log.info("Test pasado: AccessDeniedException lanzada");
    }

    // ===================== depositarDinero =====================

    @Test
    void depositarDinero_montoValido_incrementaSaldo() {
        log.info("Test: depositar dinero valido");
        mockAdmin();
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CuentaResponse response = cuentaService.depositarDinero(1L, 200.0, authentication);

        assertEquals(700.0, response.getSaldo());
        log.info("Test pasado: nuevo saldo={}", response.getSaldo());
    }

    @Test
    void depositarDinero_montoNulo_lanzaExcepcion() {
        log.info("Test: depositar monto nulo");
        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.depositarDinero(1L, null, authentication));
        log.info("Test pasado: excepcion por monto nulo");
    }

    @Test
    void depositarDinero_montoCero_lanzaExcepcion() {
        log.info("Test: depositar monto cero");
        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.depositarDinero(1L, 0.0, authentication));
        log.info("Test pasado: excepcion por monto cero");
    }

    @Test
    void depositarDinero_montoNegativo_lanzaExcepcion() {
        log.info("Test: depositar monto negativo");
        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.depositarDinero(1L, -100.0, authentication));
        log.info("Test pasado: excepcion por monto negativo");
    }

    // ===================== retirarDinero =====================

    @Test
    void retirarDinero_saldoSuficiente_decrementaSaldo() {
        log.info("Test: retirar dinero con saldo suficiente");
        mockAdmin();
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CuentaResponse response = cuentaService.retirarDinero(1L, 100.0, authentication);

        assertEquals(400.0, response.getSaldo());
        log.info("Test pasado: nuevo saldo={}", response.getSaldo());
    }

    @Test
    void retirarDinero_saldoInsuficiente_lanzaExcepcion() {
        log.info("Test: retirar mas de lo disponible");
        mockAdmin();
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cuentaService.retirarDinero(1L, 9999.0, authentication));
        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
        log.info("Test pasado: excepcion saldo insuficiente");
    }

    @Test
    void retirarDinero_montoNulo_lanzaExcepcion() {
        log.info("Test: retirar monto nulo");
        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.retirarDinero(1L, null, authentication));
    }

    @Test
    void retirarDinero_montoNegativo_lanzaExcepcion() {
        log.info("Test: retirar monto negativo");
        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.retirarDinero(1L, -50.0, authentication));
    }

    // ===================== actualizarSaldo =====================

    @Test
    void actualizarSaldo_cuentaExiste_actualizaCorrectamente() {
        log.info("Test: actualizarSaldo");
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> cuentaService.actualizarSaldo(1L, 1000.0));
        log.info("Test pasado: saldo actualizado");
    }

    @Test
    void actualizarSaldo_cuentaNoExiste_lanzaExcepcion() {
        log.info("Test: actualizarSaldo cuenta inexistente");
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cuentaService.actualizarSaldo(99L, 100.0));
    }

    // ===================== helpers =====================

    private void mockAdmin() {
        var authority = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN");
        doReturn(List.of(authority)).when(authentication).getAuthorities();
    }

    private void mockCliente(Long clienteId) {
        var authority = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE");
        doReturn(List.of(authority)).when(authentication).getAuthorities();
        Usuario usuario = new Usuario();
        usuario.setClienteId(clienteId);
        when(authentication.getPrincipal()).thenReturn(usuario);
    }
}