package entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "credential_id", nullable = false)
    public Long credentialId;

    @Column(nullable = false)
    public String token;

    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;

    public boolean used;

    public static PasswordResetToken create(Long credentialId) {
        PasswordResetToken t = new PasswordResetToken();
        t.credentialId = credentialId;
        t.token        = UUID.randomUUID().toString();
        t.expiresAt    = Instant.now().plus(Duration.ofHours(1));
        t.used         = false;
        return t;
    }
}
