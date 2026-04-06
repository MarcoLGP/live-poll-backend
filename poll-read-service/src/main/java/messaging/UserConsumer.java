package messaging;

import dto.UserProfileEventDTO;
import entities.PollRead;
import entities.PollVotesRead;
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
        PollRead.update(
                "authorName = ?1, authorGradient = null, authorAvatarUrl = null, authorId = -1 where authorId = ?2",
                "user removed", userId
        );

        PollVotesRead.update("userId = -1 where userId = ?1", userId);
    }

    @Incoming("user-updated")
    @Transactional
    public void consumeUpdated(JsonObject event) {
        UserProfileEventDTO user = event.mapTo(UserProfileEventDTO.class);

        PollRead.update(
                "authorName = ?1, authorGradient = ?2, authorAvatarUrl = ?3 where authorId = ?4",
                user.username(), user.gradientAvatar(), user.avatarUrl(), user.userId()
        );
    }
}
