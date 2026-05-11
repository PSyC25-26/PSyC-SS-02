package es.deusto.banca_online.services;

/**
 * @file ClienteServiceTest.java
 * @brief Tests unitarios del servicio de lógica de negocio de clientes.
 * @details Verifica la creación, actualización, eliminación y búsqueda de clientes
 *          usando mocks de los repositorios para aislar la lógica del servicio.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import es.deusto.banca_online.dto.ClienteRequest;
import es.deusto.banca_online.dto.ClienteUpdateDTO;
import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.Usuario;
import es.deusto.banca_online.repository.IClienteRepository;
import es.deusto.banca_online.repository.IUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;



@ExtendWith(MockitoExtension.class)  // Habilita Mockito
class ClienteServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ClienteServiceTest.class);

    /**
     * Repositorio de clientes (Mock).
     * Simula el acceso a datos para aislar la lógica de negocio del servicio.
     */
    @Mock  // Crea un repositorio falso que simula el comportamiento de la base de datos
    private IClienteRepository clienteRepository;

    /**
     * Repositorio de usuarios (Mock).
     * Se utiliza para simular la gestión de credenciales vinculadas al cliente.
     */
    @Mock
    private IUsuarioRepository usuarioRepository;

    /**
     * Codificador de contraseñas (Mock).
     * Simula el cifrado de claves durante la creación o actualización de usuarios.
     */
    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * Servicio de clientes bajo prueba.
     * Los mocks definidos anteriormente se inyectan automáticamente en esta instancia.
     */
    @InjectMocks  // Inyecta los mocks dentro de ClienteService
    private ClienteService clienteService;

    private ClienteRequest requestValido;
    private Cliente clienteGuardado;
    private List<Cliente> clientesMock;




    // Preparacion antes de cada test. Así no repetimos código en cada test.
    /**
     * Configuración inicial antes de cada método de prueba.
     * Prepara objetos de petición (Request) y entidades pre-configuradas para 
     * estandarizar el entorno de cada test unitario.
     */
    @BeforeEach  // Se ejecuta antes de cada test
    void setUp() {
        // Preparo un request válido
        requestValido = new ClienteRequest();
        requestValido.setDni("12345678A");
        requestValido.setNombre("Juan");
        requestValido.setPrimerApellido("Pérez");
        requestValido.setEmail("juan@test.com");
        requestValido.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        requestValido.setPassword("test123");

        // Preparo el cliente que "devolverá" el repositorio
        clienteGuardado = new Cliente();
        clienteGuardado.setId(1L);
        clienteGuardado.setDni("12345678A");
        clienteGuardado.setNombre("Juan");
        clienteGuardado.setEmail("juan@test.com");
        clienteGuardado.setFechaCreacion(LocalDateTime.now());

        // Segundo cliente para la lista
        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setNombre("María");
        cliente2.setEmail("maria@test.com");
        cliente2.setDni("87654321B");
        cliente2.setPrimerApellido("Gómez");
        cliente2.setFechaNacimiento(LocalDate.of(1992, 2, 2));
        cliente2.setFechaCreacion(LocalDateTime.now());

        clientesMock = Arrays.asList(clienteGuardado, cliente2);


    }

    // DATOS INVALDOS - CREAR
    /**
     * Test de creación exitosa.
     * Valida que, ante datos correctos y sin duplicados, el servicio procese
     * el registro, cifre la contraseña y persista tanto al cliente como al usuario.
     */
    @Test
    void crearCliente_DatosValidos_ClienteGuardadoCorrectamente() {
        // Configuro el comportamiento del mock
        // Cuando hagamos existsByEmail con juan@test.com, devolvemos false
        when(clienteRepository.existsByEmail("juan@test.com")).thenReturn(false);
        // Cuando guardemos cualquier cliente, devolvemos clienteGuardado
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteGuardado);
        // Mock del encoder y del guardado del usuario
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashedpassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(new Usuario());

        // Ejecutamos el método a testear
        Cliente resultado = clienteService.crearCliente(requestValido);

        // Verificamos los resultados
        assertNotNull(resultado);                       // No debe ser null
        assertEquals(1L, resultado.getId());            // Debe tener ID 1
        assertEquals("Juan", resultado.getNombre());    // Nombre correcto
        assertEquals("juan@test.com", resultado.getEmail()); // Email correcto

        // Verificamos que hemos llamado a los metodos del repositorio
        verify(clienteRepository, times(1)).existsByEmail("juan@test.com");
        verify(clienteRepository, times(1)).save(any(Cliente.class));

        log.info("Test crearCliente_DatosValidos pasado");
    }

    /**
     * Test de validación: Email duplicado.
     * Verifica que el sistema impida el registro si el correo electrónico 
     * ya existe en el sistema, lanzando la excepción correspondiente.
     */
    @Test
    void crearCliente_EmailDuplicado_LanzaExcepcion() {
        // Simulamos que el email YA EXISTE
        when(clienteRepository.existsByEmail("juan@test.com")).thenReturn(true);

        // Verificamos que lanza excepcion
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.crearCliente(requestValido);
        });

        assertEquals("Ya existe un cliente con ese email", exception.getMessage());

        // Verificamos que NUNCA se haya llamado a save
        verify(clienteRepository, never()).save(any(Cliente.class));

        log.info("Test crearCliente_EmailDuplicado pasado");
    }

    /**
     * Test de validación: DNI duplicado.
     * Asegura que no se puedan registrar dos clientes con el mismo identificador legal.
     */
    @Test
    void crearCliente_DniDuplicado_LanzaExcepcion() {
        // Simulamos que el DNI YA EXISTE
        when(clienteRepository.existsByDni("12345678A")).thenReturn(true);

        // Verificamos que lanza excepcion
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.crearCliente(requestValido);
        });

        assertEquals("Ya existe un cliente con ese DNI", exception.getMessage());

        // Verificamos que NUNCA se haya llamado a save
        verify(clienteRepository, never()).save(any(Cliente.class));

        log.info("Test crearCliente_DniDuplicado pasado");
    }



    // SIN ATRIBUTOS OBLIGATORIOS
    /**
     * Test de validación: Campos obligatorios (Nombre).
     * Comprueba que la lógica de negocio rechace peticiones donde falte el nombre.
     */
    @Test
    void crearCliente_SinNombre_LanzaExcepcion() {
        // Request sin nombre
        requestValido.setNombre(null);

        // Verificamos que lanza excepcion
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.crearCliente(requestValido);
        });

        assertEquals("El nombre es obligatorio", exception.getMessage());

        // Verificamos que NUNCA se haya llamado a save
        verify(clienteRepository, never()).save(any(Cliente.class));

        log.info("Test crearCliente_SinNombre pasado");
    }

    /**
     * Prueba la validación de integridad al crear un cliente, asegurando que 
     * el email sea un campo obligatorio.
     */
    @Test
    void crearCliente_SinEmail_LanzaExcepcion() {
        // Request sin email
        requestValido.setEmail(null);

        // Verificamos que lanza excepcion
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.crearCliente(requestValido);
        });

        assertEquals("El email es obligatorio", exception.getMessage());

        // Verificamos que NUNCA se haya llamado a save
        verify(clienteRepository, never()).save(any(Cliente.class));

        log.info("Test crearCliente_SinEmail pasado");
    }

    /**
     * Prueba la validación de integridad al crear un cliente, asegurando que 
     * el DNI sea un campo obligatorio.
     */
    @Test
    void crearCliente_SinDni_LanzaExcepcion() {
        // Request sin email
        requestValido.setDni(null);

        // Verificamos que lanza excepcion
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.crearCliente(requestValido);
        });

        assertEquals("El DNI es obligatorio", exception.getMessage());

        // Verificamos que NUNCA se haya llamado a save
        verify(clienteRepository, never()).save(any(Cliente.class));

        log.info("Test crearCliente_SinDni pasado");
    }

    /**
     * Prueba la validación de integridad al crear un cliente, asegurando que 
     * la fecha de nacimiento sea un campo obligatorio.
     */
    @Test
    void crearCliente_SinNacimiento_LanzaExcepcion() {
        // Request sin email
        requestValido.setFechaNacimiento(null);

        // Verificamos que lanza excepcion
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.crearCliente(requestValido);
        });

        assertEquals("La fecha de nacimiento es obligatoria", exception.getMessage());

        // Verificamos que NUNCA se haya llamado a save
        verify(clienteRepository, never()).save(any(Cliente.class));

        log.info("Test crearCliente_SinNacimiento pasado");
    }

    /**
     * Test de validación: Formato de Email.
     * Verifica que el servicio valide sintácticamente el correo antes de procesar el alta.
     */
    @Test
    void crearCliente_EmailInvalido_LanzaExcepcion() {
        // Email sin @
        requestValido.setEmail("email-invalido");

        // Verifcamos que lanza excepcion
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.crearCliente(requestValido);
        });

        assertEquals("Email inválido", exception.getMessage());

        // Verificamos que NUNCA se haya llamado a save
        verify(clienteRepository, never()).save(any(Cliente.class));

        log.info("Test crearCliente_EmailInvalido pasado");
    }



    // BÚSQUEDAS
    /**
     * Test de consulta masiva.
     * Valida que el servicio transforme correctamente la respuesta del repositorio 
     * al listar todos los clientes registrados.
     */
    @Test
    void listarTodos_DeberiaRetornarTodosLosClientes() {
        // Cuando se ejecute el findAll, devolver el mock de clientes
        when(clienteRepository.findAll()).thenReturn(clientesMock);

        // Llamamos al metodo
        List<Cliente> resultado = clienteService.listarTodos();

        // Validamos
        assertThat(resultado).hasSize(2); // Que hay dos clientes
        assertThat(resultado).extracting(Cliente::getNombre)
                .containsExactly("Juan", "María"); // Con estos nombres

        // Validamos que se ha llamado al metodo 1 vez
        verify(clienteRepository, times(1)).findAll();
    }

    /**
     * Verifica que el servicio retorne una lista vacía (no nula) cuando 
     * no existen registros de clientes en el repositorio.
     */
    @Test
    void listarTodos_CuandoNoHayClientes_DeberiaRetornarListaVacia() {
        // Cuando se ejecute el findAll, devolver un Array vacío
        when(clienteRepository.findAll()).thenReturn(Arrays.asList());

        // Llamamos al metodo
        List<Cliente> resultado = clienteService.listarTodos();

        // Validamos que está vacío
        assertThat(resultado).isEmpty();

        // Validamos que se ha llamado al metodo 1 vez
        verify(clienteRepository, times(1)).findAll();
    }


    /**
     * Test de consulta por ID.
     * Verifica la recuperación exitosa de un cliente específico cuando el identificador existe.
     */
    @Test
    void buscarPorId_ClienteExiste_DevuelveCliente() {
        Long id = 1L;

        // Hacemos que si alguien llama al id = 1L le devuelva el clienteGuardado
        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteGuardado));
        Cliente resultado = clienteService.buscarPorId(id);

        // Verificamos que el resultado no es nulo y que concuerda con el getId
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());

        // Verificamos que se haya llamado a findById 1 vez
        verify(clienteRepository, times(1)).findById(id);

        log.info("Test buscarPorId_ClienteExiste pasado");
    }


    /**
     * Test de error en consulta: ID inexistente.
     * Valida que el servicio gestione la ausencia de datos mediante una excepción controlada.
     */
    @Test
    void buscarPorId_ClienteNoExiste_LanzaExcepcion() {
        // Hacemos que si alguien llama al id = 999L le devuelva algo nulo
        Long id = 999L;
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        // Verificamos que se haya lanzado la excepcion
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorId(id);
        });

        // Verificamos que se haya lanzado el mensaje
        assertEquals("Cliente no encontrado", exception.getMessage());

        // Verificamos que se ha consultado el metodo findById 1 vez
        verify(clienteRepository, times(1)).findById(id);

        log.info("Test buscarPorId_ClienteNoExiste pasado");
    }


    /**
     * Verifica que cuando se busca un cliente por un email existente,
     * el servicio retorne el objeto Cliente correctamente.
     */
    @Test
    void buscarPorEmail_ClienteExiste_DevuelveCliente() {
        String email = "juan@test.com";

        // Hacemos que si alguien llama al email = juan@test.com le devuelva el clienteGuardado
        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(clienteGuardado));
        Cliente resultado = clienteService.buscarPorEmail(email);

        // Verificamos que el resultado no es nulo y que concuerda con el getId
        assertNotNull(resultado);
        assertEquals(email, resultado.getEmail());

        // Verificamos que se haya llamado a findById 1 vez
        verify(clienteRepository, times(1)).findByEmail(email);

        log.info("Test buscarPorId_ClienteExiste pasado");
    }


    /**
     * Verifica que al buscar un cliente por un email que no existe en la base de datos,
     * el servicio lance una excepción indicando que no fue encontrado.
     */
    @Test
    void buscarPorEmail_ClienteNoExiste_LanzaExcepcion() {
        // Hacemos que si alguien llama al email le devuelva algo nulo
        String email = "juan@test.com";
        when(clienteRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Verificamos que se haya lanzado la excepcion
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorEmail(email);
        });

        // Verificamos que se haya lanzado el mensaje
        assertEquals("Cliente no encontrado", exception.getMessage());

        // Verificamos que se ha consultado el metodo findById 1 vez
        verify(clienteRepository, times(1)).findByEmail(email);

        log.info("Test buscarPorEmail_ClienteNoExiste pasado");
    }


    /**
     * Prueba la recuperación exitosa de un cliente utilizando su número de DNI.
     */
    @Test
    void buscarPorDni_ClienteExiste_DevuelveCliente() {
        String dni = "12345678A";

        // Hacemos que si alguien llama al email = juan@test.com le devuelva el clienteGuardado
        when(clienteRepository.findByDni(dni)).thenReturn(Optional.of(clienteGuardado));
        Cliente resultado = clienteService.buscarPorDni(dni);

        // Verificamos que el resultado no es nulo y que concuerda con el getId
        assertNotNull(resultado);
        assertEquals(dni, resultado.getDni());

        // Verificamos que se haya llamado a findById 1 vez
        verify(clienteRepository, times(1)).findByDni(dni);

        log.info("Test buscarPorDni_ClienteExiste pasado");
    }

    /**
     * Verifica que se lance una excepción cuando se intenta buscar por DNI de un cliente que no está registrado.
     */
    @Test
    void buscarPorDni_ClienteNoExiste_LanzaExcepcion() {
        // Hacemos que si alguien llama al dnii le devuelva algo nulo
        String dni = "12345678A";
        when(clienteRepository.findByDni(dni)).thenReturn(Optional.empty());

        // Verificamos que se haya lanzado la excepcion
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorDni(dni);
        });

        // Verificamos que se haya lanzado el mensaje
        assertEquals("Cliente no encontrado", exception.getMessage());

        // Verificamos que se ha consultado el metodo findById 1 vez
        verify(clienteRepository, times(1)).findByDni(dni);

        log.info("Test buscarPorDni_ClienteNoExiste pasado");
    }



    // DATOS INVALDOS - ACUALIZAR
    /**
     * Test de actualización exitosa.
     * Comprueba que se puedan modificar los datos de un cliente existente, 
     * validando que los nuevos datos (Email/DNI) no colisionen con otros registros.
     */
    @Test
    void actualizarCliente_DatosValidos_ClienteActualizadoCorrectamente() {
        Long id = 1L;

        // Simulamos que el cliente ya existe
        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteGuardado));
        // Simular que el usuario actual tiene email "juan@test.com" (del clienteGuardado)
        Usuario usuarioActual = new Usuario();
        usuarioActual.setEmail("juan@test.com");
        when(usuarioRepository.findByClienteId(id)).thenReturn(Optional.of(usuarioActual));
        when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(clienteRepository.existsByDni("87654321B")).thenReturn(false);

        // Request con datos actualizados
        ClienteRequest requestActualizado = new ClienteRequest();
        requestActualizado.setNombre("Juan Actualizado");
        requestActualizado.setDni("87654321B");
        requestActualizado.setEmail("nuevo@test.com");
        requestActualizado.setFechaNacimiento(LocalDate.of(1990, 1, 1));

        // Simulamos que save devuelve el cliente actualizado
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setId(1L);
        clienteActualizado.setNombre("Juan Actualizado");
        clienteActualizado.setDni("87654321B");
        clienteActualizado.setEmail("nuevo@test.com");

        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

        // Ejecutamos el método
        Cliente resultado = clienteService.actualizarCliente(id, requestActualizado);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals("Juan Actualizado", resultado.getNombre());
        assertEquals("nuevo@test.com", resultado.getEmail());
        assertEquals("87654321B", resultado.getDni());

        verify(clienteRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).existsByEmail("nuevo@test.com");
        verify(clienteRepository, times(1)).existsByDni("87654321B");
        verify(clienteRepository, times(1)).save(any(Cliente.class));

        log.info("Test actualizarCliente_DatosValidos_ClienteActualizadoCorrectamente pasado");
    }


    // Email duplicado
    /**
     * Prueba que al intentar actualizar un cliente con un email que ya pertenece a otro usuario,
     * se lance una excepción de tipo RuntimeException.
     */
    @Test
    void actualizarCliente_EmailDuplicado_LanzaExcepcion() {
        Long id = 1L;
        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteGuardado));
        // Simular usuario actual con email diferente al nuevo
        Usuario usuarioActual = new Usuario();
        usuarioActual.setEmail("juan@test.com");
        when(usuarioRepository.findByClienteId(id)).thenReturn(Optional.of(usuarioActual));
        when(usuarioRepository.existsByEmail("email@duplicado.com")).thenReturn(true);

        ClienteRequest request = new ClienteRequest();
        request.setEmail("email@duplicado.com");
        request.setNombre("Juan");
        request.setDni(clienteGuardado.getDni());
        request.setFechaNacimiento(LocalDate.of(1990,1,1));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.actualizarCliente(id, request);
        });

        assertEquals("Ya existe un usuario con ese email", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));

        log.info("Test actualizarCliente_EmailDuplicado_LanzaExcepcion pasado");

    }


    // DNI duplicado
    /**
     * Prueba que al intentar actualizar un cliente con un DNI que ya existe en el sistema,
     * se lance una excepción con el mensaje correspondiente.
     */
    @Test
    void actualizarCliente_DniDuplicado_LanzaExcepcion() {
        Long id = 1L;
        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteGuardado));
        when(clienteRepository.existsByDni("87654321B")).thenReturn(true);

        ClienteRequest request = new ClienteRequest();
        request.setEmail(clienteGuardado.getEmail());
        request.setNombre("Juan");
        request.setDni("87654321B");
        request.setFechaNacimiento(LocalDate.of(1990,1,1));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.actualizarCliente(id, request);
        });

        assertEquals("Ya existe un cliente con ese DNI", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));

        log.info("Test actualizarCliente_DniDuplicado_LanzaExcepcion pasado");

    }





    // ELIMINAR
    //Cliente existente
    /**
     * Test de borrado exitoso.
     * Verifica que el servicio llame correctamente al método de eliminación del 
     * repositorio cuando el cliente existe.
     */
    @Test
    void eliminarCliente_ClienteExiste_EliminadoCorrectamente() {
        Long id = 1L;
        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteGuardado));

        clienteService.eliminarCliente(id);

        verify(clienteRepository, times(1)).findById(id);
        verify(clienteRepository, times(1)).delete(clienteGuardado);

        log.info("Test eliminarCliente_ClienteExiste_EliminadoCorrectamente pasado");
    }

    //Cliente que no existe
    /**
     * Test de error en borrado: Cliente no encontrado.
     * Asegura que no se intente eliminar un registro que no existe en el sistema.
     */
    @Test
    void eliminarCliente_ClienteNoExiste_LanzaExcepcion() {
        Long id = 999L;
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.eliminarCliente(id);
        });

        assertEquals("Cliente no encontrado", exception.getMessage());
        verify(clienteRepository, never()).delete(any(Cliente.class));
    }

    @Test
