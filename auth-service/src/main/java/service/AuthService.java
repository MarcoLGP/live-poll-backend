package service;

import dto.AuthResponseDTO;
import dto.LoginDTO;
import dto.RegisterDTO;
import dto.UserRegistrationResponseDTO;
import entities.Credential;
import entities.RefreshToken;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import messaging.RegistrationMessaging;
import org.mindrot.jbcrypt.BCrypt;
import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    @Inject
    private RefreshTokenService refreshTokenService;

    @Inject
    private RegistrationMessaging messagingService;

    @Transactional
    public AuthResponseDTO register(RegisterDTO dto) {
        if (Credential.find("email", dto.email()).count() > 0) {
            throw new BadRequestException("Email já cadastrado");
        }

        UserRegistrationResponseDTO registrationResponse = messagingService.sendAndWait(
                dto.email(),
                dto.username(),
                5000
        );

        if (!registrationResponse.success()) {
            throw new BadRequestException(registrationResponse.errorMessage());
        }

        Credential credential = new Credential();
        credential.email = dto.email();
        credential.passwordHash = BCrypt.hashpw(dto.password(), BCrypt.gensalt());
        credential.userProfileId = registrationResponse.userId();
        credential.persist();

        String accessToken = generateAccessToken(credential.userProfileId.toString());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(credential);

        return new AuthResponseDTO(accessToken, refreshToken.originalToken);
    }

    @Transactional
    public AuthResponseDTO login(LoginDTO dto) {
        Credential credential = Credential.find("email", dto.usernameOrEmail()).firstResult();
        if (credential == null) {
            throw new NotFoundException("Usuário não encontrado");
        }

        if (!BCrypt.checkpw(dto.password(), credential.passwordHash)) {
            throw new BadRequestException("Senha inválida");
        }

        String accessToken = generateAccessToken(credential.userProfileId.toString());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(credential);

        return new AuthResponseDTO(accessToken, refreshToken.originalToken);
    }

    public String generateAccessToken(String subject) {
        return Jwt.issuer("livepoll-auth")
                .subject(subject)
                .groups(Set.of("user"))
                .expiresIn(Duration.ofMinutes(15))
                .sign();
    }
}