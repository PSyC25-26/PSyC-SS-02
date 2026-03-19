package es.deusto.banca_online.repository;

import es.deusto.banca_online.entity.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ClienteRepositoryTest {

    @Autowired
    private IClienteRepository clienteRepository;

    @Test
    void testGuardarCliente() {
        // 1. CREAR
        Cliente cliente = new Cliente();
        String uniqueDni = "DNI-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        cliente.setDni(uniqueDni);
        cliente.setNombre("Juan");
        cliente.setEmail("juan@test.com");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setFechaCreacion(LocalDateTime.now());

        // 2. GUARDAR
        Cliente guardado = clienteRepository.save(cliente);

        // 3. VERIFICAR
        assertThat(guardado).isNotNull();
        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getNombre()).isEqualTo("Juan");
        assertThat(guardado.getEmail()).isEqualTo("juan@test.com");

        System.out.println("Cliente guardado con ID: " + guardado.getId());
    }

    @Test
    void testFindByEmail() {
        // 1. CREAR
        Cliente cliente = new Cliente();
        String uniqueDni = "DNI-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        cliente.setDni(uniqueDni);
        cliente.setNombre("Juan");
        cliente.setEmail("juan@test.com");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setFechaCreacion(LocalDateTime.now());
        clienteRepository.save(cliente);

        // 2. EJECUTAR
        Optional<Cliente> encontrado = clienteRepository.findByEmail("juan@test.com");

        // 3. VERIFICAR
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("Juan");
        assertThat(encontrado.get().getEmail()).isEqualTo("juan@test.com");

        System.out.println("Cliente encontrado: " + encontrado.get().getNombre());
    }

    @Test
    void testFindByDni() {
        // 1. CREAR
        Cliente cliente = new Cliente();
        String uniqueDni = "DNI-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        cliente.setDni(uniqueDni);
        cliente.setNombre("Juan");
        cliente.setEmail("juan@test.com");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setFechaCreacion(LocalDateTime.now());
        clienteRepository.save(cliente);

        // 2. EJECUTAR
        Optional<Cliente> encontrado = clienteRepository.findByDni("12345678A");

        // 3. VERIFICAR
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getDni()).isEqualTo("12345678A");

        System.out.println("Cliente encontrado por DNI");
    }

    @Test
    void testExistsByEmail() {
        // 1. CREAR
        Cliente cliente = new Cliente();
        cliente.setDni("12345678A");
        cliente.setNombre("Juan");
        cliente.setEmail("juan@test.com");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setFechaCreacion(LocalDateTime.now());
        clienteRepository.save(cliente);

        // 2. EJECUTAR y VERIFICAR
        boolean existe = clienteRepository.existsByEmail("juan@test.com");
        boolean noExiste = clienteRepository.existsByEmail("noexiste@test.com");

        assertThat(existe).isTrue();
        assertThat(noExiste).isFalse();

        System.out.println("Email existe: " + existe);
        System.out.println("Email no existe: " + noExiste);
    }

    @Test
    void testFindByEmail_NoExiste() {
        // EJECUTAR
        Optional<Cliente> encontrado = clienteRepository.findByEmail("noexiste@test.com");

        // VERIFICAR
        assertThat(encontrado).isEmpty();

        System.out.println("Cliente no encontrado (correcto)");
    }
}