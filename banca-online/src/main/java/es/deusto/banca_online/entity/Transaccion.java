package es.deusto.banca_online.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transaccion")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ETipoTransaccion tipo;

    @Column(length = 200)
    private String descripcion;

    // Añadimos @Positive para asegurar que el movimiento siempre es mayor a 0
    @Positive(message = "El importe de la transacción debe ser estrictamente mayor a cero")
    @Column(nullable = false)
    private Double total;

    // Relación con la cuenta de origen (puede ser nula en depósitos)
    @ManyToOne
    @JoinColumn(name = "cuenta_origen_id")
    private Cuenta cuentaOrigen;

    // Relación con la cuenta de destino (puede ser nula en retiros)
    @ManyToOne
    @JoinColumn(name = "cuenta_destino_id")
    private Cuenta cuentaDestino;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }
}