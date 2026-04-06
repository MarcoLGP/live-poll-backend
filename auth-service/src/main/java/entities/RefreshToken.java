package entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
public class RefreshToken extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "credential_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Credential credential;

    @Column(unique = true, nullable = false)
    public String tokenHash;

    public LocalDateTime expiresAt;

    public boolean revoked;

    @Transient
    public String originalToken;
}