package es.deusto.banca_online.dto;

/**
 * @file ClienteResponse.java
 * @brief DTO de salida con la información de un cliente para enviar a través de la API.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;


// Lo usaremos para cuando mandemos datos de un cliente ya creado
// por ejemplo, para consultar su informacion.
@Data
public class ClienteResponse {
    /*---------------
        ATRIBUTOS
    ---------------*/
    private Long id;
    private String dni;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private LocalDate fechaNacimiento;
    private String email;
    private String telefono;
    private String direccion;
    private LocalDateTime fechaCreacion;


    /*--------------------
        CONSTRUCTORES
    --------------------*/
    public ClienteResponse() {
    }

    public ClienteResponse(Long id, String dni, String nombre, String primerApellido, String segundoApellido, LocalDate fechaNacimiento, String email, String telefono, String direccion, LocalDateTime fechaCreacion) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.fechaNacimiento = fechaNacimiento;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fechaCreacion = fechaCreacion;
    }
}
