package messaging;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import sse.FeedEventBus;

@ApplicationScoped
public class FeedReadyConsumer {

    @Inject
    FeedEventBus eventBus;

    @Incoming("feed-ready")
    public void onFeedReady(JsonObject json) {
        eventBus.broadcast(json.encode());
    }
}
