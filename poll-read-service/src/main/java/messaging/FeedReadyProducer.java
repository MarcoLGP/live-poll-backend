package messaging;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class FeedReadyProducer {

    @Inject
    @Channel("feed-ready")
    Emitter<JsonObject> emitter;

    public void send(String type, Object payload) {
        emitter.send(new JsonObject()
                .put("type",    type)
                .put("payload", JsonObject.mapFrom(payload)));
    }
}