package entities;

import dto.PollOptionCreateDTO;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "poll")
@RegisterForReflection
public class Poll extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public Boolean active;

    @Column(name = "author_id")
    public Long authorId;

    @Column(nullable = false)
    public String category;

    @Column(name = "end_date")
    public LocalDateTime endDate;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PollOption> options = new ArrayList<>();

    public void addOptionsFromDTOs(List<PollOptionCreateDTO> optionDTOs) {
        if (optionDTOs == null) return;
        for (PollOptionCreateDTO dto : optionDTOs) {
            PollOption option = new PollOption();
            option.text = dto.text();
            option.displayOrder = dto.displayOrder();
            option.poll = this;
            this.options.add(option);
        }
    }

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