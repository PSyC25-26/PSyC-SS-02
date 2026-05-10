package es.deusto.banca_online.services;

/**
 * @file CuentaService.java
 * @brief Servicio de lógica de negocio para cuentas bancarias: saldos, depósitos y retiros.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.dto.CuentaRequest;
import es.deusto.banca_online.dto.CuentaResponse;
import es.deusto.banca_online.entity.*;
import es.deusto.banca_online.repository.ICuentaRepository;
import es.deusto.banca_online.repository.IClienteRepository;
import es.deusto.banca_online.repository.ITransaccionRepository;
import es.deusto.banca_online.security.AuthChecks;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

/**
 * Servicio para la administración de Cuentas Bancarias.
 * Gestiona el ciclo de vida de las cuentas, el control de saldos y 
 * la validación de propiedad de los recursos financieros.
 */
@Service
public class CuentaService {

    private final ICuentaRepository cuentaRepository;
    private final IClienteRepository clienteRepository;
    private final ITransaccionRepository transaccionRepository;
    private final AuthChecks authChecks;

    /**
     * Constructor para la inyección de dependencias de CuentaService.
     * @param cuentaRepository Repositorio para la gestión de persistencia de cuentas.
     * @param clienteRepository Repositorio para validar la existencia de clientes.
     * @param transaccionRepository Repositorio para el registro de movimientos.
     * @param authChecks Componente de validaciones de seguridad personalizadas.
     */
    public CuentaService(ICuentaRepository cuentaRepository, IClienteRepository clienteRepository,
                         ITransaccionRepository transaccionRepository, AuthChecks authChecks) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.transaccionRepository = transaccionRepository;
        this.authChecks = authChecks;
    }

    /**
     * Crea una nueva cuenta bancaria para un cliente específico.
     * Genera automáticamente un número de cuenta único (IBAN ficticio) y 
     * establece el saldo inicial y tipo de cuenta indicados.
     * @param request DTO con el ID del cliente, saldo inicial y tipo de cuenta.
     * @return Respuesta con los datos de la cuenta creada.
     */
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

    /**
     * Obtiene el listado de cuentas pertenecientes a un cliente sin realizar chequeos de seguridad.
     * Útil para llamadas internas del sistema o procesos administrativos.
     * @param clienteId ID del cliente cuyas cuentas se desean listar.
     * @return Lista de DTOs CuentaResponse con la información de las cuentas.
     */
    public List<CuentaResponse> obtenerCuentasPorCliente(Long clienteId) {
        return cuentaRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el listado de cuentas de un cliente validando que el usuario autenticado
     * sea el propietario o tenga permisos de acceso.
     * @param clienteId ID del cliente a consultar.
     * @param authentication Datos del contexto de seguridad del usuario actual.
     * @return Lista de DTOs CuentaResponse filtrada.
     */
    public List<CuentaResponse> obtenerCuentasPorCliente(Long clienteId, Authentication authentication) {
        authChecks.assertOwnsCliente(authentication, clienteId);
        return obtenerCuentasPorCliente(clienteId);
    }

    /**
     * Genera un número de cuenta único siguiendo un formato ficticio (ES + UUID).
     * @return Una cadena de texto que representa el número de cuenta bancaria.
     */
    private String generarNumeroCuenta() {
        return "ES" + UUID.randomUUID().toString().replace("-", "").substring(0, 18).toUpperCase();
    }

    /**
     * Convierte una entidad de base de datos Cuenta a su representación DTO CuentaResponse.
     * Este mapeo asegura que solo se expongan los datos necesarios a la API.
     * @param cuenta La entidad persistida.
     * @return El objeto de respuesta (DTO) listo para ser enviado al cliente.
     */
    private CuentaResponse toResponse(Cuenta cuenta) {
        CuentaResponse response = new CuentaResponse();
        response.setId(cuenta.getId());
        response.setNumeroCuenta(cuenta.getNumeroCuenta());
        response.setSaldo(cuenta.getSaldo());
        response.setTipoCuenta(cuenta.getTipoCuenta().name());
        response.setClienteId(cuenta.getCliente().getId());
        response.setFechaCreacion(cuenta.getFechaCreacion());
        response.setActiva(cuenta.getActiva());
        return response;
    }

    // Verifica que el cliente autenticado es propietario de la cuenta
    /**
     * Helper privado que delega la validación de propiedad de una cuenta al componente AuthChecks.
     * @param cuenta Entidad de la cuenta a verificar.
     * @param authentication Usuario autenticado que intenta realizar la acción.
     */
    private void verificarPropietario(Cuenta cuenta, Authentication authentication) {
        authChecks.assertOwnsCuenta(authentication, cuenta);
    }

    /**
     * Recupera el saldo actual de una cuenta, validando previamente que 
     * el usuario autenticado sea el propietario o administrador.
     * @param cuentaId ID único de la cuenta.
     * @param authentication Datos del usuario que realiza la consulta.
     * @return El saldo disponible en formato Double.
     */
    public Double obtenerSaldo(Long cuentaId, Authentication authentication) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        verificarPropietario(cuenta, authentication);
        return cuenta.getSaldo();
    }

    /**
     * Actualiza el saldo de una cuenta de forma directa en la base de datos.
     * Este método se utiliza internamente durante procesos transaccionales como transferencias.
     * @param cuentaId ID de la cuenta a modificar.
     * @param nuevoSaldo El nuevo valor numérico del saldo.
     * @throws RuntimeException Si la cuenta no existe.
     */
    @Transactional
    public void actualizarSaldo(Long cuentaId, Double nuevoSaldo) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        cuenta.setSaldo(nuevoSaldo);
        cuentaRepository.save(cuenta);
    }

    /**
     * Procesa un depósito de efectivo en una cuenta.
     * Incrementa el saldo y registra el movimiento como una transacción de tipo DEPOSITO.
     * @param cuentaId ID de la cuenta receptora.
     * @param monto Cantidad a depositar (debe ser > 0).
     * @param authentication Usuario que autoriza la operación.
     * @return Datos de la cuenta tras el depósito.
     */
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

    /**
     * Procesa un retiro de efectivo de una cuenta.
     * Valida que el usuario sea dueño de la cuenta y que exista saldo suficiente.
     * Registra el movimiento como una transacción de tipo RETIRO.
     * @param cuentaId ID de la cuenta de origen.
     * @param monto Cantidad a retirar (debe ser > 0).
     * @param authentication Usuario que realiza el retiro.
     * @return Datos de la cuenta tras la extracción.
     * @throws IllegalArgumentException Si el saldo es insuficiente.
     */
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


    // Añade este método en CuentaService.java
    /**
     * Realiza un borrado lógico de una cuenta bancaria.
     * La cuenta no se elimina de la base de datos para mantener la trazabilidad,
     * pero se marca como inactiva. Solo se permite si el saldo es exactamente cero.
     * @param id ID de la cuenta a desactivar.
     * @throws IllegalArgumentException Si la cuenta tiene saldo pendiente.
     */
    @Transactional
    public void eliminarCuentaInactiva(Long id) {
        // 1. Buscamos la cuenta (incluso si está desactivada para dar un error coherente)
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        // 2. REQUISITO HU: Validar que el saldo sea 0
        if (cuenta.getSaldo() > 0) {
            throw new IllegalArgumentException("No se puede eliminar una cuenta con saldo positivo (" + cuenta.getSaldo() + "€)");
        }

        // 3. BORRADO LÓGICO: Mantenemos los datos pero la desactivamos para poder hacer trazabilidad
        cuenta.setActiva(false);
        cuentaRepository.save(cuenta);

        // Al no usar .delete(), las transacciones en la tabla 'transaccion'
    }
}