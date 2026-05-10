package es.deusto.banca_online.controllers;

/**
 * @file AuthController.java
 * @brief Controlador REST para la autenticación de usuarios mediante JWT.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.dto.LoginRequest;
import es.deusto.banca_online.dto.LoginResponse;
import es.deusto.banca_online.entity.Usuario;
import es.deusto.banca_online.repository.IUsuarioRepository;
import es.deusto.banca_online.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
/**
 * Controlador encargado de la autenticación de usuarios.
 * Gestiona el acceso al sistema mediante la validación de credenciales y generación de tokens JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final IUsuarioRepository usuarioRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          IUsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Autentica a un usuario y devuelve un token de acceso.
     * @param request DTO con el email y la contraseña del usuario.
     * @return ResponseEntity con el token JWT, rol y datos básicos si el login es correcto.
     * @throws BadCredentialsException si las credenciales son inválidas.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        try {
            // Spring Security verifica email + password contra la BD
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword())
            );

            // Recuperar el usuario para obtener rol y clienteId
            Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow();

            // Generar el token con rol y clienteId como claims
            String token = jwtUtils.generarToken(usuario);

            Long clienteId = usuario.getCliente() != null ? usuario.getCliente().getId() : null;
            return ResponseEntity.ok(new LoginResponse(
                    token,
                    usuario.getRol().name(),
                    clienteId,
                    usuario.getEmail()
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
