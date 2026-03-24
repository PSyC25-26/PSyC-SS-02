package es.deusto.banca_online.services;

import org.springframework.stereotype.Service;

import es.deusto.banca_online.entity.Cuenta;
import es.deusto.banca_online.repository.ICuentaRepository;
import jakarta.transaction.Transactional;
import es.deusto.banca_online.dto.TransferenciaDTO;


@Service
public class TransferService {

    private final CuentaService cuentaService;
    private final ICuentaRepository cuentaRepository;

    public TransferService(CuentaService cuentaService, ICuentaRepository cuentaRepository) {
        this.cuentaService = cuentaService;
        this.cuentaRepository = cuentaRepository;
    }

    //Metodo transferir dinero entre cuentas

    @Transactional
   public TransferenciaDTO transferirDinero(TransferenciaDTO transferenciaDTO) {

        // Verificar que tanto la cuenta de origen como la de destino existen
        Cuenta origen = cuentaRepository.findByNumeroCuenta(transferenciaDTO.getCuentaOrigen())
                .orElseThrow(() -> new RuntimeException("Cuenta de origen no encontrada"));
        Cuenta destino = cuentaRepository.findByNumeroCuenta(transferenciaDTO.getCuentaDestino())
                .orElseThrow(() -> new RuntimeException("Cuenta de destino no encontrada"));

        // Verificar que la cuenta de origen tiene suficiente saldo
        double saldoOrigen = cuentaService.obtenerSaldo(origen.getId());
        if (saldoOrigen < transferenciaDTO.getCantidad()) {
            throw new RuntimeException("Saldo insuficiente en la cuenta de origen");
        }

        // Realizar la transferencia
        double nuevoSaldoOrigen = saldoOrigen - transferenciaDTO.getCantidad();
        cuentaService.actualizarSaldo(origen.getId(), nuevoSaldoOrigen);

        double saldoDestino = cuentaService.obtenerSaldo(destino.getId());
        double nuevoSaldoDestino = saldoDestino + transferenciaDTO.getCantidad();
        cuentaService.actualizarSaldo(destino.getId(), nuevoSaldoDestino);
        return transferenciaDTO;
   }

}
