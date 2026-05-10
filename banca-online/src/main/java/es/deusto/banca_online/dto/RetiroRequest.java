package es.deusto.banca_online.dto;

/**
 * @file RetiroRequest.java
 * @brief DTO con los datos necesarios para realizar un retiro de una cuenta.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class RetiroRequest {
    @NotNull(message = "El ID de la cuenta es obligatorio")
    private Long cuentaId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto a retirar debe ser mayor a cero")
    private Double monto;

    // Getters y Setters
    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
}