@DisplayName("Debe actualizar correctamente teléfono y dirección del cliente")
void testActualizarPerfilPropio_ActualizacionCompleta() {

    // ===========
    // 1. Arrange
    // ===========

    String emailCliente = "cliente@test.com";

    Cliente clienteExistente = new Cliente();

    clienteExistente.setId(1L);
    clienteExistente.setNombre("Jon");

    clienteExistente.setTelefono("600111111");
    clienteExistente.setDireccion("Dirección antigua");

    clienteExistente.setEmail(emailCliente);

    ClienteUpdateDTO request = new ClienteUpdateDTO();

    request.setTelefono("699999999");
    request.setDireccion("Nueva dirección");

    when(clienteRepository.findByEmail(emailCliente))
            .thenReturn(Optional.of(clienteExistente));

    when(clienteRepository.save(any(Cliente.class)))
            .thenReturn(clienteExistente);

    // =======
    // 2. Act
    // =======

    Cliente resultado =
            clienteService.actualizarPerfilPropio(
                    emailCliente,
                    request
            );

    // ==========
    // 3. Assert
    // ==========

    assertNotNull(resultado);

    assertEquals(1L, resultado.getId());

    assertEquals(
            "699999999",
            resultado.getTelefono()
    );

    assertEquals(
            "Nueva dirección",
            resultado.getDireccion()
    );

    assertEquals(
            "Jon",
            resultado.getNombre()
    );

    assertEquals(
            emailCliente,
            resultado.getEmail()
    );

    verify(clienteRepository, times(1))
            .findByEmail(emailCliente);

    verify(clienteRepository, times(1))
            .save(clienteExistente);

    verifyNoMoreInteractions(clienteRepository);
}

