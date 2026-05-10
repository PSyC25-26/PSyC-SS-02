package es.deusto.banca_online.dto;

/**
 * @file SaldoResponse.java
 * @brief DTO de salida que encapsula el saldo actual de una cuenta bancaria.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import lombok.Data;

@Data
public class SaldoResponse {
    private Double saldo;

    public SaldoResponse(Double saldo) {
        this.saldo = saldo;
    }
}
