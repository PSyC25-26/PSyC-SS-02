package es.deusto.banca_online.services;

import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.ERol;
import es.deusto.banca_online.entity.Usuario;
import es.deusto.banca_online.repository.IClienteRepository;
import es.deusto.banca_online.repository.IUsuarioRepository;
import es.deusto.banca_online.dto.ClienteRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import es.deusto.banca_online.dto.ClienteUpdateDTO;

import java.util.List;


// Marcamos que es la clase que va a contener la logica de negocio y por tanto,
// se encarga de agrupar las funciones mas importantes que se usaran en los controllers
/**
 * Servicio encargado de la gestión de la lógica de negocio de Clientes.
 * Centraliza operaciones como el registro de nuevos clientes, la actualización 
 * de perfiles y la búsqueda de información personal bajo criterios de seguridad.
 */
@Service
public class ClienteService {
    /*---------------
        ATRIBUTO
   Utilizamos funciones del repositorio
    ---------------*/
    private final IClienteRepository clienteRepository;
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;




    /*--------------------
        CONSTRUCTOR
    --------------------*/
    /**
     * Constructor para la inyección de dependencias de ClienteService.
     * @param clienteRepository Repositorio para la gestión de datos de clientes.
     * @param usuarioRepository Repositorio para la gestión de credenciales y perfiles de usuario.
     * @param passwordEncoder Componente para el cifrado de contraseñas.
     */
    public ClienteService(IClienteRepository clienteRepository,
                          IUsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }




    /*--------------------
        TRANSACCIONES
          CON LA BD
    --------------------*/

    //CREAR/CREATE cliente
    /**
     * Registra un nuevo cliente en el sistema y crea su usuario correspondiente.
     * Cifra la contraseña antes de almacenarla y asigna el rol 'CLIENTE' por defecto.
     * @param request Datos del cliente y credenciales de acceso.
     * @return El objeto Cliente persistido en la base de datos.
     * @throws RuntimeException Si el DNI o el Email ya se encuentran registrados.
     */
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

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }




        // Si todo esta correcto, crear el cliente
        Cliente cliente = new Cliente();
        cliente.setDni(request.getDni());
        cliente.setNombre(request.getNombre());
        cliente.setPrimerApellido(request.getPrimerApellido());
        cliente.setSegundoApellido(request.getSegundoApellido());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        // Email en Cliente para compatibilidad con BD (columna aún es NOT NULL)
        // La fuente de verdad es Usuario.email
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setFechaCreacion(LocalDateTime.now());

        // Guardamos el cliente en la BD
        Cliente clienteGuardado = clienteRepository.save(cliente);

        // Creamos el Usuario vinculado al cliente
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(ERol.CLIENTE);
        usuario.setActivo(true);
        usuario.setCliente(clienteGuardado); // la relación JPA gestiona la FK
        usuarioRepository.save(usuario);

        return clienteGuardado;
    }


    // ACTUALIZAR/UPDATE cliente
    /**
     * Actualiza la información integral de un cliente existente.
     * Realiza validaciones de seguridad para asegurar que el nuevo DNI o Email 
     * no estén siendo utilizados por otros usuarios en el sistema.
     * @param id Identificador del cliente a modificar.
     * @param request DTO con los nuevos datos (DNI, nombre, contacto, etc.).
     * @return El objeto Cliente actualizado y persistido.
     * @throws RuntimeException Si el cliente no existe, o si el DNI/Email ya están en uso.
     */
    @Transactional
    public Cliente actualizarCliente(Long id, ClienteRequest request) {
        Cliente cliente = buscarPorId(id); // validar existencia del cliente

        // Validar email unico si se cambia (via UsuarioRepository)
        // Obtener email actual del usuario vinculado
        String emailActual = usuarioRepository.findByClienteId(cliente.getId())
                .map(Usuario::getEmail)
                .orElse(null);
        if (!request.getEmail().equals(emailActual) &&
                usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con ese email");
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
        // email no se actualiza en Cliente (es @Transient), se gestiona via Usuario
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());

        //Guardamos en la BD
        return clienteRepository.save(cliente);
    }



    // ELIMINAR/DELETE cliente
    /**
     * Elimina de forma definitiva un cliente de la base de datos.
     * @param id Identificador del cliente a eliminar.
     * @throws RuntimeException Si el identificador no corresponde a ningún cliente.
     */
    @Transactional
    public void eliminarCliente(Long id) {
        Cliente cliente = buscarPorId(id); // validar existencia del cliente
        clienteRepository.delete(cliente);
    }



    // BUSCAR/READ
    // todos los clientes
    /**
     * Recupera el listado completo de clientes registrados en el sistema.
     * @return Lista con todas las entidades Cliente.
     */
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }


    // por id. No esta en el repositorio porque JPA la da
    /**
     * Busca un cliente específico por su identificador único.
     * @param id ID del cliente.
     * @return La entidad Cliente encontrada.
     * @throws RuntimeException Si el cliente no existe (404 conceptual).
     */
    @Transactional(readOnly = true) //Indicamos que solo vamos a leer la BD y por tanto, el metodo no tiene permisos para modificarla.
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    // por email
    /**
     * Localiza un cliente a partir de su dirección de correo electrónico.
     * @param email Email asociado a la cuenta del cliente.
     * @return La entidad Cliente correspondiente.
     * @throws RuntimeException Si no se encuentra el perfil asociado a ese email.
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    // por dni
    /**
     * Localiza un cliente a partir de su número de DNI.
     * @param dni Documento Nacional de Identidad del cliente.
     * @return La entidad Cliente encontrada.
     * @throws RuntimeException Si el DNI no consta en la base de datos.
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorDni(String email) {
        return clienteRepository.findByDni(email)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    /**
     * Actualiza los datos de contacto del propio perfil del cliente autenticado.
     * @param emailActual Email del usuario que realiza la petición.
     * @param request DTO con los campos teléfono y dirección a actualizar.
     * @return El cliente con los datos actualizados.
     */
    @Transactional
    public Cliente actualizarPerfilPropio(String emailActual, ClienteUpdateDTO request) {
        Cliente cliente = buscarPorEmail(emailActual);

        // Si el campo del request no es nulo ni está vacío, actualizamos.
        // De lo contrario, mantenemos el valor actual de la entidad.
        if (request.getTelefono() != null && !request.getTelefono().isBlank()) {
            cliente.setTelefono(request.getTelefono());
        }

        if (request.getDireccion() != null && !request.getDireccion().isBlank()) {
            cliente.setDireccion(request.getDireccion());
        }

        return clienteRepository.save(cliente);
    }
}