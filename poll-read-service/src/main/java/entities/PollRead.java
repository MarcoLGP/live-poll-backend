package entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "poll_read")
public class PollRead extends PanacheEntityBase {
    @Id
    public Long id; // mesmo ID do poll-service

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String category;

    @Column(name = "author_id", nullable = false)
    public Long authorId;

    @Column(name = "author_name", nullable = false)
    public String authorName;

    @Column(name = "author_gradient")
    public String authorGradient; // identificador do gradiente – nulo se usar avatarUrl

    @Column(name = "author_avatar_url")
    public String authorAvatarUrl; // URL da foto do login social – nulo se usar gradiente

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "end_date")
    public LocalDateTime endDate; // data de encerramento

    @Column(nullable = false)
    public boolean active; // se a enquete está ativa

    @Column(name = "total_votes", nullable = false)
    public int totalVotes = 0;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PollOptionRead> options;
}