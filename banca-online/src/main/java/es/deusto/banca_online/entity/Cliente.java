package es.deusto.banca_online.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entidad que contiene la información personal y de contacto de un cliente.
 */
@Data
@Entity // Especificamos que se va a hacer una tabla sobre esta clase
@Table(name = "cliente")
public class Cliente {

    /*---------------
        ATRIBUTOS
    ---------------*/
    /** Identificador del cliente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** DNI o identificador fiscal único del cliente. */
    @Column(unique = true, length = 20, nullable = false)
    private String dni;

    /** Nombre de pila del cliente. */
    @Column(length = 100, nullable = false)
    private String nombre;

    // Utilizamos name porque en la BD es primer_apellido, no primerApellido (convencion de Java)
    /** Primer apellido del cliente. */
    @Column(name = "primer_apellido", length = 100)
    private String primerApellido;

    /** Segundo apellido del cliente. */
    @Column(name = "segundo_apellido", length = 100)
    private String segundoApellido;

    /** Fecha de nacimiento del cliente. */
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    /** Email del cliente - derivado de {@link Usuario#email}, se mantiene para compatibilidad BD. */
    @Column(nullable = true, length = 100)
    private String email;

    /** Teléfono del cliente. */
    @Column(length = 20)
    private String telefono;

    /** Dirección del cliente. */
    @Column(length = 200)
    private String direccion;

    /** Fecha de creación del cliente. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;



    /*--------------------
        CONSTRUCTORES
    --------------------*/

    // Vacio
    /** Constructor vacio para crear un cliente */
    public Cliente() {
    }


    /**
     * Constructor para crear un cliente con sus datos básicos obligatorios.
     * @param dni Documento nacional de identidad.
     * @param nombre Nombre completo.
     * @param email Dirección de correo.
     * @param fechaCreacion Fecha de alta en el sistema.
     * @param fechaNacimiento Fecha de nacimiento para validación de edad.
     */
    public Cliente(String dni, String nombre, String email, LocalDateTime fechaCreacion, LocalDate fechaNacimiento) {
        this.dni = dni;
        this.nombre = nombre;
        this.email = email;
        this.fechaCreacion = LocalDateTime.now();;
        this.fechaNacimiento = fechaNacimiento;
    }

    /*--------------------
          TOSTRING
    --------------------*/
    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", dni='" + dni + '\'' +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
