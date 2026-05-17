package es.deusto.banca_online.services;

/**
 * @file CuentaServiceTest.java
 * @brief Tests unitarios del servicio de lógica de negocio de cuentas bancarias.
 * @details Verifica la creación de cuentas, depósitos, retiros, consulta de saldo
 *          y borrado lógico, usando mocks para aislar la lógica del servicio.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.entity.*;
import es.deusto.banca_online.repository.IClienteRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CuentaServiceTest.class);

    /**
     * Repositorio de cuentas (Mock).
     * Simula la persistencia de productos financieros para validar la lógica del servicio.
     */
    @Mock private ICuentaRepository cuentaRepository;

    /**
     * Repositorio de clientes (Mock).
     * Utilizado para verificar la existencia del titular antes de crear una cuenta.
     */
    @Mock private IClienteRepository clienteRepository;

    /**
     * Repositorio de transacciones (Mock).
     * Simula el registro histórico de movimientos (depósitos/retiros).
     */
    @Mock private ITransaccionRepository transaccionRepository;

    /**
     * Componente de verificación de seguridad (Mock).
     * Valida permisos de propiedad y roles de usuario sobre las cuentas.
     */
    @Mock private AuthChecks authChecks;

    /**
     * Contexto de autenticación de Spring Security (Mock).
     * Representa al usuario que intenta realizar operaciones bancarias.
     */
    @Mock private Authentication authentication;

    /**
     * Servicio de cuentas bajo prueba.
     * Integra los mocks para validar la lógica de negocio de gestión de saldos.
     */
    @InjectMocks
    private CuentaService cuentaService;

    private Cliente cliente;
    private Cuenta cuenta;

    /**
     * Configuración previa a cada test.
     * Inicializa una entidad Cliente y una Cuenta base para evitar redundancia en los escenarios.
     */
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

    /**
     * Test de creación exitosa.
     * Valida que el servicio asigne correctamente el titular, el tipo de cuenta 
     * y el saldo inicial solicitado.
     */
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

    /**
     * Test de validación de negocio: Saldo por defecto.
     * Verifica que si no se indica saldo inicial, el sistema inicialice la cuenta con 0.0.
     */
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

    /**
     * Test de error: Cliente inexistente.
     * Asegura que el sistema no permita abrir cuentas para identificadores de cliente no registrados.
     */
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

    /**
     * Test de validación: Tipo de cuenta inválido.
     * Verifica que el sistema lance una excepción cuando se intenta crear una cuenta
     * con un tipo que no coincide con el enumerado ETipoCuenta (ej. "TIPO_INVALIDO").
     * Esto asegura la integridad de los datos en la base de datos.
     */
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

    /**
     * Test de recuperación de cuentas.
     * Valida que, dado un ID de cliente válido, el servicio retorne una lista 
     * con todas las cuentas asociadas correctamente mapeadas a objetos CuentaResponse.
     */
    @Test
    void obtenerCuentasPorCliente_retornaLista() {
        log.info("Test: obtenerCuentasPorCliente");
        when(cuentaRepository.findByClienteId(1L)).thenReturn(List.of(cuenta));

        List<CuentaResponse> lista = cuentaService.obtenerCuentasPorCliente(1L);

        assertEquals(1, lista.size());
        assertEquals("ES1234567890ABCDEF12", lista.get(0).getNumeroCuenta());
        log.info("Test pasado: {} cuentas encontradas", lista.size());
    }

    /**
     * Test de recuperación sin resultados.
     * Asegura que el servicio no devuelva null ni lance excepciones si el cliente
     * no tiene cuentas abiertas, sino una lista vacía, cumpliendo con el contrato del API.
     */
    @Test
    void obtenerCuentasPorCliente_sinCuentas_retornaListaVacia() {
        log.info("Test: obtenerCuentasPorCliente sin cuentas");
        when(cuentaRepository.findByClienteId(99L)).thenReturn(List.of());

        List<CuentaResponse> lista = cuentaService.obtenerCuentasPorCliente(99L);
        assertTrue(lista.isEmpty());
        log.info("Test pasado: lista vacia correctamente");
    }

    // ===================== obtenerSaldo =====================

    /**
     * Test de permisos: Rol Administrador.
     * Valida que un administrador pueda consultar el saldo de cualquier cuenta sin restricciones.
     */
    @Test
    void obtenerSaldo_admin_retornaSaldo() {
        log.info("Test: obtenerSaldo como ADMIN");
        mockAdmin();
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        Double saldo = cuentaService.obtenerSaldo(1L, authentication);
        assertEquals(500.0, saldo);
        log.info("Test pasado: saldo={}", saldo);
    }

    /**
     * Test de error en consulta de saldo.
     * Verifica que el sistema gestione correctamente el intento de consultar
     * el saldo de una cuenta que no existe en el repositorio (ID 99), 
     * lanzando una RuntimeException controlada.
     */
    @Test
    void obtenerSaldo_cuentaNoExiste_lanzaExcepcion() {
        log.info("Test: obtenerSaldo cuenta inexistente");
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cuentaService.obtenerSaldo(99L, authentication));
        log.info("Test pasado: excepcion lanzada");
    }

    /**
     * Test de seguridad: Acceso denegado.
     * Verifica que un cliente no pueda consultar el saldo de una cuenta que no le pertenece,
     * lanzando una AccessDeniedException.
     */
    @Test
    void obtenerSaldo_clienteSinPermiso_lanzaAccessDenied() {
        log.info("Test: obtenerSaldo sin permiso");
        mockCliente(2L); // cliente con id 2, pero la cuenta pertenece al cliente 1

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        doThrow(new AccessDeniedException("No tiene permiso")).when(authChecks).assertOwnsCuenta(authentication, cuenta);

        assertThrows(AccessDeniedException.class,
                () -> cuentaService.obtenerSaldo(1L, authentication));
        log.info("Test pasado: AccessDeniedException lanzada");
    }

    // ===================== depositarDinero =====================

    /**
     * Test funcional: Depósito.
     * Comprueba que los depósitos válidos incrementen el saldo y generen un registro de transacción.
     */
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

    /**
     * Test de robustez: Depósito con valores nulos o vacíos.
     * Verifica que el sistema impida transacciones donde el monto sea nulo o exactamente cero,
     * lanzando una IllegalArgumentException para proteger la integridad del libro contable.
     */
    @Test
    void depositarDinero_montoNulo_lanzaExcepcion() {
        log.info("Test: depositar monto nulo");
        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.depositarDinero(1L, null, authentication));
        log.info("Test pasado: excepcion por monto nulo");
    }

    /**
     * Test de validación: Depósito de valor cero.
     * Asegura que no se procesen transacciones de depósito sin valor económico real,
     * manteniendo la limpieza de los registros históricos.
     */
    @Test
    void depositarDinero_montoCero_lanzaExcepcion() {
        log.info("Test: depositar monto cero");
        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.depositarDinero(1L, 0.0, authentication));
        log.info("Test pasado: excepcion por monto cero");
    }

    /**
     * Test de validación de integridad: Monto no válido.
     * Asegura que el sistema rechace depósitos de cuantía cero o negativa.
     */
    @Test
    void depositarDinero_montoNegativo_lanzaExcepcion() {
        log.info("Test: depositar monto negativo");
        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.depositarDinero(1L, -100.0, authentication));
        log.info("Test pasado: excepcion por monto negativo");
    }

    // ===================== retirarDinero =====================

    /**
     * Test funcional: Retiro.
     * Valida que un retiro disminuya el saldo disponible correctamente.
     */
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

    /**
     * Test de validación de integridad: Saldo insuficiente.
     * Verifica la regla de oro bancaria: no se puede retirar más dinero del disponible.
     */
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

    /**
     * Test de validación: Retiro de valor nulo.
     * Comprueba que el sistema gestione correctamente la ausencia de un monto en la solicitud
     * de retiro mediante el lanzamiento de una excepción controlada.
     */
    @Test
    void retirarDinero_montoNulo_lanzaExcepcion() {
        log.info("Test: retirar monto nulo");
        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.retirarDinero(1L, null, authentication));
    }

    /**
     * Test de validación: Retiro de valor negativo.
     * Verifica que no se puedan realizar retiros con importes negativos, lo cual
     * podría ser explotado para incrementar el saldo de forma fraudulenta.
     */
    @Test
    void retirarDinero_montoNegativo_lanzaExcepcion() {
        log.info("Test: retirar monto negativo");
        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.retirarDinero(1L, -50.0, authentication));
    }

    // ===================== actualizarSaldo =====================

    /**
     * Test funcional: Actualización directa de saldo.
     * Valida que el servicio sea capaz de modificar el balance de una cuenta existente
     * y persistir el cambio correctamente en el repositorio.
     */
    @Test
    void actualizarSaldo_cuentaExiste_actualizaCorrectamente() {
        log.info("Test: actualizarSaldo");
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> cuentaService.actualizarSaldo(1L, 1000.0));
        log.info("Test pasado: saldo actualizado");
    }

    /**
     * Test de error: Actualización de saldo en cuenta inexistente.
     * Asegura que el intento de modificar el saldo de una cuenta que no figura en la base de datos
     * sea detectado y notificado mediante una excepción.
     */
    @Test
    void actualizarSaldo_cuentaNoExiste_lanzaExcepcion() {
        log.info("Test: actualizarSaldo cuenta inexistente");
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cuentaService.actualizarSaldo(99L, 100.0));
    }

    // ===================== helpers =====================

    /**
     * Configura el mock de autenticación para actuar como un usuario con rol ADMIN.
     */
    private void mockAdmin() {
        var authority = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN");
        lenient().doReturn(List.of(authority)).when(authentication).getAuthorities();
        lenient().when(authChecks.isAdmin(authentication)).thenReturn(true);
    }

    /**
     * Configura el mock de autenticación para actuar como un usuario con rol CLIENTE.
     * @param clienteId Identificador del cliente que será dueño de la sesión simulada.
     */
    private void mockCliente(Long clienteId) {
        var authority = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE");
        lenient().doReturn(List.of(authority)).when(authentication).getAuthorities();
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        Usuario usuario = new Usuario();
        usuario.setCliente(cliente);
        lenient().when(authentication.getPrincipal()).thenReturn(usuario);
        lenient().when(authChecks.isAdmin(authentication)).thenReturn(false);
        lenient().when(authChecks.clienteIdOrNull(authentication)).thenReturn(clienteId);
    }

    // ===================== eliminarCuentaInactiva =====================

    @Test
    void eliminarCuentaInactiva_saldoCero_desactivaCuenta() {
        // Cuenta con saldo 0 se puede desactivar (borrado logico)
        Cuenta cuentaSaldoCero = new Cuenta();
        cuentaSaldoCero.setId(10L);
        cuentaSaldoCero.setNumeroCuenta("ES-DEL-001");
        cuentaSaldoCero.setSaldo(0.0);
        cuentaSaldoCero.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuentaSaldoCero.setCliente(cliente);
        cuentaSaldoCero.setActiva(true);

        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuentaSaldoCero));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaSaldoCero);

        cuentaService.eliminarCuentaInactiva(10L);

        // Verificamos que la cuenta queda desactivada y se guarda
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
        assertFalse(cuentaSaldoCero.getActiva());
    }

    @Test
    void eliminarCuentaInactiva_saldoPositivo_lanzaIllegalArgument() {
        // Cuenta con saldo > 0 NO se puede eliminar (restriccion de negocio)
        Cuenta cuentaConSaldo = new Cuenta();
        cuentaConSaldo.setId(10L);
        cuentaConSaldo.setSaldo(500.0);
        cuentaConSaldo.setCliente(cliente);

        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuentaConSaldo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cuentaService.eliminarCuentaInactiva(10L));

        // Verificamos el mensaje y que NO se llama a save
        assertTrue(ex.getMessage().contains("saldo"));
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @Test
    void eliminarCuentaInactiva_cuentaNoExiste_lanzaRuntimeException() {
        // Cuenta inexistente lanza excepcion
        when(cuentaRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cuentaService.eliminarCuentaInactiva(999L));

        assertEquals("Cuenta no encontrada", ex.getMessage());
    }

    // ===================== actualizarSaldo =====================

    @Test
    void actualizarSaldo_cuentaExistente_actualizaCorrectamente() {
        // Actualizar el saldo de una cuenta existente
        Cuenta cuentaParaActualizar = new Cuenta();
        cuentaParaActualizar.setId(10L);
        cuentaParaActualizar.setSaldo(100.0);
        cuentaParaActualizar.setCliente(cliente);

        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuentaParaActualizar));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaParaActualizar);

        cuentaService.actualizarSaldo(10L, 500.0);

        // Verificamos que el saldo se actualizo y se guardo
        assertEquals(500.0, cuentaParaActualizar.getSaldo());
        verify(cuentaRepository, times(1)).save(cuentaParaActualizar);
    }

    @Test
    void actualizarSaldo_cuentaNoExiste_lanzaRuntimeException() {
        // Actualizar saldo de cuenta inexistente lanza excepcion
        when(cuentaRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cuentaService.actualizarSaldo(999L, 100.0));

        assertEquals("Cuenta no encontrada", ex.getMessage());
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    // ===================== obtenerCuentasPorCliente con Authentication =====================

    @Test
    void obtenerCuentasPorCliente_conAuthentication_validaPropietarioYDevuelveLista() {
        // Verificamos que la sobrecarga con Authentication valida permisos antes de devolver datos
        Cuenta cuentaCliente = new Cuenta();
        cuentaCliente.setId(10L);
        cuentaCliente.setNumeroCuenta("ES-AUTH-001");
        cuentaCliente.setSaldo(100.0);
        cuentaCliente.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuentaCliente.setCliente(cliente);
        cuentaCliente.setFechaCreacion(LocalDateTime.now());

        when(cuentaRepository.findByClienteId(1L)).thenReturn(List.of(cuentaCliente));

        var result = cuentaService.obtenerCuentasPorCliente(1L,
                mock(org.springframework.security.core.Authentication.class));

        // Verificaciones
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(authChecks, times(1)).assertOwnsCliente(any(), eq(1L));
    }
}