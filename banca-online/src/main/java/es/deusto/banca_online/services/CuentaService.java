package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.entity.Cuenta;
import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.ETipoCuenta;
import es.deusto.banca_online.repository.ICuentaRepository;
import es.deusto.banca_online.repository.IClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CuentaService {

    private final ICuentaRepository cuentaRepository;
    private final IClienteRepository clienteRepository;

    public CuentaService(ICuentaRepository cuentaRepository, IClienteRepository clienteRepository) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
    }

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
        return response;
    }
}