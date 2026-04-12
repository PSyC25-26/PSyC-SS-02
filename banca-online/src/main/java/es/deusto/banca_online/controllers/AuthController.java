package es.deusto.banca_online.controllers;

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

            return ResponseEntity.ok(new LoginResponse(
                    token,
                    usuario.getRol().name(),
                    usuario.getClienteId(),
                    usuario.getEmail()
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
