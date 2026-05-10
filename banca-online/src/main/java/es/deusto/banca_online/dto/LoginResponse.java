package es.deusto.banca_online.dto;

/**
 * @file LoginResponse.java
 * @brief DTO de respuesta al login con el token JWT, rol, clienteId y email del usuario.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String rol;
    private Long clienteId; // null si es ADMIN
    private String email;
}
