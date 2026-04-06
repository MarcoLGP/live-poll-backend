package entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "credentials")
public class Credential extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(name = "password")
    public String passwordHash;

    @Column(name = "social_login")
    public String socialLogin;

    @Column(name = "user_profile_id", nullable = false)
    public Long userProfileId;
}