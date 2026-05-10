package es.deusto.banca_online.dto;

/**
 * @file TransferenciaDTO.java
 * @brief DTO con los datos de origen, destino e importe para ejecutar una transferencia.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import lombok.Data;

@Data
public class TransferenciaDTO {
    private String cuentaOrigen;
    private String cuentaDestino;
    private double cantidad;
}
