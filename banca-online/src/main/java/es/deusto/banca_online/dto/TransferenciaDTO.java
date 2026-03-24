package es.deusto.banca_online.dto;

import lombok.Data;

@Data
public class TransferenciaDTO {
    private String cuentaOrigen;
    private String cuentaDestino;
    private double cantidad;
}
