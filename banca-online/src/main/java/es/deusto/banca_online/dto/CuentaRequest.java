package es.deusto.banca_online.dto;

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