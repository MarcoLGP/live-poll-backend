package entities;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(name = "credentials")
public class Credential extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String email;

    @Column(nullable = false)
    public String passwordHash;

    @Column(name = "user_profile_id", nullable = false)
    public Long userProfileId;
}