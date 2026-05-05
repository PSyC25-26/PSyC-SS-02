package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.TransferenciaDTO;
import es.deusto.banca_online.entity.*;
import es.deusto.banca_online.repository.ICuentaRepository;
import es.deusto.banca_online.repository.ITransaccionRepository;
import es.deusto.banca_online.security.AuthChecks;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final Logger log = LoggerFactory.getLogger(TransferServiceTest.class);

    @Mock private CuentaService cuentaService;
    @Mock private ICuentaRepository cuentaRepository;
    @Mock private ITransaccionRepository transaccionRepository;
    @Mock private AuthChecks authChecks;
    @Mock private Authentication authentication;

    @InjectMocks
    private TransferService transferService;

    private Cliente clienteOrigen;
    private Cliente clienteDestino;
    private Cuenta cuentaOrigen;
    private Cuenta cuentaDestino;

    @BeforeEach
    void setUp() {
        clienteOrigen = new Cliente();
        clienteOrigen.setId(1L);
        clienteOrigen.setDni("11111111A");
        clienteOrigen.setNombre("Alice");
        clienteOrigen.setEmail("alice@test.com");
        clienteOrigen.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        clienteOrigen.setFechaCreacion(LocalDateTime.now());

        clienteDestino = new Cliente();
        clienteDestino.setId(2L);
        clienteDestino.setDni("22222222B");
        clienteDestino.setNombre("Bob");
        clienteDestino.setEmail("bob@test.com");
        clienteDestino.setFechaNacimiento(LocalDate.of(1992, 2, 2));
        clienteDestino.setFechaCreacion(LocalDateTime.now());

        cuentaOrigen = new Cuenta();
        cuentaOrigen.setId(1L);
        cuentaOrigen.setNumeroCuenta("ES-ORIGEN-001");
        cuentaOrigen.setSaldo(1000.0);
        cuentaOrigen.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuentaOrigen.setCliente(clienteOrigen);
        cuentaOrigen.setFechaCreacion(LocalDateTime.now());

        cuentaDestino = new Cuenta();
        cuentaDestino.setId(2L);
        cuentaDestino.setNumeroCuenta("ES-DESTINO-002");
        cuentaDestino.setSaldo(200.0);
        cuentaDestino.setTipoCuenta(ETipoCuenta.AHORRO);
        cuentaDestino.setCliente(clienteDestino);
        cuentaDestino.setFechaCreacion(LocalDateTime.now());

        lenient().when(transaccionRepository.save(any(Transaccion.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        lenient().when(cuentaRepository.save(any(Cuenta.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // ===================== transferirDinero - ADMIN =====================

    @Test
    void transferir_adminConSaldoSuficiente_realizaTransferencia() {
        log.info("Test: transferencia correcta como ADMIN");
        mockAdmin();
        when(cuentaRepository.findByNumeroCuenta("ES-ORIGEN-001")).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findByNumeroCuenta("ES-DESTINO-002")).thenReturn(Optional.of(cuentaDestino));
        // Mock cuentaService to return saldo without actually checking auth (it's a separate mock)
        lenient().when(cuentaService.obtenerSaldo(any(), any())).thenReturn(1000.0);
        doNothing().when(cuentaService).actualizarSaldo(anyLong(), anyDouble());

        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigen("ES-ORIGEN-001");
        dto.setCuentaDestino("ES-DESTINO-002");
        dto.setCantidad(300.0);

        TransferenciaDTO result = transferService.transferirDinero(dto, authentication);

        assertNotNull(result);
        assertEquals(300.0, result.getCantidad());
        verify(cuentaService).actualizarSaldo(eq(1L), eq(700.0));
        verify(cuentaRepository).save(argThat(c ->
                c.getId().equals(2L) && c.getSaldo() == 500.0));
        verify(transaccionRepository).save(any(Transaccion.class));
        log.info("Test pasado: transferencia de {} realizada", result.getCantidad());
    }

    @Test
    void transferir_saldoInsuficiente_lanzaExcepcion() {
        log.info("Test: transferencia con saldo insuficiente");
        mockAdmin();
        when(cuentaRepository.findByNumeroCuenta("ES-ORIGEN-001")).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findByNumeroCuenta("ES-DESTINO-002")).thenReturn(Optional.of(cuentaDestino));
        lenient().when(cuentaService.obtenerSaldo(any(), any())).thenReturn(100.0);

        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigen("ES-ORIGEN-001");
        dto.setCuentaDestino("ES-DESTINO-002");
        dto.setCantidad(500.0);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transferService.transferirDinero(dto, authentication));
        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
        log.info("Test pasado: excepcion por saldo insuficiente");
    }

    @Test
    void transferir_cuentaOrigenNoExiste_lanzaExcepcion() {
        log.info("Test: cuenta origen no existe");
        when(cuentaRepository.findByNumeroCuenta("NO-EXISTE")).thenReturn(Optional.empty());

        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigen("NO-EXISTE");
        dto.setCuentaDestino("ES-DESTINO-002");
        dto.setCantidad(100.0);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transferService.transferirDinero(dto, authentication));
        assertTrue(ex.getMessage().contains("origen"));
        log.info("Test pasado: excepcion cuenta origen no encontrada");
    }

    @Test
    void transferir_cuentaDestinoNoExiste_lanzaExcepcion() {
        log.info("Test: cuenta destino no existe");
        when(cuentaRepository.findByNumeroCuenta("ES-ORIGEN-001")).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findByNumeroCuenta("NO-EXISTE")).thenReturn(Optional.empty());

        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigen("ES-ORIGEN-001");
        dto.setCuentaDestino("NO-EXISTE");
        dto.setCantidad(100.0);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transferService.transferirDinero(dto, authentication));
        assertTrue(ex.getMessage().contains("destino"));
        log.info("Test pasado: excepcion cuenta destino no encontrada");
    }

    // ===================== transferirDinero - CLIENTE =====================

    @Test
    void transferir_clientePropietario_realizaTransferencia() {
        log.info("Test: transferencia valida como CLIENTE propietario");
        mockCliente(1L); // cliente 1 es dueno de cuentaOrigen
        when(cuentaRepository.findByNumeroCuenta("ES-ORIGEN-001")).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findByNumeroCuenta("ES-DESTINO-002")).thenReturn(Optional.of(cuentaDestino));
        // Mock cuentaService to return saldo without actually checking auth (it's a separate mock)
        lenient().when(cuentaService.obtenerSaldo(any(), any())).thenReturn(1000.0);
        doNothing().when(cuentaService).actualizarSaldo(anyLong(), anyDouble());

        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigen("ES-ORIGEN-001");
        dto.setCuentaDestino("ES-DESTINO-002");
        dto.setCantidad(100.0);

        assertDoesNotThrow(() -> transferService.transferirDinero(dto, authentication));
        verify(cuentaService).actualizarSaldo(eq(1L), eq(900.0));
        verify(cuentaRepository).save(argThat(c ->
                c.getId().equals(2L) && c.getSaldo() == 300.0));
        verify(transaccionRepository).save(any(Transaccion.class));
        log.info("Test pasado: cliente propietario puede transferir");
    }

    @Test
    void transferir_clienteNoPropietario_lanzaAccessDenied() {
        log.info("Test: cliente sin permiso sobre cuenta origen");

        // Cliente 1 intenta transferir desde cuenta del cliente 2
        lenient().when(authChecks.clienteIdOrNull(authentication)).thenReturn(1L);
        lenient().doThrow(new AccessDeniedException("No tiene permiso")).when(authChecks).isAdmin(authentication);

        when(cuentaRepository.findByNumeroCuenta("ES-ORIGEN-001")).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findByNumeroCuenta("ES-DESTINO-002")).thenReturn(Optional.of(cuentaDestino));

        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigen("ES-ORIGEN-001");
        dto.setCuentaDestino("ES-DESTINO-002");
        dto.setCantidad(50.0);

        assertThrows(AccessDeniedException.class,
                () -> transferService.transferirDinero(dto, authentication));
        log.info("Test pasado: AccessDeniedException para cliente no propietario");
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