@Test
@DisplayName("Debe actualizar únicamente el teléfono")
void testActualizarPerfilPropio_SoloTelefono() {

    // =============
    // 1. Arrange
    // =============

    String emailCliente = "cliente@test.com";

    Cliente clienteExistente = new Cliente();

    clienteExistente.setId(2L);

    clienteExistente.setTelefono("600000000");
    clienteExistente.setDireccion("Dirección original");

    ClienteUpdateDTO request = new ClienteUpdateDTO();

    request.setTelefono("611111111");

    request.setDireccion(null);

    when(clienteRepository.findByEmail(emailCliente))
            .thenReturn(Optional.of(clienteExistente));

    when(clienteRepository.save(any(Cliente.class)))
            .thenReturn(clienteExistente);

    // =========
    // 2. Act
    // =========

    Cliente resultado =
            clienteService.actualizarPerfilPropio(
                    emailCliente,
                    request
            );

    // ============
    // 3. Assert
    // ============

    assertNotNull(resultado);

    assertEquals(
            "611111111",
            resultado.getTelefono()
    );

    assertEquals(
            "Dirección original",
            resultado.getDireccion()
    );

    verify(clienteRepository, times(1))
            .findByEmail(emailCliente);

    verify(clienteRepository, times(1))
            .save(clienteExistente);

    verifyNoMoreInteractions(clienteRepository);
}

