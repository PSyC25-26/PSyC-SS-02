package es.deusto.banca_online.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa una cuenta bancaria en el sistema.
 * Almacena el saldo, el número de cuenta y la relación con el cliente propietario.
 */
@Data
@Entity
@Table(name = "cuenta")
public class Cuenta {

    /** Identificador único autoincremental de la cuenta. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código único de identificación de la cuenta bancaria. */
    @Column(name = "numero_cuenta", nullable = false, unique = true)
    private String numeroCuenta;

    /** Saldo actual de la cuenta. No puede ser inferior a cero. */
    @Min(value = 0, message = "El saldo de la cuenta no puede ser menor a cero")
    @Column(nullable = false)
    private Double saldo;

    /** Estado de la cuenta (true si está operativa, false si está cerrada/inactiva). */
    @Column(nullable = false)
    private Boolean activa = true;

    /** Tipo de cuenta (AHORRO o CORRIENTE). */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false)
    private ETipoCuenta tipoCuenta;

    /** Cliente titular de la cuenta. */
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /** Fecha de creación de la cuenta. */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    //Constructores
    /** Constructor vacio para crear una nueva instancia de Cuenta. */
    public Cuenta() {}

    /**
     * Constructor parametrizado para crear una nueva instancia de Cuenta.
     * Inicializa los datos básicos y establece por defecto el estado de la cuenta como activa.
     * * @param numeroCuenta Código identificador único de la cuenta (IBAN o similar).
     * @param saldo Monto de dinero inicial con el que se apertura la cuenta.
     * @param tipoCuenta Categoría de la cuenta (EJ. AHORRO, CORRIENTE).
     * @param cliente Referencia al objeto Cliente que es titular de la cuenta.
     */
    public Cuenta(String numeroCuenta, Double saldo, ETipoCuenta tipoCuenta, Cliente cliente) {
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
        this.tipoCuenta = tipoCuenta;
        this.cliente = cliente;
        this.activa = true;
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}