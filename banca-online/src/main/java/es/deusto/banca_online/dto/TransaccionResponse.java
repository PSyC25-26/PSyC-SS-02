package es.deusto.banca_online.dto;

import lombok.Data;

@Data
public class TransaccionResponse {
    private Long id;
    private String tipo;
    private String descripcion;
    private Double total;
    private String cuentaOrigenNum;
    private String cuentaDestinoNum;
    private String fecha;
}