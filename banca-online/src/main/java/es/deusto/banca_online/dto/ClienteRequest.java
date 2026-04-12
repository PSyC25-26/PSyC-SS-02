package es.deusto.banca_online.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;


// Lo usaremos para cuando un cliente se crea. Es por ello que debemos
// valdar campos obligatorios (@NotNull o @NotBlank para los strngs) para
// que el usuario no envie datos incompletos.

@Data
public class ClienteRequest {
    /*---------------
        ATRIBUTOS
    ---------------*/
    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String primerApellido;
    private String segundoApellido;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private String telefono;
    private String direccion;



    /*--------------------
        CONSTRUCTORES
    --------------------*/
    public ClienteRequest(String dni, String nombre, String primerApellido, String segundoApellido, LocalDate fechaNacimiento, String telefono, String email, String direccion) {
        this.dni = dni;
        this.nombre = nombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    public ClienteRequest() {
    }
}
