package es.deusto.banca_online.controllers;

import es.deusto.banca_online.dto.ClienteRequest;
import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.services.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ClienteControllerTest {

    @Autowired
    private ClienteService clienteService;

    // DATOS CORRECTOS - CREAR
    @Test
    void crearCliente_DatosValidos_Retorna201() {
        ClienteRequest request = new ClienteRequest();
        String uniqueDni = "DNI-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        request.setDni(uniqueDni);
        request.setNombre("Juan");
        request.setPrimerApellido("Perez");
        request.setEmail("juan@test.com");
        request.setFechaNacimiento(LocalDate.of(1990, 1, 1));

        Cliente response = clienteService.crearCliente(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Juan", response.getNombre());
        assertEquals("juan@test.com", response.getEmail());
        assertEquals(uniqueDni, response.getDni());
    }

    // DATOS CORRECTOS - BUSCAR por ID
    @Test
    void buscarPorId_ClienteExiste_Retorna200() {
        ClienteRequest request = new ClienteRequest();
        String uniqueDni = "DNI-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        request.setDni(uniqueDni);
        request.setNombre("Maria");
        request.setEmail("maria@test.com");
        request.setFechaNacimiento(LocalDate.of(1995, 5, 5));
        Cliente creado = clienteService.crearCliente(request);

        Cliente encontrado = clienteService.buscarPorId(creado.getId());

        assertNotNull(encontrado);
        assertEquals("Maria", encontrado.getNombre());
    }

    // DATOS INCORRECTOS - BUSCAR por ID inexistente
    @Test
    void buscarPorId_ClienteNoExiste_LanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> clienteService.buscarPorId(999L));
    }

    // DATOS CORRECTOS - BUSCAR por email
    @Test
    void buscarPorEmail_ClienteExiste_Retorna200() {
        ClienteRequest request = new ClienteRequest();
        String uniqueDni = "DNI-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        request.setDni(uniqueDni);
        request.setNombre("Pedro");
        request.setEmail("pedro@test.com");
        request.setFechaNacimiento(LocalDate.of(1988, 3, 3));
        clienteService.crearCliente(request);

        Cliente encontrado = clienteService.buscarPorEmail("pedro@test.com");

        assertNotNull(encontrado);
        assertEquals("pedro@test.com", encontrado.getEmail());
    }

    // DATOS INCORRECTOS - BUSCAR por email inexistente
    @Test
    void buscarPorEmail_ClienteNoExiste_LanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> clienteService.buscarPorEmail("noexiste@test.com"));
    }

    // DATOS CORRECTOS - ACTUALIZAR cliente
    @Test
    void actualizarCliente_DatosValidos_Retorna200() {
        ClienteRequest request = new ClienteRequest();
        String uniqueDni = "DNI-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        request.setDni(uniqueDni);
        request.setNombre("Ana");
        request.setEmail("ana@test.com");
        request.setFechaNacimiento(LocalDate.of(1992, 7, 7));
        Cliente creado = clienteService.crearCliente(request);

        ClienteRequest update = new ClienteRequest();
        String updateDni = "DNI-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        update.setDni(uniqueDni);
        update.setNombre("Ana Actualizada");
        update.setEmail("ana@test.com");
        update.setFechaNacimiento(LocalDate.of(1992, 7, 7));

        Cliente actualizado = clienteService.actualizarCliente(creado.getId(), update);

        assertNotNull(actualizado);
        assertEquals("Ana Actualizada", actualizado.getNombre());
    }

    // DATOS INCORRECTOS - ACTUALIZAR cliente inexistente
    @Test
    void actualizarCliente_ClienteNoExiste_LanzaExcepcion() {
        ClienteRequest update = new ClienteRequest();
        String uniqueDni = "DNI-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        update.setDni(uniqueDni);
        update.setNombre("Nadie");
        update.setEmail("nadie@test.com");
        update.setFechaNacimiento(LocalDate.of(2000, 1, 1));

        assertThrows(RuntimeException.class, () -> clienteService.actualizarCliente(999L, update));
    }

    // DATOS CORRECTOS - ELIMINAR cliente
    @Test
    void eliminarCliente_ClienteExiste_Retorna204() {
        ClienteRequest request = new ClienteRequest();
        String uniqueDni = "DNI-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        request.setDni(uniqueDni);
        request.setNombre("Luis");
        request.setEmail("luis@test.com");
        request.setFechaNacimiento(LocalDate.of(1985, 2, 2));
        Cliente creado = clienteService.crearCliente(request);

        assertDoesNotThrow(() -> clienteService.eliminarCliente(creado.getId()));
    }

    // DATOS INCORRECTOS - ELIMINAR cliente inexistente
    @Test
    void eliminarCliente_ClienteNoExiste_LanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> clienteService.eliminarCliente(999L));
    }
}