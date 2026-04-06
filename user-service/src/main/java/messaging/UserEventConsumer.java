package messaging;

import dto.UserRegistrationRequestDTO;
import dto.UserRegistrationResponseDTO;
import entities.UserProfile;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import service.UserService;

@ApplicationScoped
public class UserEventConsumer {

    @Inject
    private UserService userService;

    @Incoming("user-registration")
    @Outgoing("registration-response")
    @Transactional
    @Blocking
    public UserRegistrationResponseDTO processUser(JsonObject json) {
        UserRegistrationRequestDTO request = json.mapTo(UserRegistrationRequestDTO.class);
        boolean exists = UserProfile.find("username = ?1", request.username()).count() > 0;
        if (exists) {
            return new UserRegistrationResponseDTO(
                    request.correlationId(),
                    null,
                    false,
                    "Username já está em uso"
            );
        }

        try {
            var profile = userService.createUserProfile(request);

            return new UserRegistrationResponseDTO(
                    request.correlationId(),
                    profile.id,
                    true,
                    null
            );
        } catch (Exception e) {
            return null;
        }
    }
}