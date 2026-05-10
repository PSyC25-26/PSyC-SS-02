package es.deusto.banca_online.dto;

/**
 * @file CuentaResponse.java
 * @brief DTO de salida con el estado actual de una cuenta bancaria.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class CuentaResponse {

    private Long id;
    private String numeroCuenta;
    private Double saldo;
    private String tipoCuenta;
    private Long clienteId;

    @JsonProperty("activa")
    private boolean activa;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime fechaCreacion;
}