@Test
@DisplayName("Debe actualizar únicamente la dirección")
void testActualizarPerfilPropio_SoloDireccion() {

    // ===========
    // 1. Arrange
    // ===========

    String emailCliente = "cliente@test.com";

    Cliente clienteExistente = new Cliente();

    clienteExistente.setId(3L);

    clienteExistente.setTelefono("688888888");
    clienteExistente.setDireccion("Dirección antigua");

    ClienteUpdateDTO request = new ClienteUpdateDTO();

    request.setTelefono(null);

    request.setDireccion("Nueva dirección actualizada");

    when(clienteRepository.findByEmail(emailCliente))
            .thenReturn(Optional.of(clienteExistente));

    when(clienteRepository.save(any(Cliente.class)))
            .thenReturn(clienteExistente);

    // =========
    // 2. Act
    // =========

    Cliente resultado =
            clienteService.actualizarPerfilPropio(
                    emailCliente,
                    request
            );

    // ===========
    // 3. Assert
    // ===========

    assertNotNull(resultado);

    assertEquals(
            "688888888",
            resultado.getTelefono()
    );

    assertEquals(
            "Nueva dirección actualizada",
            resultado.getDireccion()
    );

    verify(clienteRepository, times(1))
            .findByEmail(emailCliente);

    verify(clienteRepository, times(1))
            .save(clienteExistente);

    verifyNoMoreInteractions(clienteRepository);
}

