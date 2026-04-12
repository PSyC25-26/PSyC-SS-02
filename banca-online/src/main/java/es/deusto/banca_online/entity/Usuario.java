package es.deusto.banca_online.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ERol rol;

    @Column(nullable = false)
    private Boolean activo = true;

    // Sin @OneToOne — solo guardamos el ID para meterlo en el JWT
    @Column(name = "cliente_id")
    private Long clienteId; // null si es ADMIN
}
