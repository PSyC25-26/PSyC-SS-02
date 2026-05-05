package es.deusto.banca_online.repository;

import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.ERol;
import es.deusto.banca_online.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ClienteRepositoryTest {

    // Logger con log4j2.xml
    private static final Logger logger = LoggerFactory.getLogger(ClienteRepositoryTest.class);

    // Inyectamos el repostorio sin tener que crear uno
    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /*------------
        TESTS
     ------------*/
    @Test
    void testGuardarCliente() {
        // 1. CREAR: Preparamos un cliente de prueba
        Cliente cliente = new Cliente();
        cliente.setDni("12345678A");
        cliente.setNombre("Juan");
        cliente.setEmail("juan@test.com");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setFechaCreacion(LocalDateTime.now());

        // 2. GUARDAR: Ejecutamos save para probar que se guarda correctamente
        Cliente guardado = clienteRepository.save(cliente);

        // 3. VERIFICAR: Comprobamos que funciona
        assertThat(guardado).isNotNull();
        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getNombre()).isEqualTo("Juan");
        assertThat(guardado.getEmail()).isEqualTo("juan@test.com");

        logger.info("Cliente guardado con ID: {}", guardado.getId());
    }

    // Devolver todos los clientes si existen - VÁLIDO
    @Test
    void findAll_DeberiaRetornarTodosLosClientes() {
        // Crear y guardar varios clientes
        Cliente cliente1 = new Cliente();
        cliente1.setDni("11111111A");
        cliente1.setNombre("Juan");
        cliente1.setEmail("juan@test.com");
        cliente1.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente1.setFechaCreacion(LocalDateTime.now());
        clienteRepository.save(cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setDni("22222222B");
        cliente2.setNombre("María");
        cliente2.setEmail("maria@test.com");
        cliente2.setFechaNacimiento(LocalDate.of(1991, 2, 2));
        cliente2.setFechaCreacion(LocalDateTime.now());
        clienteRepository.save(cliente2);

        Cliente cliente3 = new Cliente();
        cliente3.setDni("33333333C");
        cliente3.setNombre("Pedro");
        cliente3.setEmail("pedro@test.com");
        cliente3.setFechaNacimiento(LocalDate.of(1992, 3, 3));
        cliente3.setFechaCreacion(LocalDateTime.now());
        clienteRepository.save(cliente3);

        // Ejecutar findAll
        List<Cliente> clientes = clienteRepository.findAll();

        // Verificar resultados
        assertThat(clientes).hasSize(3); // Clientes deberia ser una lista de 3
        // Y contener los siguientes emails:
        assertThat(clientes).extracting(Cliente::getEmail)
                .containsExactlyInAnyOrder("juan@test.com", "maria@test.com", "pedro@test.com");
    }


    // Devolver todos los clientes si existen - IINVÁLIDO
    @Test
    void findAll_CuandoNoHayClientes_DeberiaRetornarListaVacia() {
        List<Cliente> clientes = clienteRepository.findAll();
        // Con @Transactional solo deberian existir clientes guardados en este test.
        // por lo que la lista deberia estar vacia
        assertThat(clientes).isEmpty();
    }



    @Test
    void testFindByEmail() {
        // 1. CREAR: Preparamos datos
        Cliente cliente = new Cliente();
        cliente.setDni("12345678A");
        cliente.setNombre("Juan");
        cliente.setEmail("juan@test.com");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setFechaCreacion(LocalDateTime.now());
        Cliente clienteGuardado = clienteRepository.save(cliente);

        // Crear usuario asociado (fuente de verdad del email)
        Usuario usuario = new Usuario();
        usuario.setEmail("juan@test.com");
        usuario.setPassword(passwordEncoder.encode("password"));
        usuario.setRol(ERol.CLIENTE);
        usuario.setActivo(true);
        usuario.setCliente(clienteGuardado);
        usuarioRepository.save(usuario);

        // 2. EJECUTAR: Buscamos por email
        Optional<Cliente> encontrado = clienteRepository.findByEmail("juan@test.com");

        // 3. VERIFICAR
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("Juan");

        logger.info("Cliente encontrado por email: {}", encontrado.get().getNombre());    }

    @Test
    void testFindByDni() {
        // 1. CREAR
        Cliente cliente = new Cliente();
        cliente.setDni("12345678A");
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

        logger.info("Cliente encontrado por DNI correctamente");
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
        Cliente clienteGuardado = clienteRepository.save(cliente);

        // Crear usuario asociado (fuente de verdad del email)
        Usuario usuario = new Usuario();
        usuario.setEmail("juan@test.com");
        usuario.setPassword(passwordEncoder.encode("password"));
        usuario.setRol(ERol.CLIENTE);
        usuario.setActivo(true);
        usuario.setCliente(clienteGuardado);
        usuarioRepository.save(usuario);

        // 2. EJECUTAR y VERIFICAR
        boolean existe = clienteRepository.existsByEmail("juan@test.com");
        boolean noExiste = clienteRepository.existsByEmail("noexiste@test.com");

        // 3. VERIFICAR
        assertThat(existe).isTrue();
        assertThat(noExiste).isFalse();

        logger.debug("Comprobación de existencia de email finalizada (Existe: {}, No existe: {})", existe, noExiste);
    }

    @Test
    void testFindByEmail_NoExiste() {
        // EJECUTAR: Buscar un email que no existe
        Optional<Cliente> encontrado = clienteRepository.findByEmail("noexiste@test.com");

        // VERIFICAR: No debe encontrar nada
        assertThat(encontrado).isEmpty();

        logger.info("Prueba de búsqueda de email inexistente exitosa");
    }
}