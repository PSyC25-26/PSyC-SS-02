package es.deusto.banca_online;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Banca Online.
 * Punto de entrada de Spring Boot que inicia el contexto de la aplicación,
 * activa la configuración automática y el escaneo de componentes.
 */
@SpringBootApplication
public class BancaOnlineApplication {

	/**
     * Método de inicio del sistema.
     * @param args Argumentos de línea de comandos.
     */
	public static void main(String[] args) {
		SpringApplication.run(BancaOnlineApplication.class, args);
	}

}
