package es.deusto.banca_online.config;

/**
 * @file DataInitializer.java
 * @brief Crea el admin por defecto si no existe (fallback de seguridad).
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.entity.ERol;
import es.deusto.banca_online.entity.Usuario;
import es.deusto.banca_online.repository.IUsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 Componente de fallback que asegura la existencia del usuario admin al arrancar.
El password se codifica dinamicamente con BCrypt en lugar de hardcodearse,
  por lo que aunque produce un hash diferente al de data.sql, ambos representan
  la misma contrasena.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(IUsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByEmail("admin@banco.com")) {
            Usuario admin = new Usuario();
            admin.setEmail("admin@banco.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(ERol.ADMIN);
            admin.setActivo(true);
            usuarioRepository.save(admin);
            log.info("Usuario admin creado por DataInitializer (fallback)");
        } else {
            log.info("Usuario admin ya existe (probablemente creado por data.sql)");
        }
    }
}