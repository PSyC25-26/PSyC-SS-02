package es.deusto.banca_online.controllers;

import es.deusto.banca_online.dto.ClienteRequest;
import es.deusto.banca_online.dto.ClienteResponse;
import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.repository.IUsuarioRepository;
import es.deusto.banca_online.security.AuthChecks;
import es.deusto.banca_online.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import es.deusto.banca_online.dto.ClienteUpdateDTO;
import org.springframework.security.core.Authentication;
import java.util.List;

/**
 * Controlador para la gestión de clientes.
 * Permite a los administradores gestionar clientes y a los usuarios actualizar su propio perfil.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    /*---------------
        ATRIBUTOS
    ---------------*/
    private final ClienteService clienteService;
    private final AuthChecks authChecks;
    private final IUsuarioRepository usuarioRepository;

    /*--------------------
        CONSTRUCTORES
    --------------------*/
    public ClienteController(ClienteService clienteService, AuthChecks authChecks,
                             IUsuarioRepository usuarioRepository) {
        this.clienteService = clienteService;
        this.authChecks = authChecks;
        this.usuarioRepository = usuarioRepository;
    }


    /*---------------
        METODOS
    ---------------*/

    // CREAR cliente
    /**
     * Registra un nuevo cliente y su usuario asociado en el sistema.
     * @param request Datos del nuevo cliente (DNI, nombre, email, password, etc.).
     * @return El cliente creado con su ID asignado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> crearCliente(@RequestBody @Valid ClienteRequest request) {
        //El cliente aún no tiene ID ni fecha_creacion. El servicio lo crea y guarda en BD
        Cliente clienteCreado = clienteService.crearCliente(request);

        //Ahora sí, tiene ID ni fecha_creacion y es por ello que hay que mapearlo
        ClienteResponse response = mapToDto(clienteCreado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // CARGAR clientes
    /**
     * Recupera una lista completa de todos los clientes registrados en el sistema.
     * Este endpoint está restringido exclusivamente a usuarios con privilegios de administrador.
     * * @return ResponseEntity con la lista de todas las entidades Cliente y estado 200 OK.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Cliente>> listarTodos() {
        List<Cliente> clientes = clienteService.listarTodos();
        return ResponseEntity.ok(clientes);
    }


    // BUSCAR cliente por ID
    /**
     * Busca un cliente específico mediante su identificador único.
     * Acceso restringido a usuarios con rol de administrador.
     * * @param id Identificador numérico del cliente en la base de datos.
     * @return ResponseEntity con los datos del cliente (200 OK) si existe, 
     * o estado 404 Not Found si el cliente no está registrado.
     * @throws RuntimeException Si ocurre un error inesperado distinto a la ausencia del cliente.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        try{
            Cliente cliente = clienteService.buscarPorId(id);
            return ResponseEntity.ok(mapToDto(cliente));
        }catch (RuntimeException e){
            if(e.getMessage().equals("Cliente no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Si no se ha encontrado el cliente,
                // mandamos un 404. Lo hacemos porque si no se lanza un NullPointerException (cliente=null).
            }
            throw e; // Otras excepciones se lanzan normalmente
        }
    }

    // BUSCAR cliente por email
    /**
     * Busca un cliente en el sistema utilizando su dirección de correo electrónico.
     * * Implementa una restricción de seguridad:
     * - Si el usuario tiene rol 'ADMIN', puede buscar cualquier email.
     * - Si el usuario tiene rol 'CLIENTE', solo puede consultar su propio email.
     * * @param email Dirección de correo del cliente a consultar.
     * @param authentication Objeto que contiene las credenciales del usuario actual.
     * @return ResponseEntity con los datos del cliente (200 OK), 
     * 403 Forbidden si un cliente intenta ver datos ajenos,
     * o 404 Not Found si el email no existe en el sistema.
     * @throws RuntimeException Si ocurre un error interno durante la búsqueda.
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')") //
    public ResponseEntity<ClienteResponse> buscarPorEmail(@PathVariable String email, Authentication authentication) {
        try {
            // Validación de seguridad: un CLIENTE solo puede ver su propio perfil
            String emailLogueado = authentication.getName();
            if (!authChecks.isAdmin(authentication) && !emailLogueado.equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Cliente cliente = clienteService.buscarPorEmail(email);
            return ResponseEntity.ok(mapToDto(cliente));
        } catch (RuntimeException e) {
            if ("Cliente no encontrado".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    // ACTUALIZAR cliente
    /**
     * Actualiza de forma integral los datos de un cliente existente.
     * Esta operación solo puede ser realizada por usuarios con privilegios de administrador.
     * * @param id Identificador numérico del cliente que se desea modificar.
     * @param request Objeto DTO que contiene los nuevos datos del cliente (DNI, nombre, etc.).
     * @return ResponseEntity con los datos del cliente ya actualizados (200 OK),
     * o estado 404 Not Found si el identificador proporcionado no corresponde a ningún cliente.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso de actualización.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> actualizarCliente(
            @PathVariable Long id,
            @RequestBody @Valid ClienteRequest request) {

        try{
            Cliente clienteActualizado = clienteService.actualizarCliente(id, request);
            return ResponseEntity.ok(mapToDto(clienteActualizado));
        }catch (RuntimeException e){
            if(e.getMessage().equals("Cliente no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Si no se ha encontrado el cliente,
                // mandamos un 404. Lo hacemos porque si no se lanza un NullPointerException (cliente=null).
            }
            throw e; // Otras excepciones se lanzan normalmente
        }
    }



    // ELIMINAR cliente
    /**
     * Elimina de forma permanente un cliente del sistema mediante su identificador.
     * Esta acción es irreversible y está restringida a usuarios con rol de administrador.
     * * @param id Identificador numérico del cliente que se desea eliminar.
     * @return ResponseEntity con estado 204 No Content si la eliminación fue exitosa,
     * o estado 404 Not Found si el cliente con el ID proporcionado no existe.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso de borrado.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        try{
            clienteService.eliminarCliente(id);
            return ResponseEntity.noContent().build();
        }catch (RuntimeException e){
            if(e.getMessage().equals("Cliente no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Si no se ha encontrado el cliente,
                // mandamos un 404. Lo hacemos porque si no se lanza un NullPointerException (cliente=null).
            }
            throw e; // Otras excepciones se lanzan normalmente
        }
    }

    /**
     * Actualiza la información del perfil del cliente autenticado actualmente.
     * @param authentication Objeto que contiene el email del usuario logueado.
     * @param request Datos actualizables del perfil.
     * @return El cliente con los datos actualizados.
     */
    @PutMapping("/perfil")
    @PreAuthorize("hasRole('CLIENTE')") // Solo usuarios con rol CLIENTE
    public ResponseEntity<ClienteResponse> actualizarMiPerfil(Authentication authentication, @RequestBody ClienteUpdateDTO request) {
        // authentication.getName() nos da el email del usuario que ha hecho login
        String emailActual = authentication.getName();

        Cliente actualizado = clienteService.actualizarPerfilPropio(emailActual, request);

        return ResponseEntity.ok(mapToDto(actualizado));
    }



    // MAPEO de entidad a DTO (Response)
    /**
     * Convierte una entidad de tipo Cliente en un objeto de transferencia de datos (DTO) ClienteResponse.
     * Este método realiza una consulta adicional al repositorio de usuarios para obtener el email 
     * actualizado, asegurando que se devuelva la "fuente de verdad" del sistema de autenticación.
     * * @param cliente La entidad Cliente recuperada de la base de datos.
     * @return Un objeto ClienteResponse con todos los campos formateados para su envío a través de la API.
     */
    private ClienteResponse mapToDto(Cliente cliente) {
        // Obtener email desde Usuario (fuente de verdad)
        String email = usuarioRepository.findByClienteId(cliente.getId())
                .map(u -> u.getEmail())
                .orElse(null);
        return new ClienteResponse(
                cliente.getId(),
                cliente.getDni(),
                cliente.getNombre(),
                cliente.getPrimerApellido(),
                cliente.getSegundoApellido(),
                cliente.getFechaNacimiento(),
                email, // desde Usuario
                cliente.getTelefono(),
                cliente.getDireccion(),
                cliente.getFechaCreacion()
        );
    }
}