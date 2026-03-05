package messaging;

import dto.UserRegistrationRequestDTO;
import dto.UserRegistrationResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
public class RegistrationMessaging {

    private static final Logger log = LoggerFactory.getLogger(RegistrationMessaging.class);
    @Inject
    @Channel("user-registration")
    Emitter<UserRegistrationRequestDTO> registrationEmitter;

    private final Map<String, CompletableFuture<UserRegistrationResponseDTO>> pendingRequests = new ConcurrentHashMap<>();

    @Incoming("registration-response")
    public void processResponse(UserRegistrationResponseDTO response) {
        CompletableFuture<UserRegistrationResponseDTO> future = pendingRequests.remove(response.correlationId());
        if (future != null) {
            future.complete(response);
        }
    }

    public UserRegistrationResponseDTO sendAndWait(String email, String username, long timeoutMillis) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<UserRegistrationResponseDTO> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        registrationEmitter.send(new UserRegistrationRequestDTO(correlationId, email, username));

        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("e: ", e);
            pendingRequests.remove(correlationId);
            return new UserRegistrationResponseDTO(correlationId, null, false, "Timeout ou erro na comunicação com user-service");
        }
    }
}
