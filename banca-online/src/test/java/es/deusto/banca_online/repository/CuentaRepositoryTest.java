package es.deusto.banca_online.repository;

import es.deusto.banca_online.entity.Cliente;
import es.deusto.banca_online.entity.Cuenta;
import es.deusto.banca_online.entity.ETipoCuenta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
}) //Indicamos que como es test, no queremos que se guarde en la BD.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CuentaRepositoryTest {

    // Inyectamos el repositorio sin tener que crear uno
    @Autowired
    private ICuentaRepository cuentaRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Cliente clientePersistido;


    /*------------
        PREPARACIÓN
     ------------*/
    @BeforeEach
    void setUp() {
        // Crear un cliente de prueba antes de cada test
        Cliente cliente = new Cliente();
        cliente.setDni("12345678A");
        cliente.setNombre("Juan");
        cliente.setEmail("juan@test.com");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setFechaCreacion(LocalDateTime.now());

        clientePersistido = clienteRepository.save(cliente);
    }


    /*------------
        TESTS
     ------------*/
    @Test
    void testGuardarCuenta() {
        // 1. CREAR: Preparamos una cuenta de prueba
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("ES123456789012345678");
        cuenta.setSaldo(1000.0);
        cuenta.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuenta.setCliente(clientePersistido);
        // NOTA: La fecha de creación se establece automáticamente con @PrePersist

        // 2. GUARDAR: Ejecutamos save para probar que se guarda correctamente
        Cuenta guardada = cuentaRepository.save(cuenta);

        // 3. VERIFICAR: Comprobamos que funciona
        assertThat(guardada).isNotNull();
        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getNumeroCuenta()).isEqualTo("ES123456789012345678");
        assertThat(guardada.getSaldo()).isEqualTo(1000.0);
        assertThat(guardada.getTipoCuenta()).isEqualTo(ETipoCuenta.CORRIENTE);
        assertThat(guardada.getCliente().getId()).isEqualTo(clientePersistido.getId());
        assertThat(guardada.getFechaCreacion()).isNotNull(); // Verificamos que se autogeneró

        System.out.println("Cuenta guardada con ID: " + guardada.getId());
    }


    // Devolver todas las cuentas si existen - VÁLIDO
    @Test
    void findAll_DeberiaRetornarTodasLasCuentas() {
        // Crear y guardar varias cuentas
        Cuenta cuenta1 = new Cuenta();
        cuenta1.setNumeroCuenta("ES111111111111111111");
        cuenta1.setSaldo(1000.0);
        cuenta1.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuenta1.setCliente(clientePersistido);
        entityManager.persist(cuenta1);

        Cuenta cuenta2 = new Cuenta();
        cuenta2.setNumeroCuenta("ES222222222222222222");
        cuenta2.setSaldo(2000.0);
        cuenta2.setTipoCuenta(ETipoCuenta.AHORRO);
        cuenta2.setCliente(clientePersistido);
        entityManager.persist(cuenta2);

        Cuenta cuenta3 = new Cuenta();
        cuenta3.setNumeroCuenta("ES333333333333333333");
        cuenta3.setSaldo(3000.0);
        cuenta3.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuenta3.setCliente(clientePersistido);
        entityManager.persist(cuenta3);

        entityManager.flush();

        // Ejecutar findAll
        List<Cuenta> cuentas = cuentaRepository.findAll();

        // Verificar resultados
        assertThat(cuentas).hasSize(3); // Cuentas deberia ser una lista de 3
        // Y contener los siguientes números de cuenta:
        assertThat(cuentas).extracting(Cuenta::getNumeroCuenta)
                .containsExactlyInAnyOrder(
                        "ES111111111111111111",
                        "ES222222222222222222",
                        "ES333333333333333333"
                );
    }


    // Devolver todas las cuentas si existen - INVÁLIDO
    @Test
    void findAll_CuandoNoHayCuentas_DeberiaRetornarListaVacia() {
        // Buscar las cuentas (no debería haber ninguna porque no hemos creado)
        List<Cuenta> cuentas = cuentaRepository.findAll();

        // Debe devolver vacío
        assertThat(cuentas).isEmpty();
    }


    @Test
    void testFindByNumeroCuenta() {
        // 1. CREAR: Preparamos datos
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("ES123456789012345678");
        cuenta.setSaldo(1000.0);
        cuenta.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuenta.setCliente(clientePersistido);
        cuentaRepository.save(cuenta);

        // 2. EJECUTAR: Buscamos por número de cuenta
        Optional<Cuenta> encontrado = cuentaRepository.findByNumeroCuenta("ES123456789012345678");

        // 3. VERIFICAR
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNumeroCuenta()).isEqualTo("ES123456789012345678");
        assertThat(encontrado.get().getSaldo()).isEqualTo(1000.0);
        assertThat(encontrado.get().getTipoCuenta()).isEqualTo(ETipoCuenta.CORRIENTE);

        System.out.println("Cuenta encontrada: " + encontrado.get().getNumeroCuenta());
    }


    @Test
    void testFindByNumeroCuenta_NoExiste() {
        // EJECUTAR: Buscar un número de cuenta que no existe
        Optional<Cuenta> encontrado = cuentaRepository.findByNumeroCuenta("NOEXISTE");

        // VERIFICAR: No debe encontrar nada
        assertThat(encontrado).isEmpty();

        System.out.println("Cuenta no encontrada (correcto)");
    }


    @Test
    void testFindByClienteId() {
        // 1. CREAR: Preparamos varias cuentas para el mismo cliente
        Cuenta cuenta1 = new Cuenta();
        cuenta1.setNumeroCuenta("ES111111111111111111");
        cuenta1.setSaldo(1000.0);
        cuenta1.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuenta1.setCliente(clientePersistido);
        cuentaRepository.save(cuenta1);

        Cuenta cuenta2 = new Cuenta();
        cuenta2.setNumeroCuenta("ES222222222222222222");
        cuenta2.setSaldo(2000.0);
        cuenta2.setTipoCuenta(ETipoCuenta.AHORRO);
        cuenta2.setCliente(clientePersistido);
        cuentaRepository.save(cuenta2);

        // 2. EJECUTAR: Buscar cuentas por clienteId
        List<Cuenta> cuentas = cuentaRepository.findByClienteId(clientePersistido.getId());

        // 3. VERIFICAR
        assertThat(cuentas).hasSize(2);
        assertThat(cuentas).extracting(Cuenta::getNumeroCuenta)
                .containsExactlyInAnyOrder("ES111111111111111111", "ES222222222222222222");

        System.out.println("Cuentas encontradas para el cliente: " + cuentas.size());
    }


    @Test
    void testFindByClienteId_NoExiste() {
        // EJECUTAR: Buscar cuentas para un cliente que no tiene cuentas
        List<Cuenta> cuentas = cuentaRepository.findByClienteId(clientePersistido.getId());

        // VERIFICAR: Debe devolver lista vacía
        assertThat(cuentas).isEmpty();

        System.out.println("No hay cuentas para el cliente (correcto)");
    }


    @Test
    void testExistsByNumeroCuenta() {
        // 1. CREAR
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("ES123456789012345678");
        cuenta.setSaldo(1000.0);
        cuenta.setTipoCuenta(ETipoCuenta.CORRIENTE);
        cuenta.setCliente(clientePersistido);
        cuentaRepository.save(cuenta);

        // 2. EJECUTAR y VERIFICAR
        boolean existe = cuentaRepository.existsByNumeroCuenta("ES123456789012345678");
        boolean noExiste = cuentaRepository.existsByNumeroCuenta("NOEXISTE");

        // 3. VERIFICAR
        assertThat(existe).isTrue();
        assertThat(noExiste).isFalse();

        System.out.println("Número de cuenta existe: " + existe);
        System.out.println("Número de cuenta no existe: " + noExiste);
    }
}