package es.deusto.banca_online.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity // Especificamos que se va a hacer una tabla sobre esta clase
@Table(name = "cliente")
public class Cliente {

    /*---------------
        ATRIBUTOS
    ---------------*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20, nullable = false)
    private String dni;

    @Column(length = 100, nullable = false)
    private String nombre;

    // Utilizamos name porque en la BD es primer_apellido, no primerApellido (convencion de Java)
    @Column(name = "primer_apellido", length = 100)
    private String primerApellido;

    @Column(name = "segundo_apellido", length = 100)
    private String segundoApellido;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;



    /*--------------------
        CONSTRUCTORES
    --------------------*/

    // Vacio
    public Cliente() {
    }


    // Con campos obligatorios
    public Cliente(String dni, String nombre, String email, LocalDateTime fechaCreacion, LocalDate fechaNacimiento) {
        this.dni = dni;
        this.nombre = nombre;
        this.email = email;
        this.fechaCreacion = LocalDateTime.now();;
        this.fechaNacimiento = fechaNacimiento;
    }



    /*--------------------
       GETTERS/SETTERS
    --------------------*/
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
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
