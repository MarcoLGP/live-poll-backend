package messaging;

import dto.UserRegistrationRequestDTO;
import dto.UserRegistrationResponseDTO;
import entities.UserProfile;
import io.quarkus.hibernate.orm.panache.Panache;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class UserRegistrationConsumer {

    @Incoming("user-registration")
    @Outgoing("registration-response")
    @Transactional
    public UserRegistrationResponseDTO process(JsonObject json) {
        UserRegistrationRequestDTO request = json.mapTo(UserRegistrationRequestDTO.class);
        boolean exists = UserProfile.find("email = ?1 or username = ?2", request.email(), request.username()).count() > 0;
        if (exists) {
            return new UserRegistrationResponseDTO(
                    request.correlationId(),
                    null,
                    false,
                    "Email ou username já está em uso"
            );
        }

        // Criar perfil
        UserProfile profile = new UserProfile();
        profile.email = request.email();
        profile.username = request.username();

        // Persistir (o método é transacional por padrão com @Incoming se houver gerenciador de transações)
        Panache.getEntityManager().persist(profile);

        return new UserRegistrationResponseDTO(
                request.correlationId(),
                profile.id,
                true,
                null
        );
    }
}