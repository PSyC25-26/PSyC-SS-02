package es.deusto.banca_online.services;

import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.repository.IClienteRepository;
import es.deusto.banca_online.dto.ClienteRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;


// Marcamos que es la clase que va a contener la logica de negocio y por tanto,
// se encarga de agrupar las funciones mas importantes que se usaran en los controllers
@Service
public class ClienteService {
    /*---------------
        ATRIBUTO
   Utilizamos funciones del repositorio
    ---------------*/
    private final IClienteRepository clienteRepository;




    /*--------------------
        CONSTRUCTOR
    --------------------*/
    public ClienteService(IClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }




    /*--------------------
        TRANSACCIONES
          CON LA BD
    --------------------*/

    //CREAR/CREATE cliente
    @Transactional
    public Cliente crearCliente(ClienteRequest request) {
        // Validaciones extra
        // Email unico
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe un cliente con ese email");
        }

        // DNI unico
        if (clienteRepository.existsByDni(request.getDni())){
            throw new RuntimeException("Ya existe un cliente con ese DNI");
        }

        // Campos obligatorios. Aunque en el DTO ya está, así tenemos doble check y podemos comprobarlo en los tests.
        if (request.getDni() == null || request.getDni().trim().isEmpty()) {
            throw new RuntimeException("El DNI es obligatorio");
        }

        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre es obligatorio");
        }

        if (request.getFechaNacimiento() == null) {
            throw new RuntimeException("La fecha de nacimiento es obligatoria");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("El email es obligatorio");
        }

        // Validación de formato de email
        if (!request.getEmail().contains("@")) {
            throw new RuntimeException("Email inválido");
        }




        // Si todo esta correcto, crear el cliente
        Cliente cliente = new Cliente();
        cliente.setDni(request.getDni());
        cliente.setNombre(request.getNombre());
        cliente.setPrimerApellido(request.getPrimerApellido());
        cliente.setSegundoApellido(request.getSegundoApellido());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setFechaCreacion(LocalDateTime.now());

        //Guardamos en la BD
        return clienteRepository.save(cliente);
    }


    // ACTUALIZAR/UPDATE cliente
    @Transactional
    public Cliente actualizarCliente(Long id, ClienteRequest request) {
        Cliente cliente = buscarPorId(id); // validar existencia del cliente

        // Validar email unico si se cambia
        if (!cliente.getEmail().equals(request.getEmail()) &&
                clienteRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe un cliente con ese email");
        }

        // Validar DNI unico si se cambia
        if (!cliente.getDni().equals(request.getDni()) &&
                clienteRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("Ya existe un cliente con ese DNI");
        }

        // Actualizar campos
        cliente.setDni(request.getDni());
        cliente.setNombre(request.getNombre());
        cliente.setPrimerApellido(request.getPrimerApellido());
        cliente.setSegundoApellido(request.getSegundoApellido());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());

        //Guardamos en la BD
        return clienteRepository.save(cliente);
    }



    // ELIMINAR/DELETE cliente
    @Transactional
    public void eliminarCliente(Long id) {
        Cliente cliente = buscarPorId(id); // validar existencia del cliente
        clienteRepository.delete(cliente);
    }



    // BUSCAR/READ
    // por id. No esta en el repositorio porque JPA la da
    @Transactional(readOnly = true) //Indicamos que solo vamos a leer la BD y por tanto
    //el metodo no tiene permisos para modificarla.
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    // por email
    @Transactional(readOnly = true)
    public Cliente buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    // por dni
    @Transactional(readOnly = true)
    public Cliente buscarPorDni(String email) {
        return clienteRepository.findByDni(email)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }
}