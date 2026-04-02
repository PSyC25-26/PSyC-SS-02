package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.Cuenta;
import es.deusto.banca_online.entity.ETipoCuenta;
import es.deusto.banca_online.repository.IClienteRepository;
import es.deusto.banca_online.repository.ICuentaRepository;
import es.deusto.banca_online.repository.ITransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)  // Habilita Mockito
class CuentaServiceTest {

    @Mock  // Crea un repositorio falso que simula el comportamiento de la base de datos
    private ICuentaRepository cuentaRepository;

    @Mock
    private IClienteRepository clienteRepository;

    @InjectMocks  // Inyecta los repositorios simulados dentro de CuentaService
    private CuentaService cuentaService;

    @Mock
    private ITransaccionRepository transaccionRepository;

    private CuentaRequest requestValido;
    private Cliente clienteExistente;
    private Cuenta cuentaGuardada;
    private List<CuentaResponse> cuentasMock;


    // Preparacion antes de cada test. Así no repetimos código en cada test.
    @BeforeEach  // Se ejecuta antes de cada test
    void setUp() {
        // Preparo un cliente existente
        clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setDni("12345678A");
        clienteExistente.setNombre("Juan");
        clienteExistente.setEmail("juan@test.com");
        clienteExistente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        clienteExistente.setFechaCreacion(LocalDateTime.now());

        // Preparo un request válido
        requestValido = new CuentaRequest();
        requestValido.setClienteId(1L);
        requestValido.setTipoCuenta(ETipoCuenta.CORRIENTE.name());
        requestValido.setSaldoInicial(1000.0);

        // Preparo la cuenta que "devolverá" el repositorio
        cuentaGuardada = new Cuenta();
        cuentaGuardada.setId(1L);
        cuentaGuardada.setNumeroCuenta("ES123456789012345678");
        cuentaGuardada.setSaldo(1000.0);
        cuentaGuardada.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuentaGuardada.setCliente(clienteExistente);
        // NOTA: No se llama a setFechaCreacion porque no existe setter en la entidad Cuenta

        // Segunda cuenta para la lista
        Cuenta cuenta2 = new Cuenta();
        cuenta2.setId(2L);
        cuenta2.setNumeroCuenta("ES876543210987654321");
        cuenta2.setSaldo(500.0);
        cuenta2.setTipoCuenta(ETipoCuenta.AHORRO);
        cuenta2.setCliente(clienteExistente);
        // NOTA: No se llama a setFechaCreacion porque no existe setter en la entidad Cuenta

        // Preparo las respuestas mock para los tests
        CuentaResponse response1 = new CuentaResponse();
        response1.setId(1L);
        response1.setNumeroCuenta("ES123456789012345678");
        response1.setSaldo(1000.0);
        response1.setTipoCuenta(ETipoCuenta.CORRIENTE.name());
        response1.setClienteId(1L);

        CuentaResponse response2 = new CuentaResponse();
        response2.setId(2L);
        response2.setNumeroCuenta("ES876543210987654321");
        response2.setSaldo(500.0);
        response2.setTipoCuenta(ETipoCuenta.AHORRO.name());
        response2.setClienteId(1L);

        cuentasMock = Arrays.asList(response1, response2);
    }


    // DATOS VÁLIDOS - CREAR
    @Test
    void crearCuenta_DatosValidos_CuentaGuardadaCorrectamente() {
        // Configuro el comportamiento del mock
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaGuardada);

        // Ejecutamos el método a testear
        CuentaResponse resultado = cuentaService.crearCuenta(requestValido);

        // Verificamos los resultados
        assertNotNull(resultado);                       // No debe ser null
        assertEquals(1L, resultado.getId());            // Debe tener ID 1
        assertEquals("ES123456789012345678", resultado.getNumeroCuenta()); // Número de cuenta correcto
        assertEquals(1000.0, resultado.getSaldo());     // Saldo correcto
        assertEquals("CORRIENTE", resultado.getTipoCuenta()); // Tipo correcto
        assertEquals(1L, resultado.getClienteId());     // Cliente ID correcto

