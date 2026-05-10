package es.deusto.banca_online.security;

/**
 * @file JwtAuthFilter.java
 * @brief Filtro HTTP que intercepta cada petición para validar y procesar el token JWT.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.repository.IUsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de seguridad que se ejecuta en cada petición entrante (OncePerRequestFilter).
 * Verifica la presencia de un token JWT en la cabecera 'Authorization', lo valida 
 * y establece el contexto de seguridad de Spring si el token es correcto.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Constructor para inyectar las utilidades JWT y el servicio de detalles de usuario.
     * @param jwtUtils Herramientas para validar y extraer datos del token.
     * @param userDetailsService Servicio para cargar el principal del usuario.
     */
    public JwtAuthFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Lógica interna del filtro para interceptar la petición, extraer el token Bearer 
     * y autenticar al usuario en el SecurityContextHolder.
     * @param request Objeto de petición HTTP.
     * @param response Objeto de respuesta HTTP.
     * @param filterChain Cadena de filtros de seguridad a seguir.
     * @throws ServletException Si ocurre un error en el procesamiento del servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay token, continuar sin autenticar (Spring Security decidirá si denegar)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (jwtUtils.esValido(token)) {
            String email = jwtUtils.extraerEmail(token);
            String rol = jwtUtils.extraerRol(token);

            // Solo autenticar si no hay ya un usuario en el contexto
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Usar el ROL del token JWT (fuente de verdad), no el de la BD
                List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + rol)
                );

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
