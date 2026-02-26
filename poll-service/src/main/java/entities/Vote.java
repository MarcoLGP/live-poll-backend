package entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "votes",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "poll_id"},
                name = "uk_user_poll"
        )
)
@RegisterForReflection
public class Vote extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    public Poll poll;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    public PollOption option;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    // Construtores
    public Vote() {}

    public Vote(User user, Poll poll, PollOption option) {
        this.user = user;
        this.poll = poll;
        this.option = option;
    }
}