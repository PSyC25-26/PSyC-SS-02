package es.deusto.banca_online.dto;

import lombok.Data;

@Data
public class CuentaResponse {

    private Long id;
    private String numeroCuenta;
    private Double saldo;
    private String tipoCuenta;
    private Long clienteId;
}