package es.deusto.banca_online.dto;

/**
 * @file TransaccionResponse.java
 * @brief DTO de salida con los datos de un movimiento bancario para el historial.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

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