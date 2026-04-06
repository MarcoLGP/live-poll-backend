package messaging;

import dto.UserProfileEventDTO;
import entities.Notification;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class UserConsumer {

    @Incoming("user-deleted")
    @Transactional
    public void consumeDeleted(JsonObject event) {
        Long userId = event.getLong("userId");
        Notification.update("actorUsername = ?1 where actorUserId = ?2", "user removed", userId);
    }

    @Incoming("user-updated")
    @Transactional
    public void consumeUpdated(JsonObject event) {
        UserProfileEventDTO userProfile = event.mapTo(UserProfileEventDTO.class);
        Notification.update("actorUsername = ?1 where actorUserId = ?2", userProfile.username(), userProfile.userId());
    }

}
