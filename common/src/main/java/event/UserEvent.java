package event;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class UserEvent {
    public String eventType;
    public Long userId;
    public String username;
    public String email;
    public String passwordHash;

    public UserEvent() {}

    public UserEvent(String eventType, Long userId, String username, String email, String passwordHash) {
        this.eventType = eventType;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}