@Test
@DisplayName("No debe actualizar campos vacíos")
void testActualizarPerfilPropio_CamposVacios() {

    // ============
    // 1. Arrange
    // ============

    String emailCliente = "cliente@test.com";

    Cliente clienteExistente = new Cliente();

    clienteExistente.setTelefono("677777777");

    clienteExistente.setDireccion("Dirección correcta");

    ClienteUpdateDTO request = new ClienteUpdateDTO();

    request.setTelefono("");

    request.setDireccion("   ");

    when(clienteRepository.findByEmail(emailCliente))
            .thenReturn(Optional.of(clienteExistente));

    when(clienteRepository.save(any(Cliente.class)))
            .thenReturn(clienteExistente);

    // ==========
    // 2. Act
    // =========

    Cliente resultado =
            clienteService.actualizarPerfilPropio(
                    emailCliente,
                    request
            );

    // ===========
    // 3. Assert
    // ===========

    assertNotNull(resultado);

    assertEquals(
            "677777777",
            resultado.getTelefono()
    );

    assertEquals(
            "Dirección correcta",
            resultado.getDireccion()
    );

    verify(clienteRepository, times(1))
            .findByEmail(emailCliente);

    verify(clienteRepository, times(1))
            .save(clienteExistente);

    verifyNoMoreInteractions(clienteRepository);
}

