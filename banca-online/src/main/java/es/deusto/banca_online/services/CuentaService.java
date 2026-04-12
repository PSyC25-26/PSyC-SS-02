package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.entity.*;
import es.deusto.banca_online.repository.ICuentaRepository;
import es.deusto.banca_online.repository.IClienteRepository;
import es.deusto.banca_online.repository.ITransaccionRepository;
import es.deusto.banca_online.entity.Usuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

@Service
public class CuentaService {

    private final ICuentaRepository cuentaRepository;
    private final IClienteRepository clienteRepository;
    private final ITransaccionRepository transaccionRepository;

    public CuentaService(ICuentaRepository cuentaRepository, IClienteRepository clienteRepository, ITransaccionRepository transaccionRepository) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.transaccionRepository = transaccionRepository;
    }

    @Transactional
    public CuentaResponse crearCuenta(CuentaRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + request.getClienteId()));

        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(generarNumeroCuenta());
        cuenta.setSaldo(request.getSaldoInicial() != null ? request.getSaldoInicial() : 0.0);
        cuenta.setTipoCuenta(ETipoCuenta.valueOf(request.getTipoCuenta()));
        cuenta.setCliente(cliente);

        Cuenta guardada = cuentaRepository.save(cuenta);
        return toResponse(guardada);
    }

    public List<CuentaResponse> obtenerCuentasPorCliente(Long clienteId) {
        return cuentaRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CuentaResponse> obtenerCuentasPorCliente(Long clienteId, Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!esAdmin) {
            Usuario principal = (Usuario) authentication.getPrincipal();
            if (principal.getClienteId() == null || !principal.getClienteId().equals(clienteId)) {
                throw new AccessDeniedException("No tiene permiso para ver estas cuentas");
            }
        }
        return obtenerCuentasPorCliente(clienteId);
    }

    private String generarNumeroCuenta() {
        return "ES" + UUID.randomUUID().toString().replace("-", "").substring(0, 18).toUpperCase();
    }

    private CuentaResponse toResponse(Cuenta cuenta) {
        CuentaResponse response = new CuentaResponse();
        response.setId(cuenta.getId());
        response.setNumeroCuenta(cuenta.getNumeroCuenta());
        response.setSaldo(cuenta.getSaldo());
        response.setTipoCuenta(cuenta.getTipoCuenta().name());
        response.setClienteId(cuenta.getCliente().getId());
        response.setFechaCreacion(cuenta.getFechaCreacion());
        return response;
    }

    // Verifica que el cliente autenticado es propietario de la cuenta
    private void verificarPropietario(Cuenta cuenta, Authentication authentication) {
        Usuario principal = (Usuario) authentication.getPrincipal();
        // ADMIN puede acceder a cualquier cuenta
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return;
        }
        // CLIENTE solo puede acceder a sus propias cuentas
        if (principal.getClienteId() == null ||
                !principal.getClienteId().equals(cuenta.getCliente().getId())) {
            throw new AccessDeniedException("No tiene permiso sobre esta cuenta");
        }
    }

    public Double obtenerSaldo(Long cuentaId, Authentication authentication) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        verificarPropietario(cuenta, authentication);
        return cuenta.getSaldo();
    }

    @Transactional
    public void actualizarSaldo(Long cuentaId, Double nuevoSaldo) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        cuenta.setSaldo(nuevoSaldo);
        cuentaRepository.save(cuenta);
    }

    @Transactional
    public CuentaResponse depositarDinero(Long cuentaId, Double monto, Authentication authentication) {
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a depositar debe ser mayor a cero");
        }

        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        verificarPropietario(cuenta, authentication);

        // Actualizamos el saldo
        cuenta.setSaldo(cuenta.getSaldo() + monto);
        Cuenta cuentaActualizada = cuentaRepository.save(cuenta);

        Transaccion transaccion = new Transaccion();
        transaccion.setTipo(ETipoTransaccion.DEPOSITO);
        transaccion.setDescripcion("Depósito en cuenta " + cuenta.getNumeroCuenta());
        transaccion.setTotal(monto);

        // Al ser un depósito, el dinero entra a esta cuenta, por lo que es la "cuentaDestino".
        // La cuentaOrigen queda como null.
        transaccion.setCuentaDestino(cuenta);

        // Guardamos el historial
        transaccionRepository.save(transaccion);
        // ----------------------------------------------

        return toResponse(cuentaActualizada);
    }

    @Transactional
    public CuentaResponse retirarDinero(Long cuentaId, Double monto, Authentication authentication) {
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser mayor a cero");
        }

        // 2. Buscamos la cuenta en la base de datos
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        verificarPropietario(cuenta, authentication);

        // 3. Verificamos que haya saldo suficiente para el retiro
        if (cuenta.getSaldo() < monto) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar el retiro");
        }

        // 4. Restamos el monto al saldo actual
        cuenta.setSaldo(cuenta.getSaldo() - monto);

        // 5. Guardamos la cuenta con el nuevo saldo
        Cuenta cuentaActualizada = cuentaRepository.save(cuenta);

        // --- REGISTRO DE TRANSACCIÓN ---
        Transaccion transaccion = new Transaccion();
        transaccion.setTipo(ETipoTransaccion.RETIRO); // Usamos el tipo RETIRO
        transaccion.setDescripcion("Retiro en cuenta " + cuenta.getNumeroCuenta());
        transaccion.setTotal(monto);

        // La cuenta de Destino queda como null (no la establecemos).
        transaccion.setCuentaOrigen(cuenta);

        // Guardamos el historial
        transaccionRepository.save(transaccion);
        // ----------------------------------------------

        // 6. Retornamos los datos actualizados
        return toResponse(cuentaActualizada);
    }
}