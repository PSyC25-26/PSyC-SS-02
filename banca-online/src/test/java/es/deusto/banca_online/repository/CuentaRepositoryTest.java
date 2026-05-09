package es.deusto.banca_online.repository;

import es.deusto.banca_online.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de sistema: verifican que la capa de persistencia funciona
 * correctamente contra la base de datos real.
 * Cada test usa @Transactional para hacer rollback automatico.
 */
@SpringBootTest
@Transactional
class CuentaRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(CuentaRepositoryTest.class);

    /**
     * Repositorio de cuentas bajo prueba.
     */
    @Autowired private ICuentaRepository cuentaRepository;
    /**
     * Repositorio de clientes necesario para establecer las relaciones de clave ajena (FK)
     * durante las pruebas de cuentas.
     */
    @Autowired private IClienteRepository clienteRepository;

    /**
     * Instancia de cliente compartida para los tests de una misma ejecución.
     */
    private Cliente cliente;

    /**
     * Configuración inicial antes de cada test.
     * Crea y persiste un cliente con datos aleatorios (UUID) para servir como 
     * propietario de las cuentas que se crearán en los métodos de prueba.
     */
    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setDni("DNI-REPO-" + UUID.randomUUID().toString().substring(0, 6));
        cliente.setNombre("Test Sistema");
        cliente.setEmail("sistema-" + UUID.randomUUID().toString().substring(0, 6) + "@test.com");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setFechaCreacion(LocalDateTime.now());
        clienteRepository.save(cliente);
        log.info("Setup: cliente creado con id={}", cliente.getId());
    }

    /**
     * Test de persistencia: Creación y guardado de cuenta.
     * Verifica que una nueva entidad Cuenta se almacene correctamente en la BD,
     * comprobando que se genere un ID y se mantengan los valores de saldo y tipo.
     */
    @Test
    void guardarCuenta_persisteCorrectamente() {
        log.info("Test sistema: guardar cuenta");
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("ES-SYS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        cuenta.setSaldo(1000.0);
        cuenta.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuenta.setCliente(cliente);

        Cuenta guardada = cuentaRepository.save(cuenta);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getSaldo()).isEqualTo(1000.0);
        assertThat(guardada.getTipoCuenta()).isEqualTo(ETipoCuenta.CORRIENTE);
        log.info("Test sistema pasado: cuenta guardada con id={}", guardada.getId());
    }

    /**
     * Test de consulta: Recuperación de cuentas por identificador de cliente.
     * Valida que el método de consulta derivado devuelva todas las cuentas asociadas
     * a un cliente específico, verificando la integridad de la relación 1:N.
     */
    @Test
    void findByClienteId_retornaCuentasDelCliente() {
        log.info("Test sistema: findByClienteId");
        Cuenta c1 = crearCuenta(ETipoCuenta.CORRIENTE, 500.0);
        Cuenta c2 = crearCuenta(ETipoCuenta.AHORRO, 200.0);
        cuentaRepository.save(c1);
        cuentaRepository.save(c2);

        List<Cuenta> cuentas = cuentaRepository.findByClienteId(cliente.getId());

        assertThat(cuentas).hasSize(2);
        log.info("Test sistema pasado: {} cuentas encontradas", cuentas.size());
    }

    /**
     * Test de consulta: Búsqueda por número de cuenta (IBAN).
     * Comprueba que se pueda recuperar una cuenta única utilizando su número de cuenta
     * y que el resultado sea un Optional con datos.
     */
    @Test
    void findByNumeroCuenta_existente_retornaCuenta() {
        log.info("Test sistema: findByNumeroCuenta existente");
        String numero = "ES-FIND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Cuenta cuenta = crearCuenta(ETipoCuenta.AHORRO, 300.0);
        cuenta.setNumeroCuenta(numero);
        cuentaRepository.save(cuenta);

        Optional<Cuenta> resultado = cuentaRepository.findByNumeroCuenta(numero);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getSaldo()).isEqualTo(300.0);
        log.info("Test sistema pasado: cuenta encontrada por numero");
    }

    /**
     * Test de consulta (caso negativo): Búsqueda de número de cuenta inexistente.
     * Asegura que el repositorio gestione correctamente la ausencia de datos
     * devolviendo un Optional vacío en lugar de null.
     */
    @Test
    void findByNumeroCuenta_noExistente_retornaVacio() {
        log.info("Test sistema: findByNumeroCuenta no existente");
        Optional<Cuenta> resultado = cuentaRepository.findByNumeroCuenta("NO-EXISTE-JAMAS");

        assertThat(resultado).isEmpty();
        log.info("Test sistema pasado: Optional vacio correctamente");
    }

    /**
     * Test de validación: Comprobación de existencia de número de cuenta.
     * Verifica que la función existsByNumeroCuenta retorne true cuando el número 
     * ya está registrado en el sistema.
     */
    @Test
    void existsByNumeroCuenta_existente_retornaTrue() {
        log.info("Test sistema: existsByNumeroCuenta existente");
        String numero = "ES-EXIST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Cuenta cuenta = crearCuenta(ETipoCuenta.CORRIENTE, 100.0);
        cuenta.setNumeroCuenta(numero);
        cuentaRepository.save(cuenta);

        boolean existe = cuentaRepository.existsByNumeroCuenta(numero);

        assertThat(existe).isTrue();
        log.info("Test sistema pasado: existsByNumeroCuenta=true");
    }

    /**
     * Test de validación (caso negativo): Comprobación de número no registrado.
     * Asegura que el sistema informe correctamente (false) cuando un número de cuenta
     * está disponible para ser asignado.
     */
    @Test
    void existsByNumeroCuenta_noExistente_retornaFalse() {
        log.info("Test sistema: existsByNumeroCuenta no existente");
        boolean existe = cuentaRepository.existsByNumeroCuenta("NO-EXISTE-NUMERO");

        assertThat(existe).isFalse();
        log.info("Test sistema pasado: existsByNumeroCuenta=false");
    }

    /**
     * Test de actualización: Persistencia de cambios en el saldo.
     * Valida que al modificar el atributo saldo y ejecutar save(), el cambio se
     * refleje permanentemente en la base de datos tras una nueva consulta.
     */
    @Test
    void actualizarSaldo_persiste() {
        log.info("Test sistema: actualizar saldo en BD");
        Cuenta cuenta = crearCuenta(ETipoCuenta.CORRIENTE, 100.0);
        cuentaRepository.save(cuenta);

        cuenta.setSaldo(999.0);
        cuentaRepository.save(cuenta);

        Cuenta actualizada = cuentaRepository.findById(cuenta.getId()).orElseThrow();
        assertThat(actualizada.getSaldo()).isEqualTo(999.0);
        log.info("Test sistema pasado: saldo actualizado a {}", actualizada.getSaldo());
    }

    /**
     * Test de borrado: Eliminación física de una cuenta.
     * Verifica que tras ejecutar el borrado, el registro deje de ser accesible
     * mediante su identificador primario.
     */
    @Test
    void eliminarCuenta_yaNoExiste() {
        log.info("Test sistema: eliminar cuenta");
        Cuenta cuenta = crearCuenta(ETipoCuenta.AHORRO, 50.0);
        cuentaRepository.save(cuenta);
        Long id = cuenta.getId();

        cuentaRepository.delete(cuenta);

        assertThat(cuentaRepository.findById(id)).isEmpty();
        log.info("Test sistema pasado: cuenta eliminada con id={}", id);
    }

    /**
     * Método auxiliar (Helper): Creación de objetos Cuenta para pruebas.
     * Centraliza la lógica de instanciación para mantener los tests limpios,
     * asignando automáticamente el cliente de prueba y generando números de cuenta únicos.
     * @param tipo El tipo de cuenta (CORRIENTE/AHORRO).
     * @param saldo El saldo inicial de la cuenta.
     * @return Una instancia de Cuenta lista para ser persistida.
     */
    private Cuenta crearCuenta(ETipoCuenta tipo, Double saldo) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("ES-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        cuenta.setSaldo(saldo);
        cuenta.setTipoCuenta(tipo);
        cuenta.setCliente(cliente);
        return cuenta;
    }
}