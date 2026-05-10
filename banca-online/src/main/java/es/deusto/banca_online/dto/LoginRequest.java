package es.deusto.banca_online.dto;

/**
 * @file LoginRequest.java
 * @brief DTO con las credenciales de acceso (email y contraseña) para el proceso de login.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
