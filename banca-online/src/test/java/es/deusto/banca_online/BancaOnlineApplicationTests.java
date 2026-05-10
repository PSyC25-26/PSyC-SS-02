package es.deusto.banca_online;

/**
 * @file BancaOnlineApplicationTests.java
 * @brief Test de arranque que verifica que el contexto de Spring Boot carga correctamente.
 * @author Equipo Banca Online - Universidad de Deusto
 * @version 0.3.0-SNAPSHOT
 */

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BancaOnlineApplicationTests {

	/**
     * Prueba de integridad del contexto de la aplicación.
     * <p>
     * Este test verifica que la configuración de Spring Boot, las dependencias y 
     * el contexto de la aplicación se carguen correctamente sin errores. 
     * Es la primera línea de defensa para detectar fallos en la configuración de beans.
     */
	@Test
	void contextLoads() {
	}

}
