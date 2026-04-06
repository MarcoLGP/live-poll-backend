package entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@RegisterForReflection
public class UserProfile extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, nullable = false)
    public String username;

    @Column
    public String email;

    @Column(nullable = false)
    public String language;

    @Column(name = "gradient_avatar")
    public String gradientAvatar;

    @Column(name = "avatar_url")
    public String avatarUrl;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
