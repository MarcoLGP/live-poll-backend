package entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_change_token")
public class EmailChangeToken extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long userId;
    public String newEmail;
    public String token;
    public Instant expiresAt;
    public boolean used;

    public static EmailChangeToken create(Long userId, String newEmail) {
        EmailChangeToken t = new EmailChangeToken();
        t.userId = userId;
        t.newEmail = newEmail;
        t.token = UUID.randomUUID().toString();
        t.expiresAt = Instant.now().plus(java.time.Duration.ofHours(24));
        t.used = false;
        return t;
    }
}