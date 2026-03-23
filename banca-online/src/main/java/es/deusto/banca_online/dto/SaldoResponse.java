package es.deusto.banca_online.dto;

import lombok.Data;

@Data
public class SaldoResponse {
    private Double saldo;

    public SaldoResponse(Double saldo) {
        this.saldo = saldo;
    }
}
