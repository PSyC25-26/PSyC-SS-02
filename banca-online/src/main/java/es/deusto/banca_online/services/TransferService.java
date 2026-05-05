package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.TransferenciaDTO;
import es.deusto.banca_online.entity.Cuenta;
import es.deusto.banca_online.entity.Transaccion;
import es.deusto.banca_online.entity.ETipoTransaccion;
import es.deusto.banca_online.repository.ICuentaRepository;
import es.deusto.banca_online.repository.ITransaccionRepository;
import es.deusto.banca_online.security.AuthChecks;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    private final CuentaService cuentaService;
    private final ICuentaRepository cuentaRepository;
    private final ITransaccionRepository transaccionRepository;
    private final AuthChecks authChecks;

    public TransferService(CuentaService cuentaService,
                           ICuentaRepository cuentaRepository,
                           ITransaccionRepository transaccionRepository,
                           AuthChecks authChecks) {
        this.cuentaService = cuentaService;
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
        this.authChecks = authChecks;
    }

    @Transactional
    public TransferenciaDTO transferirDinero(TransferenciaDTO transferenciaDTO, Authentication authentication) {

        Cuenta origen = cuentaRepository.findByNumeroCuenta(transferenciaDTO.getCuentaOrigen())
                .orElseThrow(() -> new RuntimeException("Cuenta de origen no encontrada"));
        Cuenta destino = cuentaRepository.findByNumeroCuenta(transferenciaDTO.getCuentaDestino())
                .orElseThrow(() -> new RuntimeException("Cuenta de destino no encontrada"));

        // Solo validamos que el ORIGEN sea del usuario ---
        Long clienteIdAutenticado = authChecks.clienteIdOrNull(authentication);
        if (!authChecks.isAdmin(authentication)) {
            if (clienteIdAutenticado == null || origen.getCliente() == null) {
                throw new AccessDeniedException("No se pudo verificar la propiedad de la cuenta");
            }

            long idPropietario = origen.getCliente().getId().longValue();

            if (clienteIdAutenticado.longValue() != idPropietario) {
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