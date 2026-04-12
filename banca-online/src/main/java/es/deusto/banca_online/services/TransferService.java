package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.TransferenciaDTO;
import es.deusto.banca_online.entity.Cuenta;
import es.deusto.banca_online.repository.ICuentaRepository;
import es.deusto.banca_online.entity.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


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
    public TransferenciaDTO transferirDinero(TransferenciaDTO transferenciaDTO, Authentication authentication) {

        // Verificar que tanto la cuenta de origen como la de destino existen
        Cuenta origen = cuentaRepository.findByNumeroCuenta(transferenciaDTO.getCuentaOrigen())
                .orElseThrow(() -> new RuntimeException("Cuenta de origen no encontrada"));
        Cuenta destino = cuentaRepository.findByNumeroCuenta(transferenciaDTO.getCuentaDestino())
                .orElseThrow(() -> new RuntimeException("Cuenta de destino no encontrada"));

        // CLIENTE solo puede transferir desde sus propias cuentas (destino puede ser de cualquiera)
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!esAdmin) {
            Usuario principal = (Usuario) authentication.getPrincipal();
            if (principal.getClienteId() == null ||
                    !principal.getClienteId().equals(origen.getCliente().getId())) {
                throw new AccessDeniedException("No tiene permiso sobre la cuenta de origen");
            }
        }

        // Verificar que la cuenta de origen tiene suficiente saldo
        double saldoOrigen = cuentaService.obtenerSaldo(origen.getId(), authentication);
        if (saldoOrigen < transferenciaDTO.getCantidad()) {
            throw new RuntimeException("Saldo insuficiente en la cuenta de origen");
        }

        // Realizar la transferencia
        double nuevoSaldoOrigen = saldoOrigen - transferenciaDTO.getCantidad();
        cuentaService.actualizarSaldo(origen.getId(), nuevoSaldoOrigen);

        double saldoDestino = cuentaService.obtenerSaldo(destino.getId(), authentication);
        double nuevoSaldoDestino = saldoDestino + transferenciaDTO.getCantidad();
        cuentaService.actualizarSaldo(destino.getId(), nuevoSaldoDestino);
        return transferenciaDTO;
   }

}
