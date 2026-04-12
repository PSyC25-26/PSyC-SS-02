package es.deusto.banca_online.dto;

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
