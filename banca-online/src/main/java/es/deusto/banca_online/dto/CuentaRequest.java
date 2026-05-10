package es.deusto.banca_online.dto;

/**
 * @file CuentaRequest.java
 * @brief DTO con los datos de entrada para crear una nueva cuenta bancaria.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CuentaRequest {
    @NotNull(message = "El clienteId es obligatorio")
    private Long clienteId;
    @NotBlank(message = "El tipo de cuenta es obligatorio")
    private String tipoCuenta;
    @Min(value = 0, message = "El saldo inicial no puede ser negativo")
    private Double saldoInicial;
}