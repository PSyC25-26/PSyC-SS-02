package es.deusto.banca_online.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración principal de seguridad de la aplicación.
 * Define la cadena de filtros de seguridad (SecurityFilterChain), la política de sesiones 
 * sin estado (stateless) para JWT y las reglas de acceso a los diferentes endpoints.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Constructor para la inyección de dependencias de los componentes de seguridad.
     * @param jwtAuthFilter Filtro personalizado para la validación de tokens JWT.
     * @param userDetailsService Servicio para la carga de detalles de usuario.
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configura la seguridad de las peticiones HTTP.
     * - Desactiva CSRF (no necesario para APIs con tokens).
     * - Establece política sin estado (STATELESS).
     * - Define rutas públicas (Login, Swagger, Frontend estático).
     * - Protege el resto de rutas requiriendo autenticación.
     * - Registra el filtro JWT antes del filtro de autenticación estándar.
     * @param http Objeto para configurar la seguridad web.
     * @return La cadena de filtros configurada.
     * @throws Exception Si ocurre un error en la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoint de login: público
                .requestMatchers("/api/auth/**").permitAll()
                // Recursos estáticos: públicos
                .requestMatchers(
                    "/", "/index.html",
                    "/css/**", "/js/**", "/modales/**"
                ).permitAll()
                // Swagger: público
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Expone el gestor de autenticación oficial de Spring Security.
     * @param config Configuración de autenticación de Spring.
     * @return El AuthenticationManager configurado.
     * @throws Exception Si no se puede recuperar el manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Define el algoritmo de cifrado para las contraseñas.
     * Utiliza BCrypt, que implementa un hash seguro con sal aleatoria.
     * @return Instancia de BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación de acceso a datos (DAO).
     * Conecta el servicio de detalles de usuario con el codificador de contraseñas.
     * @return El proveedor de autenticación configurado.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
