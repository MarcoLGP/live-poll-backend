package service;

import dto.AuthResponseDTO;
import dto.LoginDTO;
import dto.RegisterDTO;
import entities.User;
import entities.RefreshToken;
import event.UserEvent;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.mindrot.jbcrypt.BCrypt;
import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    @Inject
    RefreshTokenService refreshTokenService;

    @Inject
    @Channel("user-events")
    Emitter<UserEvent> userEventEmitter;

    @Transactional
    public AuthResponseDTO register(RegisterDTO dto) {
        // Verificar se usuário já existe
        if (User.find("username = ?1 or email = ?2", dto.username(), dto.email()).count() > 0) {
            throw new BadRequestException("Username or email already exists");
        }

        User user = new User();
        user.username = dto.username();
        user.email = dto.email();
        user.password = BCrypt.hashpw(dto.password(), BCrypt.gensalt());
        user.persist();

        // Gerar tokens
        String accessToken = generateAccessToken(user.id.toString());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDTO(accessToken, refreshToken.originalToken);
    }

    @Transactional
    public AuthResponseDTO login(LoginDTO dto) {
        User user = User.find("username = ?1 or email = ?1", dto.usernameOrEmail()).firstResult();
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        if (!BCrypt.checkpw(dto.password(), user.password)) {
            throw new BadRequestException("Invalid password");
        }

        String accessToken = generateAccessToken(user.id.toString());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDTO(accessToken, refreshToken.originalToken);
    }

    public String generateAccessToken(String userId) {
        return Jwt.issuer("livepoll-auth")
                .subject(userId)
                .groups(Set.of("user"))
                .expiresIn(Duration.ofMinutes(15))
                .sign();
    }
}