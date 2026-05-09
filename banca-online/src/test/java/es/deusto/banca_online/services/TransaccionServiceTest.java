package es.deusto.banca_online.services;


import es.deusto.banca_online.dto.TransaccionResponse;
import es.deusto.banca_online.entity.Cuenta;
import es.deusto.banca_online.entity.ETipoTransaccion;
import es.deusto.banca_online.entity.Transaccion;
import es.deusto.banca_online.repository.ITransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para {@link TransaccionService}.
 * Verifica la correcta recuperación del historial de movimientos y 
 * la generación dinámica de descripciones según el tipo de transacción.
 */
@ExtendWith(MockitoExtension.class)
public class TransaccionServiceTest {

    @Mock
    private ITransaccionRepository transaccionRepository;

    @InjectMocks
    private TransaccionService transaccionService;

    // Variables globales para los tests
    private Cuenta cuentaPrincipal;
    private Cuenta cuentaSecundaria;

    /**
     * Método de configuración que se ejecuta antes de cada test.
     * Prepara los datos simulados que vamos a utilizar en las pruebas.
     */
    @BeforeEach
    void setUp() {
        // Configuramos la cuenta principal (sobre la que haremos las consultas)
        cuentaPrincipal = new Cuenta();
        cuentaPrincipal.setId(1L);
        cuentaPrincipal.setNumeroCuenta("ES1111111111111111111111");

        // Configuramos una cuenta secundaria (para simular transferencias)
        cuentaSecundaria = new Cuenta();
        cuentaSecundaria.setId(2L);
        cuentaSecundaria.setNumeroCuenta("ES2222222222222222222222");
    }

    /**
     * Prueba 1: Verifica que si una cuenta no tiene transacciones en la base de datos,
     * el servicio devuelve una lista vacía de forma segura sin lanzar errores.
     */
    @Test
    @DisplayName("Debe devolver una lista vacía si la cuenta no tiene historial")
    void testObtenerHistorial_SinTransacciones() {
        // 1. Arrange (Preparación)
        Long cuentaId = cuentaPrincipal.getId();
        when(transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(cuentaId, cuentaId))
                .thenReturn(Collections.emptyList());

        // 2. Act (Ejecución)
        List<TransaccionResponse> resultado = transaccionService.obtenerHistorial(cuentaId);

        // 3. Assert (Verificación)
        assertNotNull(resultado, "El resultado no debería ser nulo");
        assertTrue(resultado.isEmpty(), "La lista de transacciones debería estar vacía");
        
        // Verificamos que el repositorio fue llamado exactamente una vez con los parámetros correctos
        verify(transaccionRepository, times(1))
                .findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(cuentaId, cuentaId);
    }

    /**
     * Prueba 2: Verifica el comportamiento de la descripción inteligente 
     * cuando la cuenta principal es el DESTINO de una transferencia.
     */
    @Test
    @DisplayName("Debe generar la descripción 'Transferencia RECIBIDA' cuando la cuenta es destino")
    void testObtenerHistorial_TransferenciaRecibida() {
        // 1. Arrange
        Long cuentaId = cuentaPrincipal.getId();
        Transaccion transferencia = new Transaccion();
        transferencia.setId(100L);
        transferencia.setTotal(500.0);
        transferencia.setFecha(LocalDateTime.now());
        transferencia.setTipo(ETipoTransaccion.TRANSFERENCIA);
        transferencia.setCuentaOrigen(cuentaSecundaria);
        transferencia.setCuentaDestino(cuentaPrincipal); // La cuenta principal recibe el dinero

        when(transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(cuentaId, cuentaId))
                .thenReturn(Arrays.asList(transferencia));

        // 2. Act
        List<TransaccionResponse> resultado = transaccionService.obtenerHistorial(cuentaId);

        // 3. Assert
        assertEquals(1, resultado.size(), "Debería haber exactamente 1 transacción en el historial");
        TransaccionResponse response = resultado.get(0);
        
        assertEquals(100L, response.getId());
        assertEquals(500.0, response.getTotal());
        assertEquals(ETipoTransaccion.TRANSFERENCIA.name(), response.getTipo());
        assertEquals(cuentaSecundaria.getNumeroCuenta(), response.getCuentaOrigenNum());
        assertEquals(cuentaPrincipal.getNumeroCuenta(), response.getCuentaDestinoNum());
        
        // Comprobación clave: la lógica de descripción inteligente
        assertEquals("Transferencia RECIBIDA de " + cuentaSecundaria.getNumeroCuenta(), response.getDescripcion());
    }

    /**
     * Prueba 3: Verifica el comportamiento de la descripción inteligente 
     * cuando la cuenta principal es el ORIGEN de una transferencia.
     */
    @Test
    @DisplayName("Debe generar la descripción 'Transferencia ENVIADA' cuando la cuenta es origen")
    void testObtenerHistorial_TransferenciaEnviada() {
        // 1. Arrange
        Long cuentaId = cuentaPrincipal.getId();
        Transaccion transferencia = new Transaccion();
        transferencia.setId(101L);
        transferencia.setTotal(300.0);
        transferencia.setFecha(LocalDateTime.now());
        transferencia.setTipo(ETipoTransaccion.TRANSFERENCIA);
        transferencia.setCuentaOrigen(cuentaPrincipal); // La cuenta principal envía el dinero
        transferencia.setCuentaDestino(cuentaSecundaria);

        when(transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(cuentaId, cuentaId))
                .thenReturn(Arrays.asList(transferencia));

        // 2. Act
        List<TransaccionResponse> resultado = transaccionService.obtenerHistorial(cuentaId);

        // 3. Assert
        assertFalse(resultado.isEmpty());
        TransaccionResponse response = resultado.get(0);
        
        // Comprobación clave: la lógica de descripción inteligente cambia
        assertEquals("Transferencia ENVIADA a " + cuentaSecundaria.getNumeroCuenta(), response.getDescripcion());
    }

    /**
     * Prueba 4: Verifica que las transacciones simples (Depósitos o Retiros)
     * conservan su descripción original introducida por el usuario.
     */
    @Test
    @DisplayName("Debe conservar la descripción original para DEPÓSITOS y RETIROS")
    void testObtenerHistorial_DepositoMantieneDescripcion() {
        // 1. Arrange
        Long cuentaId = cuentaPrincipal.getId();
        Transaccion deposito = new Transaccion();
        deposito.setId(102L);
        deposito.setTotal(1000.0);
        deposito.setFecha(LocalDateTime.now());
        deposito.setTipo(ETipoTransaccion.DEPOSITO);
        deposito.setDescripcion("Ingreso de nómina");
        deposito.setCuentaDestino(cuentaPrincipal); // En los depósitos origen es nulo

        when(transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(cuentaId, cuentaId))
                .thenReturn(Arrays.asList(deposito));

        // 2. Act
        List<TransaccionResponse> resultado = transaccionService.obtenerHistorial(cuentaId);

        // 3. Assert
        TransaccionResponse response = resultado.get(0);
        
        assertEquals(ETipoTransaccion.DEPOSITO.name(), response.getTipo());
        assertEquals("N/A", response.getCuentaOrigenNum(), "Si no hay cuenta de origen, debe ser 'N/A'");
        assertEquals(cuentaPrincipal.getNumeroCuenta(), response.getCuentaDestinoNum());
        
        // Comprobación clave: No modifica la descripción original
        assertEquals("Ingreso de nómina", response.getDescripcion());
    }
}
