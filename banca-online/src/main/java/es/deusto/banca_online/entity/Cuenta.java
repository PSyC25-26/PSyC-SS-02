package es.deusto.banca_online.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numeroCuenta;

    @Column(nullable = false)
    private Double saldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ETipoCuenta tipoCuenta;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    //Constructores
    public Cuenta() {}

    public Cuenta(String numeroCuenta, Double saldo, ETipoCuenta tipoCuenta, Cliente cliente) {
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
        this.tipoCuenta = tipoCuenta;
        this.cliente = cliente;
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    //Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }

    public Double getSaldo() { return saldo; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }

    public ETipoCuenta getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(ETipoCuenta tipoCuenta) { this.tipoCuenta = tipoCuenta; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}