package service;

import dto.UpdateProfileDTO;
import dto.UserProfileEventDTO;
import dto.UserRegistrationRequestDTO;
import entities.UserProfile;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class UserService {

    @Inject
    @Channel("user-updated")
    Emitter<UserProfileEventDTO> profileUpdatedEmitter;

    @Inject
    @Channel("user-deleted")
    Emitter<JsonObject> userDeletedEmitter;

    public UserProfile getUserProfile(Long id) {
        UserProfile userProfile = UserProfile.findById(id);
        if (userProfile == null) {
            throw new NotFoundException("UserProfile not found");
        }
        return userProfile;
    }

    @Transactional
    public UserProfile createUserProfile(UserRegistrationRequestDTO request) {
        UserProfile profile = new UserProfile();
        profile.username = request.username();
        profile.language = request.language();
        profile.gradientAvatar = request.gradientAvatar();
        profile.avatarUrl = request.avatarUrl();
        profile.email = request.email();

        profile.persist();

        return profile;
    }

    @Transactional
    public void deleteUserProfile(Long id) {
        UserProfile userProfile = UserProfile.findById(id);
        if (userProfile == null) {
            throw new NotFoundException("UserProfile not found");
        }
        userProfile.delete();

        JsonObject event = JsonObject.of("userId", id);
        userDeletedEmitter.send(event);
    }

    @Transactional
    public UserProfile updateUserProfile(Long id, UpdateProfileDTO dto) {
        UserProfile existing = UserProfile.findById(id);
        if (existing == null) {
            throw new NotFoundException("Perfil não encontrado");
        }

        if (!existing.username.equals(dto.username())) {
            UserProfile another = UserProfile.find("username = ?1", dto.username()).firstResult();
            if (another != null && !another.id.equals(id)) {
                throw new WebApplicationException("Username já está em uso", Response.Status.CONFLICT);
            }
        }

        existing.username = dto.username();
        existing.language = dto.language();
        existing.gradientAvatar = dto.gradientAvatar();
        existing.avatarUrl = dto.avatarUrl();

        UserProfileEventDTO event = new UserProfileEventDTO(
                existing.id,
                existing.username,
                existing.language,
                existing.gradientAvatar,
                existing.avatarUrl
        );
        profileUpdatedEmitter.send(event);

        return existing;
    }
}
