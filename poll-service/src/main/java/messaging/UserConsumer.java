package messaging;

import entities.Poll;
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

        Poll.update("authorId = -1 where authorId = ?1", userId);
    }
}