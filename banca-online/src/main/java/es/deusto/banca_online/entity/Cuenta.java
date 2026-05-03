package es.deusto.banca_online.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cuenta")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, unique = true)
    private String numeroCuenta;

    // (para garantizar que el saldo no es negativo)
    @Min(value = 0, message = "El saldo de la cuenta no puede ser menor a cero")
    @Column(nullable = false)
    private Double saldo;

    // Para eliminar cuentas inactivas
    @Column(nullable = false)
    private Boolean activa = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false)
    private ETipoCuenta tipoCuenta;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    //Constructores
    public Cuenta() {}

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