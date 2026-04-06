package sse;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@ApplicationScoped
public class NotificationEventBus {

    private final Map<String, MultiEmitter<String>> emitters = new ConcurrentHashMap<>();

    public Multi<String> subscribe(Long userId) {
        return Multi.createFrom().emitter(emitter -> {
            String key = userId + ":" + UUID.randomUUID();
            emitters.put(key, (MultiEmitter<String>) emitter);
            emitter.onTermination(() -> emitters.remove(key));
        });
    }

    public void emitToUser(Long userId, String json) {
        emitters.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(userId + ":")) {
                try {
                    entry.getValue().emit(json);
                    return false;
                } catch (Exception e) {
                    return true;
                }
            }
            return false;
        });
    }
}
