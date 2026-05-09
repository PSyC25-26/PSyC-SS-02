package es.deusto.banca_online.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entidad de seguridad que gestiona las credenciales de acceso al sistema.
 * Implementa UserDetails para la integración con Spring Security.
 */
@Data
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

    /** Identificador del usuario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Email utilizado como login del usuario. */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** Contraseña cifrada del usuario. */
    @Column(nullable = false, length = 255)
    private String password;

    /** Rol asignado (ADMIN o CLIENTE) para el control de acceso. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ERol rol;

    
    @Column(nullable = false)
    private Boolean activo = true;

    /** Perfil de cliente enlazado; la FK se gestiona via esta relación JPA. */
    @OneToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "cliente_id", referencedColumnName = "id")
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
