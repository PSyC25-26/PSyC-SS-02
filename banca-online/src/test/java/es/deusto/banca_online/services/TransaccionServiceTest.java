package es.deusto.banca_online.services;

/**
 * @file TransaccionServiceTest.java
 * @brief Tests unitarios del servicio de historial de transacciones.
 * @details Verifica la recuperación y el mapeo correcto de movimientos bancarios
 *          (depósitos, retiros y transferencias) para su presentación al usuario.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

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
        // =========================================================
        // 1. Arrange (Preparación de datos)
        // =========================================================

        Long cuentaId = cuentaPrincipal.getId();

        List<Transaccion> historialVacio = Collections.emptyList();

        when(
                transaccionRepository
                        .findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(
                                cuentaId,
                                cuentaId
                        )
        ).thenReturn(historialVacio);

        // =========================================================
        // 2. Act (Ejecución del método a probar)
        // =========================================================

        List<TransaccionResponse> resultado =
                transaccionService.obtenerHistorial(cuentaId);

        // =========================================================
        // 3. Assert (Verificaciones)
        // =========================================================

        // Verificamos que el resultado exista
        assertNotNull(
                resultado,
                "El resultado no debería ser null"
        );

        // Comprobamos explícitamente si la lista está vacía
        boolean listaVacia = resultado.isEmpty();

        assertTrue(
                listaVacia,
                "La lista de transacciones debería estar vacía"
        );

        // Verificamos también el tamaño exacto
        int numeroElementos = resultado.size();

        assertEquals(
                0,
                numeroElementos,
                "El número de transacciones debería ser exactamente 0"
        );

        // Comprobamos que la referencia de lista no haya cambiado inesperadamente
        assertNotSame(
                historialVacio,
                resultado,
                "El servicio debería devolver una nueva lista transformada"
        );

        // =========================================================
        // 4. Verificación de interacción con el repositorio
        // =========================================================

        verify(transaccionRepository, times(1))
                .findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(
                        cuentaId,
                        cuentaId
                );

        // Nos aseguramos de que no existan más llamadas inesperadas
        verifyNoMoreInteractions(transaccionRepository);
    }

    /**
     * Prueba 2: Verifica el comportamiento de la descripción inteligente 
     * cuando la cuenta principal es el DESTINO de una transferencia.
     */
    @Test
    @DisplayName("Debe generar la descripción 'Transferencia RECIBIDA' cuando la cuenta es destino")
    void testObtenerHistorial_TransferenciaRecibida() {
        // =========================================================
    // 1. Arrange (Preparación de datos)
    // =========================================================

    Long cuentaId = cuentaPrincipal.getId();

    LocalDateTime fechaOperacion = LocalDateTime.now();

    Transaccion transferencia = new Transaccion();

    transferencia.setId(100L);
    transferencia.setTotal(500.0);
    transferencia.setFecha(fechaOperacion);
    transferencia.setTipo(ETipoTransaccion.TRANSFERENCIA);

    // La cuenta secundaria envía dinero
    transferencia.setCuentaOrigen(cuentaSecundaria);

    // La cuenta principal recibe dinero
    transferencia.setCuentaDestino(cuentaPrincipal);

    List<Transaccion> historialMock =
            Arrays.asList(transferencia);

    when(
            transaccionRepository
                    .findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(
                            cuentaId,
                            cuentaId
                    )
    ).thenReturn(historialMock);

    // =========================================================
    // 2. Act (Ejecución)
    // =========================================================

    List<TransaccionResponse> resultado =
            transaccionService.obtenerHistorial(cuentaId);

    // =========================================================
    // 3. Assert (Verificaciones generales)
    // =========================================================

    assertNotNull(
            resultado,
            "La lista de resultados no debería ser null"
    );

    boolean listaVacia = resultado.isEmpty();

    assertFalse(
            listaVacia,
            "La lista debería contener transacciones"
    );

    int totalResultados = resultado.size();

    assertEquals(
            1,
            totalResultados,
            "Solo debería existir una transacción"
    );

    // =========================================================
    // 4. Validación del contenido de la respuesta
    // =========================================================

    TransaccionResponse response = resultado.get(0);

    assertNotNull(
            response,
            "La respuesta transformada no debería ser null"
    );

    // =========================================================
    // 5. Verificación de campos básicos
    // =========================================================

    Long idEsperado = 100L;
    Long idReal = response.getId();

    assertEquals(
            idEsperado,
            idReal,
            "El ID de la transacción no coincide"
    );

    double totalEsperado = 500.0;
    double totalReal = response.getTotal();

    assertEquals(
            totalEsperado,
            totalReal,
            "El importe total no coincide"
    );

    String tipoEsperado =
            ETipoTransaccion.TRANSFERENCIA.name();

    String tipoReal = response.getTipo();

    assertEquals(
            tipoEsperado,
            tipoReal,
            "El tipo de transacción no coincide"
    );

    // =========================================================
    // 6. Verificación de cuentas origen/destino
    // =========================================================

    String cuentaOrigenEsperada =
            cuentaSecundaria.getNumeroCuenta();

    String cuentaOrigenReal =
            response.getCuentaOrigenNum();

    assertEquals(
            cuentaOrigenEsperada,
            cuentaOrigenReal,
            "El número de cuenta origen no coincide"
    );

    String cuentaDestinoEsperada =
            cuentaPrincipal.getNumeroCuenta();

    String cuentaDestinoReal =
            response.getCuentaDestinoNum();

    assertEquals(
            cuentaDestinoEsperada,
            cuentaDestinoReal,
            "El número de cuenta destino no coincide"
    );

    // =========================================================
    // 7. Verificación de la descripción inteligente
    // =========================================================

    String descripcionEsperada =
            "Transferencia RECIBIDA de "
                    + cuentaSecundaria.getNumeroCuenta();

    String descripcionReal =
            response.getDescripcion();

    assertNotNull(
            descripcionReal,
            "La descripción generada no debería ser null"
    );

    assertEquals(
            descripcionEsperada,
            descripcionReal,
            "La descripción inteligente generada no es correcta"
    );

    // =========================================================
    // 8. Validación de fecha
    // =========================================================

    String fechaEsperada = fechaOperacion.toString();
    String fechaReal = response.getFecha();

    assertEquals(
            fechaEsperada,
            fechaReal,
            "La fecha transformada no coincide"
    );

    // =========================================================
    // 9. Verificación de interacción con el repositorio
    // =========================================================

    verify(transaccionRepository, times(1))
            .findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(
                    cuentaId,
                    cuentaId
            );

    verifyNoMoreInteractions(transaccionRepository);
    }

    /**
     * Prueba 3: Verifica el comportamiento de la descripción inteligente 
     * cuando la cuenta principal es el ORIGEN de una transferencia.
     */
    @Test
    @DisplayName("Debe generar la descripción 'Transferencia ENVIADA' cuando la cuenta es origen")
    void testObtenerHistorial_TransferenciaEnviada() {
        // =========================================================
    // 1. Arrange (Preparación de datos)
    // =========================================================

    Long cuentaId = cuentaPrincipal.getId();

    LocalDateTime fechaTransferencia = LocalDateTime.now();

    Transaccion transferencia = new Transaccion();

    transferencia.setId(101L);
    transferencia.setTotal(300.0);
    transferencia.setFecha(fechaTransferencia);
    transferencia.setTipo(ETipoTransaccion.TRANSFERENCIA);

    // La cuenta principal realiza el envío
    transferencia.setCuentaOrigen(cuentaPrincipal);

    // La cuenta secundaria recibe el dinero
    transferencia.setCuentaDestino(cuentaSecundaria);

    List<Transaccion> historialMock =
            Arrays.asList(transferencia);

    when(
            transaccionRepository
                    .findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(
                            cuentaId,
                            cuentaId
                    )
    ).thenReturn(historialMock);

    // =========================================================
    // 2. Act (Ejecución del método)
    // =========================================================

    List<TransaccionResponse> resultado =
            transaccionService.obtenerHistorial(cuentaId);

    // =========================================================
    // 3. Assert (Comprobaciones generales)
    // =========================================================

    assertNotNull(
            resultado,
            "La lista devuelta no debería ser null"
    );

    boolean resultadoVacio = resultado.isEmpty();

    assertFalse(
            resultadoVacio,
            "La lista debería contener al menos una transacción"
    );

    int numeroResultados = resultado.size();

    assertEquals(
            1,
            numeroResultados,
            "Solo debería existir una transacción"
    );

    // =========================================================
    // 4. Obtención y validación de la respuesta
    // =========================================================

    TransaccionResponse response = resultado.get(0);

    assertNotNull(
            response,
            "La respuesta obtenida no debería ser null"
    );

    // =========================================================
    // 5. Validación de datos básicos
    // =========================================================

    Long idEsperado = 101L;
    Long idReal = response.getId();

    assertEquals(
            idEsperado,
            idReal,
            "El ID de la transacción no coincide"
    );

    double totalEsperado = 300.0;
    double totalReal = response.getTotal();

    assertEquals(
            totalEsperado,
            totalReal,
            "El total de la transferencia no coincide"
    );

    String tipoEsperado =
            ETipoTransaccion.TRANSFERENCIA.name();

    String tipoReal = response.getTipo();

    assertEquals(
            tipoEsperado,
            tipoReal,
            "El tipo de transacción devuelto no es correcto"
    );

    // =========================================================
    // 6. Validación de cuentas origen y destino
    // =========================================================

    String cuentaOrigenEsperada =
            cuentaPrincipal.getNumeroCuenta();

    String cuentaOrigenReal =
            response.getCuentaOrigenNum();

    assertEquals(
            cuentaOrigenEsperada,
            cuentaOrigenReal,
            "La cuenta origen no coincide"
    );

    String cuentaDestinoEsperada =
            cuentaSecundaria.getNumeroCuenta();

    String cuentaDestinoReal =
            response.getCuentaDestinoNum();

    assertEquals(
            cuentaDestinoEsperada,
            cuentaDestinoReal,
            "La cuenta destino no coincide"
    );

    // =========================================================
    // 7. Verificación de descripción inteligente
    // =========================================================

    String descripcionEsperada =
            "Transferencia ENVIADA a "
                    + cuentaSecundaria.getNumeroCuenta();

    String descripcionReal =
            response.getDescripcion();

    assertNotNull(
            descripcionReal,
            "La descripción generada no debería ser null"
    );

    assertEquals(
            descripcionEsperada,
            descripcionReal,
            "La descripción de transferencia enviada no es correcta"
    );

    // =========================================================
    // 8. Validación de fecha
    // =========================================================

    String fechaEsperada =
            fechaTransferencia.toString();

    String fechaReal =
            response.getFecha();

    assertEquals(
            fechaEsperada,
            fechaReal,
            "La fecha transformada no coincide"
    );

    // =========================================================
    // 9. Verificación de interacción con el repositorio
    // =========================================================

    verify(transaccionRepository, times(1))
            .findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(
                    cuentaId,
                    cuentaId
            );

    verifyNoMoreInteractions(transaccionRepository);
    }

    /**
     * Prueba 4: Verifica que las transacciones simples (Depósitos o Retiros)
     * conservan su descripción original introducida por el usuario.
     */
    @Test
    @DisplayName("Debe conservar la descripción original para DEPÓSITOS y RETIROS")
    void testObtenerHistorial_DepositoMantieneDescripcion() {
        // =========================================================
    // 1. Arrange (Preparación de datos)
    // =========================================================

    Long cuentaId = cuentaPrincipal.getId();

    LocalDateTime fechaDeposito = LocalDateTime.now();

    Transaccion deposito = new Transaccion();

    deposito.setId(102L);
    deposito.setTotal(1000.0);
    deposito.setFecha(fechaDeposito);

    deposito.setTipo(ETipoTransaccion.DEPOSITO);

    String descripcionOriginal = "Ingreso de nómina";

    deposito.setDescripcion(descripcionOriginal);

    // En un depósito la cuenta origen puede ser null
    deposito.setCuentaOrigen(null);

    deposito.setCuentaDestino(cuentaPrincipal);

    List<Transaccion> historialMock =
            Arrays.asList(deposito);

    when(
            transaccionRepository
                    .findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(
                            cuentaId,
                            cuentaId
                    )
    ).thenReturn(historialMock);

    // =========================================================
    // 2. Act (Ejecución del método)
    // =========================================================

    List<TransaccionResponse> resultado =
            transaccionService.obtenerHistorial(cuentaId);

    // =========================================================
    // 3. Assert (Comprobaciones generales)
    // =========================================================

    assertNotNull(
            resultado,
            "La lista devuelta no debería ser null"
    );

    boolean listaVacia = resultado.isEmpty();

    assertFalse(
            listaVacia,
            "La lista debería contener una transacción"
    );

    int numeroResultados = resultado.size();

    assertEquals(
            1,
            numeroResultados,
            "Solo debería existir una transacción"
    );

    // =========================================================
    // 4. Obtención de la respuesta transformada
    // =========================================================

    TransaccionResponse response = resultado.get(0);

    assertNotNull(
            response,
            "La respuesta transformada no debería ser null"
    );

    // =========================================================
    // 5. Validación de datos básicos
    // =========================================================

    Long idEsperado = 102L;
    Long idReal = response.getId();

    assertEquals(
            idEsperado,
            idReal,
            "El ID del depósito no coincide"
    );

    double totalEsperado = 1000.0;
    double totalReal = response.getTotal();

    assertEquals(
            totalEsperado,
            totalReal,
            "El importe del depósito no coincide"
    );

    String tipoEsperado =
            ETipoTransaccion.DEPOSITO.name();

    String tipoReal =
            response.getTipo();

    assertEquals(
            tipoEsperado,
            tipoReal,
            "El tipo de transacción debería ser DEPÓSITO"
    );

    // =========================================================
    // 6. Validación de cuentas origen y destino
    // =========================================================

    String cuentaOrigenEsperada = "N/A";

    String cuentaOrigenReal =
            response.getCuentaOrigenNum();

    assertEquals(
            cuentaOrigenEsperada,
            cuentaOrigenReal,
            "Cuando no existe cuenta origen debería mostrarse 'N/A'"
    );

    String cuentaDestinoEsperada =
            cuentaPrincipal.getNumeroCuenta();

    String cuentaDestinoReal =
            response.getCuentaDestinoNum();

    assertEquals(
            cuentaDestinoEsperada,
            cuentaDestinoReal,
            "La cuenta destino no coincide"
    );

    // =========================================================
    // 7. Verificación de descripción original
    // =========================================================

    String descripcionReal =
            response.getDescripcion();

    assertNotNull(
            descripcionReal,
            "La descripción no debería ser null"
    );

    assertEquals(
            descripcionOriginal,
            descripcionReal,
            "La descripción original del depósito debería mantenerse"
    );

    // =========================================================
    // 8. Validación de fecha
    // =========================================================

    String fechaEsperada =
            fechaDeposito.toString();

    String fechaReal =
            response.getFecha();

    assertEquals(
            fechaEsperada,
            fechaReal,
            "La fecha transformada no coincide"
    );

    // =========================================================
    // 9. Verificación de interacción con el repositorio
    // =========================================================

    verify(transaccionRepository, times(1))
            .findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(
                    cuentaId,
                    cuentaId
            );

    verifyNoMoreInteractions(transaccionRepository);
    }
}
