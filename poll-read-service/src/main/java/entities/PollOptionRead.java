package entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "poll_option_read")
public class PollOptionRead extends PanacheEntityBase {
    @Id
    public Long id; // mesmo ID da opção no poll-service

    @Column(nullable = false)
    public String text;

    @Column(name = "display_order", nullable = false)
    public int displayOrder;

    @Column(nullable = false)
    public int totalVotes = 0;

    @Column(nullable = false)
    public int percentage = 0;

    @ManyToOne
    @JoinColumn(name = "poll_id", nullable = false)
    @JsonIgnore
    public PollRead poll;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PollVotesRead> votes;
}