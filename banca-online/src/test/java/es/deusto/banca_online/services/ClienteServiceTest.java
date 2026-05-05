package es.deusto.banca_online.services;

import es.deusto.banca_online.dto.ClienteRequest;
import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.Usuario;
import es.deusto.banca_online.repository.IClienteRepository;
import es.deusto.banca_online.repository.IUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock  // Crea un repositorio falso que simula el comportamiento de la base de datos
    private IClienteRepository clienteRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks  // Inyecta los mocks dentro de ClienteService
    private ClienteService clienteService;

    private ClienteRequest requestValido;
    private Cliente clienteGuardado;
    private List<Cliente> clientesMock;




    // Preparacion antes de cada test. Así no repetimos código en cada test.
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
}