@Test
@DisplayName("Debe lanzar excepción cuando el cliente no existe")
void testActualizarPerfilPropio_ClienteNoEncontrado() {

    // =============
    // 1. Arrange
    // =============

    String emailCliente = "inexistente@test.com";

    ClienteUpdateDTO request = new ClienteUpdateDTO();

    request.setTelefono("600000000");

    request.setDireccion("Dirección");

    when(clienteRepository.findByEmail(emailCliente))
            .thenReturn(Optional.empty());

    // ==================
    // 2. Act + Assert
    // ==================

    RuntimeException exception =
            assertThrows(
                    RuntimeException.class,
                    () -> clienteService.actualizarPerfilPropio(
                            emailCliente,
                            request
                    )
            );

    String mensajeEsperado = "Cliente no encontrado";

    String mensajeReal = exception.getMessage();

    assertEquals(
            mensajeEsperado,
            mensajeReal
    );

    verify(clienteRepository, times(1))
            .findByEmail(emailCliente);

    verify(clienteRepository, never())
            .save(any(Cliente.class));

    verifyNoMoreInteractions(clienteRepository);
}

@Test
@DisplayName("Debe guardar correctamente el cliente actualizado usando ArgumentCaptor")
void testActualizarPerfilPropio_ValidarObjetoGuardado() {

    // ============
    // 1. Arrange
    // ============

    String emailCliente = "cliente@test.com";

    Cliente clienteExistente = new Cliente();

    clienteExistente.setId(10L);

    clienteExistente.setNombre("Ane");

    clienteExistente.setTelefono("600000000");

    clienteExistente.setDireccion("Dirección antigua");

    clienteExistente.setEmail(emailCliente);

    ClienteUpdateDTO request = new ClienteUpdateDTO();

    request.setTelefono("699999999");

    request.setDireccion("Nueva dirección");

    when(clienteRepository.findByEmail(emailCliente))
            .thenReturn(Optional.of(clienteExistente));

    when(clienteRepository.save(any(Cliente.class)))
            .thenReturn(clienteExistente);

    // =========
    // 2. Act
    // =========

    Cliente resultado =
            clienteService.actualizarPerfilPropio(
                    emailCliente,
                    request
            );

    // ===========
    // 3. Assert
    // ===========

    assertNotNull(resultado);

    ArgumentCaptor<Cliente> captor =
            ArgumentCaptor.forClass(Cliente.class);

    verify(clienteRepository, times(1))
            .save(captor.capture());

    Cliente clienteGuardado = captor.getValue();

    assertNotNull(clienteGuardado);

    assertEquals(
            "699999999",
            clienteGuardado.getTelefono()
    );

    assertEquals(
            "Nueva dirección",
            clienteGuardado.getDireccion()
    );

    assertEquals(
            "Ane",
            clienteGuardado.getNombre()
    );

    assertEquals(
            emailCliente,
            clienteGuardado.getEmail()
    );

    assertSame(
            clienteExistente,
            clienteGuardado
    );

    verify(clienteRepository, times(1))
            .findByEmail(emailCliente);

    verifyNoMoreInteractions(clienteRepository);
}

