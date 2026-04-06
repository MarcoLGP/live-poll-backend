package messaging;

import dto.UserRegistrationRequestDTO;
import dto.UserRegistrationResponseDTO;
import entities.Credential;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class UserConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserConsumer.class);
    private final Map<String, CompletableFuture<UserRegistrationResponseDTO>> pendingFutures = new ConcurrentHashMap<>();

    @Inject
    @Channel("user-registration")
    private Emitter<UserRegistrationRequestDTO> registrationEmitter;

    public UserRegistrationResponseDTO sendAndWait(String username, String language, String gradientAvatar, String avatarUrl, long timeoutMillis) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<UserRegistrationResponseDTO> future = new CompletableFuture<>();

        pendingFutures.put(correlationId, future);

        try {
            registrationEmitter.send(new UserRegistrationRequestDTO(correlationId, username, language, gradientAvatar, avatarUrl));

            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Erro ao aguardar resposta para correlationId: {}", correlationId, e);
            pendingFutures.remove(correlationId);
            return new UserRegistrationResponseDTO(correlationId, null, false, "Timeout ou erro na comunicação");
        }
    }

    @Incoming("user-deleted")
    @Transactional
    public void consume(JsonObject event) {
        Long userId = event.getLong("userId");
        Credential.delete("userProfileId = ?1", userId);
    }

    @Incoming("registration-response")
    public void consumeResponse(JsonObject json) {
        UserRegistrationResponseDTO response = json.mapTo(UserRegistrationResponseDTO.class);
        String correlationId = response.correlationId();
        CompletableFuture<UserRegistrationResponseDTO> future = pendingFutures.remove(correlationId);

        if (future != null) {
            future.complete(response);
        } else {
            log.warn("Resposta recebida para correlationId {} sem future pendente (possível timeout ou duplicada)", correlationId);
        }
    }

    @PreDestroy
    public void cleanup() {
        pendingFutures.values().forEach(f -> f.completeExceptionally(new RuntimeException("Aplicação sendo encerrada")));
        pendingFutures.clear();
    }
}