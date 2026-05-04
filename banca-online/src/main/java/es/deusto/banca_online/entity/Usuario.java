package es.deusto.banca_online.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

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

    // FK persistida vía este campo (compatibilidad con código existente).
    @Column(name = "cliente_id")
    private Long clienteId; // null si es ADMIN

    /** Perfil de cliente enlazado; solo lectura en JPA (la FK la escribe {@link #clienteId}). */
    @OneToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "cliente_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Cliente cliente;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