@Test
@DisplayName("Debe mantener intactos los datos no modificados")
void testActualizarPerfilPropio_MantenerDatosOriginales() {

    // ============
    // 1. Arrange
    // ============

    String emailCliente = "cliente@test.com";

    LocalDate fechaNacimiento =
            LocalDate.of(1995, 5, 10);

    LocalDateTime fechaCreacion =
            LocalDateTime.now().minusYears(2);

    Cliente clienteExistente = new Cliente();

    clienteExistente.setId(20L);

    clienteExistente.setDni("12345678A");

    clienteExistente.setNombre("Iker");

    clienteExistente.setPrimerApellido("García");

    clienteExistente.setSegundoApellido("López");

    clienteExistente.setFechaNacimiento(fechaNacimiento);

    clienteExistente.setFechaCreacion(fechaCreacion);

    clienteExistente.setEmail(emailCliente);

    clienteExistente.setTelefono("611111111");

    clienteExistente.setDireccion("Dirección antigua");

    ClienteUpdateDTO request = new ClienteUpdateDTO();

    request.setTelefono("622222222");

    request.setDireccion(null);

    when(clienteRepository.findByEmail(emailCliente))
            .thenReturn(Optional.of(clienteExistente));

    when(clienteRepository.save(any(Cliente.class)))
            .thenReturn(clienteExistente);

    // ===========
    // 2. Act
    // ===========

    Cliente resultado =
            clienteService.actualizarPerfilPropio(
                    emailCliente,
                    request
            );

    // =============
    // 3. Assert
    // =============

    assertNotNull(resultado);

    // Campo actualizado
    assertEquals(
            "622222222",
            resultado.getTelefono()
    );

    // Campo no actualizado
    assertEquals(
            "Dirección antigua",
            resultado.getDireccion()
    );

    // Verificación de integridad de datos
    assertEquals(
            "12345678A",
            resultado.getDni()
    );

    assertEquals(
            "Iker",
            resultado.getNombre()
    );

    assertEquals(
            "García",
            resultado.getPrimerApellido()
    );

    assertEquals(
            "López",
            resultado.getSegundoApellido()
    );

    assertEquals(
            fechaNacimiento,
            resultado.getFechaNacimiento()
    );

    assertEquals(
            fechaCreacion,
            resultado.getFechaCreacion()
    );

    assertEquals(
            emailCliente,
            resultado.getEmail()
    );

    verify(clienteRepository, times(1))
            .findByEmail(emailCliente);

    verify(clienteRepository, times(1))
            .save(clienteExistente);

    verifyNoMoreInteractions(clienteRepository);
}

