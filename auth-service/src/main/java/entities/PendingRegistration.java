package entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pending_registration")
public class PendingRegistration extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(nullable = false)
    public String username;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    public String language;

    @Column(name = "gradient_avatar")
    public String gradientAvatar;

    @Column(nullable = false)
    public String token;

    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;

    public static PendingRegistration create(String email, String username,
                                             String passwordHash, String language,
                                             String gradientAvatar) {
        PendingRegistration p = new PendingRegistration();
        p.email         = email;
        p.username      = username;
        p.passwordHash  = passwordHash;
        p.language      = language;
        p.gradientAvatar = gradientAvatar;
        p.token         = UUID.randomUUID().toString();
        p.expiresAt     = Instant.now().plus(Duration.ofHours(24));
        return p;
    }
}
