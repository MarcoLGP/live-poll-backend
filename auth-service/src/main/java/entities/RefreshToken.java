package entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(name = "refresh_token")
public class RefreshToken extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "credential_id", nullable = false)
    public Credential credential;

    @Column(unique = true, nullable = false)
    public String tokenHash;

    public LocalDateTime expiresAt;

    public boolean revoked;

    @Transient
    public String originalToken;
}