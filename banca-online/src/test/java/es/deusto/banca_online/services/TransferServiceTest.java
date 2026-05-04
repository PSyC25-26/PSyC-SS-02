package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.TransferenciaDTO;
import es.deusto.banca_online.entity.*;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final Logger log = LoggerFactory.getLogger(TransferServiceTest.class);

    @Mock private CuentaService cuentaService;
    @Mock private ICuentaRepository cuentaRepository;
    @Mock private ITransaccionRepository transaccionRepository;
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
        when(cuentaService.obtenerSaldo(eq(1L), any())).thenReturn(1000.0);
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
        when(cuentaService.obtenerSaldo(eq(1L), any())).thenReturn(100.0);

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
        when(cuentaService.obtenerSaldo(eq(1L), any())).thenReturn(1000.0);
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
        log.info("Test: cliente intenta transferir desde cuenta ajena");
        mockCliente(99L); // cliente 99, pero la cuenta pertenece al 1
        when(cuentaRepository.findByNumeroCuenta("ES-ORIGEN-001")).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findByNumeroCuenta("ES-DESTINO-002")).thenReturn(Optional.of(cuentaDestino));

        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setCuentaOrigen("ES-ORIGEN-001");
        dto.setCuentaDestino("ES-DESTINO-002");
        dto.setCantidad(100.0);

        assertThrows(AccessDeniedException.class,
                () -> transferService.transferirDinero(dto, authentication));
        log.info("Test pasado: AccessDeniedException para cliente no propietario");
    }

    // ===================== helpers =====================

    private void mockAdmin() {
        var auth = new SimpleGrantedAuthority("ROLE_ADMIN");
        doReturn(List.of(auth)).when(authentication).getAuthorities();
    }

    private void mockCliente(Long clienteId) {
        var auth = new SimpleGrantedAuthority("ROLE_CLIENTE");
        doReturn(List.of(auth)).when(authentication).getAuthorities();
        Usuario usuario = new Usuario();
        usuario.setClienteId(clienteId);
        when(authentication.getPrincipal()).thenReturn(usuario);
    }
}