@Test
@DisplayName("Debe actualizar dirección aunque el teléfono esté vacío")
void testActualizarPerfilPropio_TelefonoVacioDireccionValida() {

    // ===========
    // 1. Arrange
    // ===========

    String emailCliente = "cliente@test.com";

    Cliente clienteExistente = new Cliente();

    clienteExistente.setId(30L);

    clienteExistente.setTelefono("633333333");

    clienteExistente.setDireccion("Dirección antigua");

    ClienteUpdateDTO request = new ClienteUpdateDTO();

    // Teléfono inválido
    request.setTelefono("   ");

    // Dirección válida
    request.setDireccion("Nueva dirección válida");

    when(clienteRepository.findByEmail(emailCliente))
            .thenReturn(Optional.of(clienteExistente));

    when(clienteRepository.save(any(Cliente.class)))
            .thenReturn(clienteExistente);

    // =========
    // 2. Act
    // =========
    Cliente resultado =
            clienteService.actualizarPerfilPropio(
                    emailCliente,
                    request
            );

    // ==============
    // 3. Assert
    // ==============

    assertNotNull(resultado);

    // El teléfono debe mantenerse
    assertEquals(
            "633333333",
            resultado.getTelefono()
    );

    // La dirección sí debe actualizarse
    assertEquals(
            "Nueva dirección válida",
            resultado.getDireccion()
    );

    verify(clienteRepository, times(1))
            .findByEmail(emailCliente);

    verify(clienteRepository, times(1))
            .save(clienteExistente);

    verifyNoMoreInteractions(clienteRepository);
}
}