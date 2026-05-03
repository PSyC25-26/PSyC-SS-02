package es.deusto.banca_online.controllers;

import es.deusto.banca_online.dto.ClienteRequest;
import es.deusto.banca_online.dto.ClienteResponse;
import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import es.deusto.banca_online.dto.ClienteUpdateDTO;
import org.springframework.security.core.Authentication;
import java.util.List;


@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    /*---------------
        ATRIBUTOS
    ---------------*/
    private final ClienteService clienteService;

    /*--------------------
        CONSTRUCTORES
    --------------------*/
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }


    /*---------------
        METODOS
    ---------------*/

    // CREAR cliente
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
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Cliente>> listarTodos() {
        List<Cliente> clientes = clienteService.listarTodos();
        return ResponseEntity.ok(clientes);
    }


    // BUSCAR cliente por ID
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
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> buscarPorEmail(@PathVariable String email) {
        try{
            Cliente cliente = clienteService.buscarPorEmail(email);
            return ResponseEntity.ok(mapToDto(cliente));
        }catch (RuntimeException e){
            if(e.getMessage().equals("Cliente no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Si no se ha encontrado el cliente,
                // mandamos un 404. Lo hacemos porque si no se lanza un NullPointerException (cliente=null).
            }
            throw e; // Otras excepciones se lanzan normalmente
        }

    }

    // ACTUALIZAR cliente
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


    @PutMapping("/perfil")
    @PreAuthorize("hasRole('CLIENTE')") // Solo usuarios con rol CLIENTE
    public ResponseEntity<ClienteResponse> actualizarMiPerfil(Authentication authentication, @RequestBody ClienteUpdateDTO request) {
        // authentication.getName() nos da el email del usuario que ha hecho login
        String emailActual = authentication.getName();

        Cliente actualizado = clienteService.actualizarPerfilPropio(emailActual, request);

        return ResponseEntity.ok(mapToDto(actualizado));
    }



    // MAPEO de entidad a DTO (Response)
    private ClienteResponse mapToDto(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getDni(),
                cliente.getNombre(),
                cliente.getPrimerApellido(),
                cliente.getSegundoApellido(),
                cliente.getFechaNacimiento(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                cliente.getFechaCreacion()
        );
    }
}