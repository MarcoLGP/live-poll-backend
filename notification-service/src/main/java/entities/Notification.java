package entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "receiver_user_id", nullable = false)  // quem recebe a notificação
    public Long receiverUserId;

    @Column(name = "reference_title")
    public String referenceTitle;

    @Column(name = "sub_reference_title")
    public String subReferenceTitle;

    @Column(name = "actor_user_id", nullable = false)     // quem praticou a ação
    public Long actorUserId;

    @Column(name = "actor_username", nullable = false)    // username de quem praticou a ação
    public String actorUsername;

    @Column(name = "actor_avatar_url")
    public String actorAvatarUrl;

    @Column(name = "actor_avatar_gradient")
    public String actorAvatarGradient;

    @Column(nullable = false)
    public String type;

    @Column(name = "reference_id")
    public Long referenceId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(nullable = false)
    public boolean read = false;
}