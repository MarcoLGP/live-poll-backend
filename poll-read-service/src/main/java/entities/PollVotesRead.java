package entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "poll_votes_read",
        uniqueConstraints = @UniqueConstraint(columnNames = {"option_id", "user_id"}))
public class PollVotesRead extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "option_id", nullable = false)
    @JsonIgnore
    public PollOptionRead option;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;
}
