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

        // Solo validamos que el ORIGEN sea del usuario ---
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!esAdmin) {
            Usuario principal = (Usuario) authentication.getPrincipal();
            if (principal.getClienteId() == null || origen.getCliente() == null) {
                throw new AccessDeniedException("No se pudo verificar la propiedad de la cuenta");
            }

            long idUsuario = principal.getClienteId().longValue();
            long idPropietario = origen.getCliente().getId().longValue();

            if (idUsuario != idPropietario) {
                throw new AccessDeniedException("No tiene permiso sobre la cuenta de origen");
            }
        }

        // --- OPERACIÓN CUENTA ORIGEN (Usando el servicio con seguridad) ---
        double saldoOrigen = cuentaService.obtenerSaldo(origen.getId(), authentication);
        if (saldoOrigen < transferenciaDTO.getCantidad()) {
            throw new RuntimeException("Saldo insuficiente en la cuenta de origen");
        }
        cuentaService.actualizarSaldo(origen.getId(), saldoOrigen - transferenciaDTO.getCantidad());

        // --- OPERACIÓN CUENTA DESTINO (Usando Repositorio para evitar el 403) ---
        // No usamos cuentaService.obtenerSaldo porque daría 403 al no ser nuestra cuenta
        double nuevoSaldoDestino = destino.getSaldo() + transferenciaDTO.getCantidad();
        destino.setSaldo(nuevoSaldoDestino);
        cuentaRepository.save(destino); // Guardamos directamente en BD

        // --- 4. REGISTRO EN EL HISTORIAL (HU 4.2) ---
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