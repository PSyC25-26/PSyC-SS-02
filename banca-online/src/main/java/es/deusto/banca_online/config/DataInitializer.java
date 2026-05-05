package es.deusto.banca_online.config;

import es.deusto.banca_online.entity.ERol;
import es.deusto.banca_online.entity.Usuario;
import es.deusto.banca_online.repository.IUsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

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
            // admin.setCliente(null) por defecto
            usuarioRepository.save(admin);
            System.out.println("Usuario admin creado");
        }
    }
}
