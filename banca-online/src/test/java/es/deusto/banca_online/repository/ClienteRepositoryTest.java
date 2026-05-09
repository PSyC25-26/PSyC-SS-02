package es.deusto.banca_online.repository;

import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.ERol;
import es.deusto.banca_online.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
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

    /** Logger con log4j2.xml */ 
    private static final Logger logger = LoggerFactory.getLogger(ClienteRepositoryTest.class);

    // Inyectamos el repostorio sin tener que crear uno
    /**
     * Repositorio de clientes inyectado para las pruebas de persistencia.
     */
    @Autowired
    private IClienteRepository clienteRepository;

    /**
     * Repositorio de usuarios inyectado para gestionar la autenticación asociada.
     */
    @Autowired
    private IUsuarioRepository usuarioRepository;

    /**
     * Repositorio de cuentas inyectado para las pruebas.
     * Se utiliza principalmente en el método de limpieza (setUp) para eliminar 
     * registros que dependen de los clientes, asegurando la integridad referencial.
     */
    @Autowired
    private ICuentaRepository cuentaRepository;

    /**
     * Repositorio de transacciones inyectado para las pruebas.
     * Al ser el nivel más bajo de la jerarquía de datos (depende de cuentas), 
     * es el primer repositorio que se vacía durante la limpieza de la base de datos.
     */
    @Autowired
    private ITransaccionRepository transaccionRepository;

    /**
     * Componente para el cifrado de contraseñas.
     * Se utiliza para generar hashes de contraseñas válidos al crear entidades 
     * de tipo Usuario en los tests que verifican la relación Cliente-Usuario.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Limpieza previa de la base de datos antes de cada test.
     * Garantiza la independencia de las pruebas eliminando registros en el orden
     * correcto para respetar las restricciones de integridad referencial (FK).
     */
    @BeforeEach
    void limpiarBD() {
        // Eliminar en orden: transacciones (FK) -> cuentas (FK) -> usuarios (FK) -> clientes
        transaccionRepository.deleteAll();
        cuentaRepository.deleteAll();
        usuarioRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    /*------------
        TESTS
     ------------*/
    /**
     * Test de persistencia: Guardado básico de un cliente.
     * Verifica que la entidad Cliente se mapee correctamente a las columnas de la tabla
     * y que la base de datos genere y retorne un identificador único (ID).
     */
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
    /**
     * Test de consulta masiva: Recuperación de todos los clientes registrados.
     * Valida que el método findAll() devuelva el número exacto de entidades
     * insertadas y que los datos críticos (como el email) coincidan con los de entrada.
     */
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
    /**
     * Test de consulta masiva (caso vacío): Comportamiento sin datos.
     * Asegura que el repositorio devuelva una lista vacía y no nula cuando no existen
     * registros, evitando posibles NullPointerException en la aplicación.
     */
    @Test
    void findAll_CuandoNoHayClientes_DeberiaRetornarListaVacia() {
        List<Cliente> clientes = clienteRepository.findAll();
        // Con @Transactional solo deberian existir clientes guardados en este test.
        // por lo que la lista deberia estar vacia
        assertThat(clientes).isEmpty();
    }


    /**
     * Test de consulta personalizada: Búsqueda por email a través de la relación Usuario-Cliente.
     * Verifica que el repositorio sea capaz de realizar el Join necesario para encontrar
     * un cliente basado en las credenciales de su usuario asociado.
     */
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

    /**
     * Test de consulta: Búsqueda por DNI.
     * Valida la recuperación de un cliente utilizando su documento nacional de identidad,
     * asegurando que la búsqueda sea exacta y sensible a los datos persistidos.
     */
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

    /**
     * Test de verificación: Existencia de email en el sistema.
     * Comprueba que la lógica de comprobación de existencia (existsByEmail) retorne
     * correctamente true para emails registrados y false para los que no lo están.
     */
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

    /**
     * Test de error (Búsqueda): Email inexistente.
     * Confirma que el método findByEmail retorne un Optional vacío cuando el registro
     * solicitado no existe, siguiendo el estándar de Java 8+.
     */
    @Test
    void testFindByEmail_NoExiste() {
        // EJECUTAR: Buscar un email que no existe
        Optional<Cliente> encontrado = clienteRepository.findByEmail("noexiste@test.com");

        // VERIFICAR: No debe encontrar nada
        assertThat(encontrado).isEmpty();

        logger.info("Prueba de búsqueda de email inexistente exitosa");
    }
}