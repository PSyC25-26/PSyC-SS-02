package es.deusto.banca_online.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.deusto.banca_online.dto.ClienteRequest;
import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.Usuario;
import es.deusto.banca_online.repository.IUsuarioRepository;
import es.deusto.banca_online.security.JwtUtils;
import es.deusto.banca_online.security.UserDetailsServiceImpl;
import es.deusto.banca_online.services.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

// NUEVA IMPORTACIÓN - Reemplaza a MockBean
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//Indicamos que vamos a arrancar la capa web
@SpringBootTest
@WithMockUser(roles = "ADMIN")
class ClienteControllerTest {

    private static final Logger log = LoggerFactory.getLogger(ClienteControllerTest.class);

    /*---------------
        ATRIBUTOS
    ---------------*/
    private MockMvc mockMvc;  // Simulamos peticiones HTTP.

    @MockitoBean
    private ClienteService clienteService;  // Mock del servicio

    @MockitoBean
    private IUsuarioRepository usuarioRepository;  // Mock del repositorio de usuarios

    @MockitoBean
    private JwtUtils jwtUtils;  // Necesario porque JwtAuthFilter es un Filter incluido en el slice

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;  // Necesario por JwtAuthFilter

    private ObjectMapper objectMapper; // Convierte objetos a JSON

    private ClienteRequest clienteRequest;
    private Cliente clienteEntity;
    private LocalDateTime fechaFija;
    private List<Cliente> clientesMock;


    /*
    FLUJO QUE VA A TENER:
    clienteRequest --> ObjectMapper --> JSON --> MockMvc --> Controller --> ClienteService --> Respuesta JSON
     */



    // Preparacion antes de cada test. Así no repetimos código en cada test.
    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(org.springframework.security.test.web.servlet
                        .setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
        // Fecha fija para los tests
        fechaFija = LocalDateTime.of(2024, 3, 16, 10, 30);

        // Hay que configurar manualmente el mapper para que entienda fechas
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()); // Indica como pasar de fecha a JSON y viceversa
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Indicamos que no serialice, ya que queremos que las serialices en formato legible

        // Preparar ClienteRequest (lo que envia el usuario)
        clienteRequest = new ClienteRequest();
        clienteRequest.setDni("12345678A");
        clienteRequest.setNombre("Juan");
        clienteRequest.setPrimerApellido("Pérez");
        clienteRequest.setEmail("juan@test.com");
        clienteRequest.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        clienteRequest.setPassword("test123");

        // Preparar Cliente (lo que devuelve el servicio)
        // Lo mismo que lo que envía el usuario pero con ID y fechaCreacion, pues eso lo asigna la BD
        clienteEntity = new Cliente();
        clienteEntity.setId(1L);
        clienteEntity.setDni("12345678A");
        clienteEntity.setNombre("Juan");
        clienteEntity.setPrimerApellido("Pérez");
        clienteEntity.setEmail("juan@test.com");
        clienteEntity.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        clienteEntity.setFechaCreacion(fechaFija);

        // Crear segundo cliente para la lista
        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setNombre("María");
        cliente2.setEmail("maria@test.com");
        cliente2.setDni("87654321B");
        cliente2.setPrimerApellido("Gómez");
        cliente2.setFechaNacimiento(LocalDate.of(1992, 2, 2));
        cliente2.setFechaCreacion(fechaFija);

