package es.deusto.banca_online.security;

/**
 * @file JwtUtils.java
 * @brief Utilidades para la generación, validación y extracción de claims de tokens JWT.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.entity.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Componente de utilidad para la gestión de JSON Web Tokens (JWT).
 * Se encarga de la generación, parseo y validación de tokens de seguridad
 * utilizados en la autenticación de la API.
 */
@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // Genera un token a partir del usuario autenticado
    /**
     * Genera un token JWT para un usuario autenticado.
     * Incluye claims personalizados como el rol y el clienteId.
     * @param usuario Entidad de usuario para la que se genera el token.
     * @return String con el token JWT compactado.
     */
    public String generarToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("rol", usuario.getRol().name())
                .claim("clienteId", usuario.getCliente() != null ? usuario.getCliente().getId() : null)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el identificador del sujeto (email) contenido en el cuerpo (claims) del token JWT.
     * @param token Cadena de caracteres que representa el JWT.
     * @return El email del usuario al que pertenece el token.
     */
    public String extraerEmail(String token) {
        return parsearClaims(token).getSubject();
    }

    /**
     * Recupera el rol asignado al usuario desde los claims personalizados del token.
     * @param token Cadena de caracteres que representa el JWT.
     * @return El nombre del rol (ej. "ADMIN" o "CLIENTE").
     */
    public String extraerRol(String token) {
        return parsearClaims(token).get("rol", String.class);
    }

    /**
     * Extrae el ID del cliente asociado al usuario desde los claims del token.
     * Este valor puede ser nulo si el usuario es un administrador sin perfil de cliente.
     * @param token Cadena de caracteres que representa el JWT.
     * @return El identificador numérico del cliente o null si no aplica.
     */
    public Long extraerClienteId(String token) {
        return parsearClaims(token).get("clienteId", Long.class);
    }

    /**
     * Valida si un token JWT es estructuralmente correcto y no ha expirado.
     * @param token El token JWT a validar.
     * @return true si es válido, false si ha sido manipulado o ha caducado.
     */
    public boolean esValido(String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Descifra y valida la firma de un token JWT para obtener su contenido (Claims).
     * Utiliza la clave de firma del sistema para asegurar que el token no ha sido alterado.
     * * @param token Cadena de caracteres que representa el JWT.
     * @return El objeto Claims con toda la información (payload) del token.
     * @throws io.jsonwebtoken.JwtException Si la firma es inválida o el token ha expirado.
     */
    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Genera la clave secreta necesaria para firmar y verificar los tokens JWT.
     * Decodifica la cadena secreta configurada en las propiedades del sistema (Base64)
     * y la transforma en una clave HMAC-SHA segura.
     * * @return SecretKey lista para ser usada por el motor de JJWT.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
