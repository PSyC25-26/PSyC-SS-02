package es.deusto.banca_online;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    @Test
    void generarHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("==========================================");
        System.out.println("admin123    -> " + encoder.encode("admin123"));
        System.out.println("cliente123  -> " + encoder.encode("cliente123"));
        System.out.println("==========================================");
    }
}