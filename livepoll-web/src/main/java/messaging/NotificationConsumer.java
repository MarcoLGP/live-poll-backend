package messaging;

import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import sse.NotificationEventBus;

@ApplicationScoped
public class NotificationConsumer {

    @Inject
    NotificationEventBus notificationEventBus;

    @Incoming("user-notification")
    @Blocking
    public void consume(JsonObject notification) {
        Long userId = notification.getLong("receiverUserId");
        String jsonString = notification.encode();
        notificationEventBus.emitToUser(userId, jsonString);
    }
}
