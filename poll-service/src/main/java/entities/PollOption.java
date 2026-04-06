package entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.*;

@Entity
@Table(name = "poll_option")
@RegisterForReflection
public class PollOption extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String text;

    @Column(name = "display_order")
    public Integer displayOrder;

    @ManyToOne
    @JoinColumn(name = "poll_id", nullable = false)
    public Poll poll;
}