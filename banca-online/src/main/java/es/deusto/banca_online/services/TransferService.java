package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.TransferenciaDTO;
import es.deusto.banca_online.entity.Cuenta;
import es.deusto.banca_online.entity.Transaccion;
import es.deusto.banca_online.entity.ETipoTransaccion;
import es.deusto.banca_online.repository.ICuentaRepository;
import es.deusto.banca_online.repository.ITransaccionRepository;
import es.deusto.banca_online.entity.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    private final CuentaService cuentaService;
    private final ICuentaRepository cuentaRepository;
    private final ITransaccionRepository transaccionRepository;

    public TransferService(CuentaService cuentaService,
                           ICuentaRepository cuentaRepository,
                           ITransaccionRepository transaccionRepository) {
        this.cuentaService = cuentaService;
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
    }

    @Transactional
    public TransferenciaDTO transferirDinero(TransferenciaDTO transferenciaDTO, Authentication authentication) {

        Cuenta origen = cuentaRepository.findByNumeroCuenta(transferenciaDTO.getCuentaOrigen())
                .orElseThrow(() -> new RuntimeException("Cuenta de origen no encontrada"));
        Cuenta destino = cuentaRepository.findByNumeroCuenta(transferenciaDTO.getCuentaDestino())
                .orElseThrow(() -> new RuntimeException("Cuenta de destino no encontrada"));

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!esAdmin) {
            Usuario principal = (Usuario) authentication.getPrincipal();

            // 1. Verificamos nulos
            if (principal.getClienteId() == null || origen.getCliente() == null) {
                throw new AccessDeniedException("No se pudo verificar la propiedad de la cuenta");
            }

            // 2. COMPARACIÓN SEGURA: Convertimos ambos a long primitivo
            long idUsuario = principal.getClienteId().longValue();
            long idPropietario = origen.getCliente().getId().longValue();

            if (idUsuario != idPropietario) {
                throw new AccessDeniedException("No tiene permiso. Usuario: " + idUsuario + ", Propietario: " + idPropietario);
            }
        }

        double saldoOrigen = cuentaService.obtenerSaldo(origen.getId(), authentication);
        if (saldoOrigen < transferenciaDTO.getCantidad()) {
            throw new RuntimeException("Saldo insuficiente en la cuenta de origen");
        }

        double nuevoSaldoOrigen = saldoOrigen - transferenciaDTO.getCantidad();
        cuentaService.actualizarSaldo(origen.getId(), nuevoSaldoOrigen);

        double saldoDestino = cuentaService.obtenerSaldo(destino.getId(), authentication);
        double nuevoSaldoDestino = saldoDestino + transferenciaDTO.getCantidad();
        cuentaService.actualizarSaldo(destino.getId(), nuevoSaldoDestino);

        // Guardamos transacciones para HU4.2: Historial de transacciones
        Transaccion t = new Transaccion();
        t.setTipo(ETipoTransaccion.TRANSFERENCIA);
        t.setDescripcion("Transferencia enviada a " + destino.getNumeroCuenta());
        t.setTotal(transferenciaDTO.getCantidad());
        t.setCuentaOrigen(origen);
        t.setCuentaDestino(destino);

        transaccionRepository.save(t);

        return transferenciaDTO;
    }
}