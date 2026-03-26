package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.TransferenciaDTO;
import es.deusto.banca_online.entity.Cuenta;
import es.deusto.banca_online.repository.ICuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private CuentaService cuentaService;

    @Mock
    private ICuentaRepository cuentaRepository;

    @InjectMocks
    private TransferService transferService;

    private Cuenta cuentaOrigen;
    private Cuenta cuentaDestino;
    private TransferenciaDTO transferenciaDTO;

    @BeforeEach
    void setUp() {
        cuentaOrigen = new Cuenta();
        cuentaOrigen.setId(1L);
        cuentaOrigen.setNumeroCuenta("ES111");

        cuentaDestino = new Cuenta();
        cuentaDestino.setId(2L);
        cuentaDestino.setNumeroCuenta("ES222");

        transferenciaDTO = new TransferenciaDTO();
        transferenciaDTO.setCuentaOrigen("ES111");
        transferenciaDTO.setCuentaDestino("ES222");
        transferenciaDTO.setCantidad(100.0);
    }

    @Test
    void transferirDinero_Exito() {
        when(cuentaRepository.findByNumeroCuenta("ES111")).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findByNumeroCuenta("ES222")).thenReturn(Optional.of(cuentaDestino));
        when(cuentaService.obtenerSaldo(1L)).thenReturn(500.0);
        when(cuentaService.obtenerSaldo(2L)).thenReturn(200.0);

        TransferenciaDTO resultado = transferService.transferirDinero(transferenciaDTO);

        assertNotNull(resultado);
        assertEquals(100.0, resultado.getCantidad());

        verify(cuentaService, times(1)).actualizarSaldo(1L, 400.0);
        verify(cuentaService, times(1)).actualizarSaldo(2L, 300.0);
        System.out.println("Transferir dinero_exito pasado");
    }

    @Test
    void transferirDinero_CuentaOrigenNoExiste() {
        when(cuentaRepository.findByNumeroCuenta("ES111")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            transferService.transferirDinero(transferenciaDTO)
        );

        assertEquals("Cuenta de origen no encontrada", exception.getMessage());
        verify(cuentaRepository, never()).findByNumeroCuenta("ES222");
        verify(cuentaService, never()).obtenerSaldo(anyLong());
        System.out.println("Test transferirDinero_CuentaOrigenNoExiste pasado");
    }

    @Test
    void transferirDinero_CuentaDestinoNoExiste() {
        // Arrange: origen exists
        when(cuentaRepository.findByNumeroCuenta("ES111")).thenReturn(Optional.of(cuentaOrigen));
        // Arrange: destino is empty
        when(cuentaRepository.findByNumeroCuenta("ES222")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            transferService.transferirDinero(transferenciaDTO)
        );

        assertEquals("Cuenta de destino no encontrada", exception.getMessage());
        verify(cuentaService, never()).obtenerSaldo(anyLong());
        System.out.println("Test transferirDinero_CuentaDestinoNoExiste pasado");

    }

    @Test
    void transferirDinero_SaldoInsuficiente() {
        when(cuentaRepository.findByNumeroCuenta("ES111")).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findByNumeroCuenta("ES222")).thenReturn(Optional.of(cuentaDestino));
        when(cuentaService.obtenerSaldo(1L)).thenReturn(50.0); // Menor que 100.0

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            transferService.transferirDinero(transferenciaDTO)
        );

        assertEquals("Saldo insuficiente en la cuenta de origen", exception.getMessage());
        verify(cuentaService, never()).actualizarSaldo(anyLong(), anyDouble());
        System.out.println("Test transferirDinero_SaldoInsuficiente pasado");
    }
}
