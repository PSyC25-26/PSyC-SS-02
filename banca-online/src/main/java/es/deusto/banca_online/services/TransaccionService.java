package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.TransaccionResponse;
import es.deusto.banca_online.entity.Transaccion;
import es.deusto.banca_online.repository.ITransaccionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import es.deusto.banca_online.entity.ETipoTransaccion;

@Service
public class TransaccionService {
    private final ITransaccionRepository transaccionRepository;

    public TransaccionService(ITransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    public List<TransaccionResponse> obtenerHistorial(Long cuentaId) {
        List<Transaccion> transacciones = transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(cuentaId, cuentaId);

        return transacciones.stream().map(t -> {
            TransaccionResponse res = new TransaccionResponse();
            res.setId(t.getId());
            res.setTotal(t.getTotal());
            res.setFecha(t.getFecha().toString());
            res.setTipo(t.getTipo().name());

            String numOrigen = t.getCuentaOrigen() != null ? t.getCuentaOrigen().getNumeroCuenta() : "N/A";
            String numDestino = t.getCuentaDestino() != null ? t.getCuentaDestino().getNumeroCuenta() : "N/A";

            res.setCuentaOrigenNum(numOrigen);
            res.setCuentaDestinoNum(numDestino);

            // Lógica de descripción inteligente
            if (t.getTipo() == ETipoTransaccion.TRANSFERENCIA) {
                if (t.getCuentaDestino() != null && t.getCuentaDestino().getId().equals(cuentaId)) {
                    res.setDescripcion("Transferencia RECIBIDA de " + numOrigen);
                } else {
                    res.setDescripcion("Transferencia ENVIADA a " + numDestino);
                }
            } else {
                res.setDescripcion(t.getDescripcion());
            }

            return res;
        }).collect(Collectors.toList());
    }
}