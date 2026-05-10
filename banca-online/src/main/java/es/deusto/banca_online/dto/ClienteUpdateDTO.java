package es.deusto.banca_online.dto;

/**
 * @file ClienteUpdateDTO.java
 * @brief DTO para la actualización parcial del perfil propio del cliente (teléfono y dirección).
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import lombok.Data;

@Data
public class ClienteUpdateDTO {
    // Creamos este DTO ya que solo queremos modificar
    // estos atributos cuando el usuario quiera actualizar
    private String telefono;
    private String direccion;
}