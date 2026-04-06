package sse;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class FeedEventBus {

    private final Map<String, MultiEmitter<String>> emitters = new ConcurrentHashMap<>();

    public Multi<String> subscribe(Long userId) {
        return Multi.createFrom().emitter(emitter -> {
            String key = userId + ":" + UUID.randomUUID();
            emitters.put(key, (MultiEmitter<String>) emitter);
            emitter.onTermination(() -> emitters.remove(key));
        });
    }

    public void broadcast(String json) {
        emitters.values().removeIf(emitter -> {
            try {
                emitter.emit(json);
                return false;
            } catch (Exception e) {
                return true;
            }
        });
    }
}