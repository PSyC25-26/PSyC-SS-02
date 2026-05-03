package es.deusto.banca_online.dto;

import lombok.Data;

@Data
public class ClienteUpdateDTO {
    // Creamos este DTO ya que solo queremos modificar
    // estos atributos cuando el usuario quiera actualizar
    private String telefono;
    private String direccion;
}