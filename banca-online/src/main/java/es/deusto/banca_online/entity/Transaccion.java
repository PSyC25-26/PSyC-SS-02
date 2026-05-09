package es.deusto.banca_online.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Representa un movimiento de dinero en el sistema.
 * Registra el tipo de operación, el importe y las cuentas involucradas.
 */
@Data
@Entity
@Table(name = "transaccion")
public class Transaccion {

    /** Identificador de la transacción */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tipo de operación realizada (DEPOSITO, RETIRO o TRANSFERENCIA). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ETipoTransaccion tipo;

    /** Descripción de la operación realizada. */
    @Column(length = 200)
    private String descripcion;

    /** Monto total de la operación. Debe ser un valor positivo. */
    // Añadimos @Positive para asegurar que el movimiento siempre es mayor a 0
    @Positive(message = "El importe de la transacción debe ser estrictamente mayor a cero")
    @Column(nullable = false)
    private Double total;

    /** Cuenta que emite el dinero (nulo en depósitos). */
    // Relación con la cuenta de origen (puede ser nula en depósitos)
    @ManyToOne
    @JoinColumn(name = "cuenta_origen_id")
    private Cuenta cuentaOrigen;

    /** Cuenta que recibe el dinero (nulo en retiros). */
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