        // Inicializar lista de clientesMock para los tests
        clientesMock = Arrays.asList(clienteEntity, cliente2);

    }



    /*---------------
        TESTS
    ---------------*/

    // DATOS CORRECTOS - CREAR
    @Test
    void crearCliente_DatosValidos_Retorna201() throws Exception {
        log.info("Test: POST /api/clientes con datos validos debe retornar 201");
        // Cuando se cree un cliente, devolvemos el que crearia la BD
        when(clienteService.crearCliente(any(ClienteRequest.class))).thenReturn(clienteEntity);
        // Simular que existe un usuario asociado con el email
        Usuario usuarioMock = new Usuario();
        usuarioMock.setEmail("juan@test.com");
        when(usuarioRepository.findByClienteId(1L)).thenReturn(java.util.Optional.of(usuarioMock));

        // Simulamos la peticion a /api/clientes
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el body de la petición es JSON
                        .content(objectMapper.writeValueAsString(clienteRequest))) // Escribimos el body en modo JSON
                .andExpect(status().isCreated())  // Esperamos un 201 Created
                .andExpect(jsonPath("$.id").value(1)) // Que el cliente tenga ID 1
                .andExpect(jsonPath("$.nombre").value("Juan")) // Que el cliente tenga nombre Juan
                .andExpect(jsonPath("$.email").value("juan@test.com")) // Que el cliente tenga email juan@test.com
                .andExpect(jsonPath("$.dni").value("12345678A")) // Que el cliente tenga DNI 12345678A
                .andExpect(jsonPath("$.fechaCreacion").exists()); // Que el cliente tenga una fecha de creacion

        // Verificamos que se ha llamado a crearCliente 1 vez
        verify(clienteService, times(1)).crearCliente(any(ClienteRequest.class));
        log.info("Test pasado: cliente creado correctamente");
    }


    // DATOS INCORRECTOS - CREAR
    @Test
    void crearCliente_SinNombre_Retorna400() throws Exception {
        log.info("Test: POST /api/clientes sin nombre debe retornar 400");
        // Request sin nombre
        clienteRequest.setNombre(null);

        // Llamamos al endpoint
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el body de la petición es JSON
                        .content(objectMapper.writeValueAsString(clienteRequest))) // Pasamos el clienteRequest como body
                .andExpect(status().isBadRequest());  // Esperamos un 400 Bad Request

        // Verificar que NO se haya llamado al servicio
        verify(clienteService, never()).crearCliente(any(ClienteRequest.class));
        log.info("Test pasado: 400 retornado por falta de nombre");
    }




    // DATOS CORRECTOS - Obtener todos los clientes
    @Test
    void listarTodos_CuandoHayClientes_DeberiaRetornarLista() throws Exception {
        log.info("Test: GET /api/clientes con datos debe retornar lista");
        // Cuando se llame al metodo listarTodos, utilizamos el clientesMock
        when(clienteService.listarTodos()).thenReturn(clientesMock);

        // Llamamos al endpoint y verificamos los datos
        mockMvc.perform(get("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Juan"))
                .andExpect(jsonPath("$[0].email").value("juan@test.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nombre").value("María"))
                .andExpect(jsonPath("$[1].email").value("maria@test.com"));

        // Verificamos que se haya llamado al servicio listarTodos 1 vez
        verify(clienteService, times(1)).listarTodos();
        log.info("Test pasado: lista de clientes retornada correctamente");
    }

    // DATOS INCORRECTOS - Obtener todos los clientes
    @Test
    void listarTodos_CuandoNoHayClientes_DeberiaRetornarListaVacia() throws Exception {
        log.info("Test: GET /api/clientes sin datos debe retornar lista vacia");
        // Cuando se llame al metodo listarTodos, utilizamos un Array vacío
        when(clienteService.listarTodos()).thenReturn(Arrays.asList());

        // Llamamos al endpoint y verificamos los datos
        mockMvc.perform(get("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Que ha devuelto el status 200
                .andExpect(jsonPath("$", hasSize(0))); // Que el array tiene size 0

        // Verificamos que se haya llamado al servicio listarTodos 1 vez
        verify(clienteService, times(1)).listarTodos();
        log.info("Test pasado: lista vacia retornada correctamente");
    }



    // DATOS CORRECTOS - BUSCAR por ID
    @Test
    void buscarPorId_ClienteExiste_Retorna200() throws Exception {
        log.info("Test: GET /api/clientes/{id} con id existente debe retornar 200");
        // Preparamos el usuario con id=1
        Long id = 1L;
        when(clienteService.buscarPorId(id)).thenReturn(clienteEntity);

        // Llamamos al endpoint
        mockMvc.perform(get("/api/clientes/{id}", id))
                .andExpect(status().isOk()) // Esperamos un OK
                .andExpect(jsonPath("$.id").value(1)) // Que tenga id=1
                .andExpect(jsonPath("$.nombre").value("Juan")); // Y nombre=Juan

        // Verificamos que se haya llamado al servicio buscarPorId 1 vez
        verify(clienteService, times(1)).buscarPorId(id);
        log.info("Test pasado: cliente con id={} encontrado", id);
    }



    // DATOS INCORRECTOS - BUSCAR por ID
    @Test
    void buscarPorId_ClienteNoExiste_Retorna404() throws Exception {
        log.info("Test: GET /api/clientes/{id} con id inexistente debe retornar 404");
        // Buscamos usuario con id inexistente
        Long id = 999L;
        when(clienteService.buscarPorId(id)).thenThrow(new RuntimeException("Cliente no encontrado"));

        // Llamamos al endpoint
        mockMvc.perform(get("/api/clientes/{id}", id))
                .andExpect(status().isNotFound());  // Esperamos un 404 Not Found

        // Verificamos que se haya llamado al servicio buscarPorId 1 vez
        verify(clienteService, times(1)).buscarPorId(id);
        log.info("Test pasado: cliente con id={} no encontrado", id);
    }


    // DATOS CORRECTOS - BUSCAR por email
    @Test
    void buscarPorEmail_ClienteExiste_Retorna200() throws Exception {
        log.info("Test: GET /api/clientes/email/{email} con email existente debe retornar 200");

        // Simular que el servicio encuentra el cliente
        when(clienteService.buscarPorEmail("juan@test.com")).thenReturn(clienteEntity);
        // Simular que existe un usuario asociado con el email
        Usuario usuarioMock = new Usuario();
        usuarioMock.setEmail("juan@test.com");
        when(usuarioRepository.findByClienteId(1L)).thenReturn(java.util.Optional.of(usuarioMock));

        // Llamamos al endpoint
        mockMvc.perform(get("/api/clientes/email/{email}", "juan@test.com"))
                .andExpect(status().isOk()) // Esperamos un OK
                .andExpect(jsonPath("$.email").value("juan@test.com")); // Verificamos el email

        // Verificamos que se haya llamado al servicio buscarPorEmail 1 vez
        verify(clienteService, times(1)).buscarPorEmail("juan@test.com");
        log.info("Test pasado: cliente con email={} encontrado", "juan@test.com");
    }


    // DATOS INCORRECTOS - BUSCAR por email
    @Test
    void buscarPorEmail_ClienteNoExiste_Retorna404() throws Exception {
        log.info("Test: GET /api/clientes/email/{email} con email inexistente debe retornar 404");
        // Preparamos el email
        String email = "noexiste@test.com";
        when(clienteService.buscarPorEmail(email)).thenThrow(new RuntimeException("Cliente no encontrado"));

        // Llamamos al endpoint
        mockMvc.perform(get("/api/clientes/email/{email}", email))
                .andExpect(status().isNotFound()); // Esperamos un 404

        // Verificamos que se haya llamado al servicio buscarPorEmail 1 vez
        verify(clienteService, times(1)).buscarPorEmail(email);
        log.info("Test pasado: 404 retornado para email={}", email);
    }






    // DATOS CORRECTOS - ACTUALIZAR cliente
    @Test
    void actualizarCliente_DatosValidos_Retorna200() throws Exception {
        log.info("Test: PUT /api/clientes/{id} con datos validos debe retornar 200");
        // Preparamos el cliente a actualizar
        Long id = 1L;

        // Creamos un cliente actualizado con el nuevo nombre
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setId(id);
        clienteActualizado.setNombre("Juan Actualizado");
        clienteActualizado.setEmail("juan@test.com");
        clienteActualizado.setDni("12345678A");

        // Cuando se llame a actualzarCliente, devolver clienteActualizado
        when(clienteService.actualizarCliente(eq(id), any(ClienteRequest.class))).thenReturn(clienteActualizado);

        // Llamamos al endpoint
        mockMvc.perform(put("/api/clientes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el body de la petición es JSON
                        .content(objectMapper.writeValueAsString(clienteRequest))) // Escribimos el body en modo JSON
                .andExpect(status().isOk()) // Esperamos un OK
                .andExpect(jsonPath("$.id").value(1)) // Verificamos que tiene el ID actualizado
                .andExpect(jsonPath("$.nombre").value("Juan Actualizado")); // Y que el nombre sigue siendo el mismo

        // Verificamos que se haya llamado al servicio actualizarCliente 1 vez
        verify(clienteService, times(1)).actualizarCliente(eq(id), any(ClienteRequest.class));
        log.info("Test pasado: cliente con id={} actualizado", id);
    }



    // DATOS INCORRECTOS - ACTUALIZAR cliente
    @Test
    void actualizarCliente_ClienteNoExiste_Retorna404() throws Exception {
        log.info("Test: PUT /api/clientes/{id} con id inexistente debe retornar 404");
        // Preparamos un ID inexistente
        Long id = 999L;

        // Cuando se llame a actualizar cliente, devolvemos el error "Cliente no encontrado"
        when(clienteService.actualizarCliente(eq(id), any(ClienteRequest.class)))
                .thenThrow(new RuntimeException("Cliente no encontrado"));

        // Llamamos al endpoint
        mockMvc.perform(put("/api/clientes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON) // Indicamos que el body de la petición es JSON
                        .content(objectMapper.writeValueAsString(clienteRequest))) // Escribimos el body en modo JSON
                .andExpect(status().isNotFound()); // Verificamos que ha devuelto un 404

        // Verificamos que se haya llamado al servicio actualizarCliente 1 vez
        verify(clienteService, times(1)).actualizarCliente(eq(id), any(ClienteRequest.class));
        log.info("Test pasado: 404 retornado para id={}", id);
    }






    // DATOS CORRECTOS - ELIMINAR cliente
    @Test
    void eliminarCliente_ClienteExiste_Retorna204() throws Exception {
        log.info("Test: DELETE /api/clientes/{id} con id existente debe retornar 204");
        // Preparamos el id del cliente a eliminar
        Long id = 1L;

        // No hacemos nada cuando se llame a eliminarCliente
        doNothing().when(clienteService).eliminarCliente(id);

        // Llamamos al endpoint
        mockMvc.perform(delete("/api/clientes/{id}", id))
                .andExpect(status().isNoContent());  // Esperamos un 204 No Content

        // Verificamos que se haya llamado al servicio eliminarCliente 1 vez
        verify(clienteService, times(1)).eliminarCliente(id);
        log.info("Test pasado: cliente con id={} eliminado", id);
    }




    // DATOS INCORRECTOS - ELIMINAR cliente
    @Test
    void eliminarCliente_ClienteNoExiste_Retorna404() throws Exception {
        log.info("Test: DELETE /api/clientes/{id} con id inexistente debe retornar 404");
        // Preparamos un id inexistente
        Long id = 999L;

        // Devolvemos la excepción cuando se llame a eliminarCliente
        doThrow(new RuntimeException("Cliente no encontrado")).when(clienteService).eliminarCliente(id);

        // Llamamos al endpoint
        mockMvc.perform(delete("/api/clientes/{id}", id))
                .andExpect(status().isNotFound()); // Esperamos un 404 Not Found

        // Verificamos que se haya llamado al servicio eliminarCliente 1 vez
        verify(clienteService, times(1)).eliminarCliente(id);
        log.info("Test pasado: 404 retornado para id={}", id);
    }
}