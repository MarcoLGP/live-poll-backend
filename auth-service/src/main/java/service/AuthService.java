package service;

import dto.*;
import entities.*;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import messaging.UserConsumer;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    @Inject RefreshTokenService refreshTokenService;
    @Inject UserConsumer userConsumer;
    @Inject EmailService emailService;

    @Transactional
    public void register(RegisterDTO dto) {
        if (Credential.find("email", dto.email()).count() > 0) {
            throw new BadRequestException("Email já cadastrado");
        }
        if (PendingRegistration.find("email", dto.email()).count() > 0) {
            PendingRegistration.delete("email", dto.email());
        }

        String passwordHash = BCrypt.hashpw(dto.password(), BCrypt.gensalt());
        PendingRegistration pending = PendingRegistration.create(
                dto.email(), dto.username(), passwordHash,
                dto.language(), dto.gradientAvatar());
        pending.persist();

        emailService.sendConfirmationEmail(dto.email(), dto.username(), pending.token);
    }

    @Transactional
    public AuthResponseDTO confirmRegistration(String token) {
        PendingRegistration pending = PendingRegistration.find("token", token).firstResult();

        if (pending == null || pending.expiresAt.isBefore(Instant.now())) {
            throw new BadRequestException("Token inválido ou expirado");
        }
        if (Credential.find("email", pending.email).count() > 0) {
            throw new BadRequestException("Conta já confirmada");
        }

        UserRegistrationResponseDTO registrationResponse = userConsumer.sendAndWait(
                pending.username, pending.language, pending.gradientAvatar, null, 5000);

        if (!registrationResponse.success()) {
            throw new BadRequestException(registrationResponse.errorMessage());
        }

        Credential credential = new Credential();
        credential.email        = pending.email;
        credential.passwordHash = pending.passwordHash;
        credential.userProfileId = registrationResponse.userId();
        credential.persist();

        pending.delete();

        return generateAuthTokens(credential);
    }

    @Transactional
    public AuthResponseDTO login(LoginDTO dto) {
        if (PendingRegistration.find("email", dto.email()).count() > 0) {
            throw new WebApplicationException("Conta ainda não confirmada. Verifique seu e-mail.", 403);
        }

        Credential credential = Credential.find("email", dto.email()).firstResult();
        if (credential == null) {
            throw new NotAuthorizedException("Credenciais inválidas", "Bearer");
        }
        if (credential.socialLogin != null) {
            throw new BadRequestException("Usuário com login social deve autenticar via provedor");
        }
        if (!BCrypt.checkpw(dto.password(), credential.passwordHash)) {
            throw new NotAuthorizedException("Credenciais inválidas", "Bearer");
        }

        return generateAuthTokens(credential);
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequestDTO dto) {
        Credential credential = Credential.find("email", dto.email()).firstResult();
        if (credential == null) {
            return;
        }
        if (credential.socialLogin != null) {
            throw new BadRequestException("Usuário com login social não pode redefinir senha");
        }

        PasswordResetToken.delete("credentialId = ?1 and used = false", credential.id);

        PasswordResetToken resetToken = PasswordResetToken.create(credential.id);
        resetToken.persist();

        String username = credential.email.split("@")[0];
        emailService.sendPasswordResetEmail(credential.email, username, resetToken.token);
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmDTO dto) {
        PasswordResetToken resetToken = PasswordResetToken
                .find("token = ?1 and used = false", dto.token()).firstResult();

        if (resetToken == null || resetToken.expiresAt.isBefore(Instant.now())) {
            throw new BadRequestException("Token inválido ou expirado");
        }

        Credential credential = Credential.findById(resetToken.credentialId);
        if (credential == null) {
            throw new NotFoundException("Credencial não encontrada");
        }

        credential.passwordHash = BCrypt.hashpw(dto.newPassword(), BCrypt.gensalt());
        credential.persist();

        resetToken.used = true;
        resetToken.persist();

        RefreshToken.update("revoked = true where credential = ?1 and revoked = false", credential);

        emailService.sendPasswordChangedEmail(credential.email, credential.email.split("@")[0], credential.email);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        Credential credential = Credential.find("userProfileId", userId).firstResult();
        if (credential == null) throw new NotFoundException("Credencial não encontrada");
        if (credential.socialLogin != null) throw new BadRequestException("Usuário com login social não pode alterar senha");
        if (!BCrypt.checkpw(dto.currentPassword(), credential.passwordHash)) throw new BadRequestException("Senha atual incorreta");

        credential.passwordHash = BCrypt.hashpw(dto.newPassword(), BCrypt.gensalt());
        credential.persist();

        emailService.sendPasswordChangedEmail(credential.email, credential.email.split("@")[0], credential.email);
    }

    @Transactional
    public AuthResponseDTO refresh(String refreshTokenCookie) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank())
            throw new WebApplicationException("Refresh token missing", 401);

        try {
            RefreshToken newRefreshToken = refreshTokenService.verifyAndRotate(refreshTokenCookie);
            String newAccessToken = generateAccessToken(newRefreshToken.credential.userProfileId.toString());
            return new AuthResponseDTO(newAccessToken, newRefreshToken.originalToken);
        } catch (Exception e) {
            System.err.printf("[AUTH-SERVICE REFRESH] Falha ao rotacionar token: %s — %s%n",
                    e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    public void logout(String refreshTokenCookie) {
        if (refreshTokenCookie != null) refreshTokenService.revokeToken(refreshTokenCookie);
    }

    public String generateAccessToken(String subject) {
        return Jwt.issuer("livepoll-auth")
                .subject(subject)
                .groups(Set.of("user"))
                .expiresIn(Duration.ofMinutes(15))
                .sign();
    }

    private AuthResponseDTO generateAuthTokens(Credential credential) {
        String accessToken = generateAccessToken(credential.userProfileId.toString());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(credential);
        return new AuthResponseDTO(accessToken, refreshToken.originalToken);
    }
}