        // Verificamos que hemos llamado a los metodos del repositorio
        verify(clienteRepository, times(1)).findById(1L);
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));

        System.out.println("Test crearCuenta_DatosValidos pasado");
    }


    // Saldo inicial nulo
    @Test
    void crearCuenta_SaldoInicialNulo_EstableceSaldoCero() {
        // Request sin saldo inicial
        requestValido.setSaldoInicial(null);

        // Creo una cuenta con saldo 0
        Cuenta cuentaConSaldoCero = new Cuenta();
        cuentaConSaldoCero.setId(1L);
        cuentaConSaldoCero.setNumeroCuenta("ES123456789012345678");
        cuentaConSaldoCero.setSaldo(0.0);  // Saldo debería ser 0
        cuentaConSaldoCero.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuentaConSaldoCero.setCliente(clienteExistente);
        // NOTA: No se llama a setFechaCreacion porque no existe setter en la entidad Cuenta

        // Configuro el comportamiento del mock
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaConSaldoCero);

        // Ejecutamos el método a testear
        CuentaResponse resultado = cuentaService.crearCuenta(requestValido);

        // Verificamos que el saldo es 0
        assertNotNull(resultado);
        assertEquals(0.0, resultado.getSaldo());

        System.out.println("Test crearCuenta_SaldoInicialNulo pasado");
    }


    // Cliente no existe
    @Test
    void crearCuenta_ClienteNoExiste_LanzaExcepcion() {
        // Simulamos que el cliente NO EXISTE
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        requestValido.setClienteId(999L);

        // Verificamos que lanza excepcion
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cuentaService.crearCuenta(requestValido);
        });

        assertEquals("Cliente no encontrado con id: 999", exception.getMessage());

        // Verificamos que NUNCA se haya llamado a save
        verify(cuentaRepository, never()).save(any(Cuenta.class));

        System.out.println("Test crearCuenta_ClienteNoExiste pasado");
    }


    // Tipo de cuenta inválido
    @Test
    void crearCuenta_TipoCuentaInvalido_LanzaExcepcion() {
        // Request con tipo de cuenta inválido
        requestValido.setTipoCuenta("INVALIDO");

        // Configuro el comportamiento del mock
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));

        // Verificamos que lanza excepcion
        assertThrows(IllegalArgumentException.class, () -> {
            cuentaService.crearCuenta(requestValido);
        });

        // Verificamos que NUNCA se haya llamado a save
        verify(cuentaRepository, never()).save(any(Cuenta.class));

        System.out.println("Test crearCuenta_TipoCuentaInvalido pasado");
    }


    // DATOS VÁLIDOS - OBTENER CUENTAS POR CLIENTE
    @Test
    void obtenerCuentasPorCliente_ClienteExiste_DeberiaRetornarLista() {
        // Preparo las cuentas que devolverá el repositorio
        Cuenta cuenta1 = new Cuenta();
        cuenta1.setId(1L);
        cuenta1.setNumeroCuenta("ES123456789012345678");
        cuenta1.setSaldo(1000.0);
        cuenta1.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuenta1.setCliente(clienteExistente);

        Cuenta cuenta2 = new Cuenta();
        cuenta2.setId(2L);
        cuenta2.setNumeroCuenta("ES876543210987654321");
        cuenta2.setSaldo(500.0);
        cuenta2.setTipoCuenta(ETipoCuenta.AHORRO);
        cuenta2.setCliente(clienteExistente);

        List<Cuenta> cuentas = Arrays.asList(cuenta1, cuenta2);

        // Configuro el comportamiento del mock
        when(cuentaRepository.findByClienteId(1L)).thenReturn(cuentas);

        // Llamamos al método
        List<CuentaResponse> resultado = cuentaService.obtenerCuentasPorCliente(1L);

        // Validamos
        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(CuentaResponse::getNumeroCuenta)
                .containsExactly("ES123456789012345678", "ES876543210987654321");
        assertThat(resultado).extracting(CuentaResponse::getSaldo)
                .containsExactly(1000.0, 500.0);
        assertThat(resultado).extracting(CuentaResponse::getTipoCuenta)
                .containsExactly("CORRIENTE", "AHORRO");

        // Validamos que se ha llamado al método 1 vez
        verify(cuentaRepository, times(1)).findByClienteId(1L);

        System.out.println("Test obtenerCuentasPorCliente_ClienteExiste pasado");
    }


    @Test
    void obtenerCuentasPorCliente_ClienteNoTieneCuentas_DeberiaRetornarListaVacia() {
        // Configuro el comportamiento del mock para devolver lista vacía
        when(cuentaRepository.findByClienteId(1L)).thenReturn(Arrays.asList());

        // Llamamos al método
        List<CuentaResponse> resultado = cuentaService.obtenerCuentasPorCliente(1L);

        // Validamos que está vacío
        assertThat(resultado).isEmpty();

        // Validamos que se ha llamado al método 1 vez
        verify(cuentaRepository, times(1)).findByClienteId(1L);

        System.out.println("Test obtenerCuentasPorCliente_ClienteNoTieneCuentas pasado");
    }


    // Número de cuenta único - Verificación indirecta
    @Test
    void crearCuenta_GeneraNumeroCuentaUnico() {
        // Configuro el comportamiento del mock
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaGuardada);

        // Ejecutamos el método a testear
        CuentaResponse resultado = cuentaService.crearCuenta(requestValido);

        // Verificamos que el número de cuenta no es nulo y tiene el formato esperado
        assertNotNull(resultado.getNumeroCuenta());
        assertTrue(resultado.getNumeroCuenta().startsWith("ES"));
        assertEquals(20, resultado.getNumeroCuenta().length()); // "ES" + 18 caracteres

        System.out.println("Test crearCuenta_GeneraNumeroCuentaUnico pasado");
    }

    //SALDO CORRECTO
    @Test
    void obtenerSaldo_CuentaExiste_DevuelveSaldoCorrecto() {
        // GIVEN
        Long cuentaId = 1L;
        Double saldoEsperado = 1000.0;

        Cuenta cuenta = new Cuenta();
        cuenta.setId(cuentaId);
        cuenta.setSaldo(saldoEsperado);

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        // WHEN
        Double saldo = cuentaService.obtenerSaldo(cuentaId);

        // THEN
        assertEquals(saldoEsperado, saldo);
        verify(cuentaRepository, times(1)).findById(cuentaId);

        System.out.println("Test obtenerSaldo_CuentaExiste pasado");
    }

    //CUENTA NO EXISTE
    @Test
    void obtenerSaldo_CuentaNoExiste_LanzaExcepcion() {
        // GIVEN
        Long cuentaId = 999L;

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.empty());

        // WHEN + THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cuentaService.obtenerSaldo(cuentaId);
        });

        assertEquals("Cuenta no encontrada", exception.getMessage());
        verify(cuentaRepository, times(1)).findById(cuentaId);

        System.out.println("Test obtenerSaldo_CuentaNoExiste pasado");
    }

    // ==========================================
    // TESTS PARA DEPOSITAR DINERO
    // ==========================================

    @Test
    void depositarDinero_DatosValidos_AumentaSaldoYRegistraTransaccion() {
        // GIVEN: Una cuenta con 1000€ y queremos depositar 500€
        Long cuentaId = 1L;
        Double montoDeposito = 500.0;

        Cuenta cuenta = new Cuenta();
        cuenta.setId(cuentaId);
        cuenta.setSaldo(1000.0);
        cuenta.setNumeroCuenta("ES123");
        cuenta.setCliente(clienteExistente);
        cuenta.setTipoCuenta(ETipoCuenta.CORRIENTE);

        when(cuentaRepository.findById(cuentaId)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuenta);

        // WHEN: Ejecutamos el depósito
        CuentaResponse response = cuentaService.depositarDinero(cuentaId, montoDeposito);

        // THEN: El saldo debe ser 1500€
        assertEquals(1500.0, response.getSaldo());

        // Verificamos que se guardó la cuenta y la transacción
        verify(cuentaRepository, times(1)).save(cuenta);
        // import es.deusto.banca_online.entity.Transaccion; (Asegúrate de tener el import)
        verify(transaccionRepository, times(1)).save(any());

        System.out.println("Test depositarDinero_DatosValidos pasado");
    }

    @Test
    void depositarDinero_MontoInvalido_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            cuentaService.depositarDinero(1L, -50.0);
        });
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void retirarDinero_DatosValidos_DisminuyeSaldoYRegistraTransaccion() {
        // GIVEN: Una cuenta con 1000€ y queremos retirar 200€
        Long cuentaId = 1L;
        Double montoRetiro = 200.0;

        Cuenta cuenta = new Cuenta();
        cuenta.setId(cuentaId);
        cuenta.setSaldo(1000.0);
        cuenta.setNumeroCuenta("ES123");
        cuenta.setCliente(clienteExistente);
        cuenta.setTipoCuenta(ETipoCuenta.CORRIENTE);

        when(cuentaRepository.findById(cuentaId)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuenta);

        // WHEN: Ejecutamos el retiro
        CuentaResponse response = cuentaService.retirarDinero(cuentaId, montoRetiro);

        // THEN: El saldo debe ser 800€
        assertEquals(800.0, response.getSaldo());
        verify(cuentaRepository, times(1)).save(cuenta);
        verify(transaccionRepository, times(1)).save(any());

        System.out.println("Test retirarDinero_DatosValidos pasado");
    }

    @Test
    void retirarDinero_SaldoInsuficiente_LanzaExcepcion() {
        // GIVEN: Una cuenta con 50€ y queremos retirar 1000€
        Cuenta cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setSaldo(50.0);

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        // WHEN & THEN: Debe lanzar excepción de argumento ilegal
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            cuentaService.retirarDinero(1L, 1000.0);
        });

        assertEquals("Saldo insuficiente para realizar el retiro", exception.getMessage());
        verify(cuentaRepository, never()).save(any());
    }
}