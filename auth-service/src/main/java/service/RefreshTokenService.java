package service;

import entities.Credential;
import entities.RefreshToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@ApplicationScoped
public class RefreshTokenService {

    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    @Transactional
    public RefreshToken createRefreshToken(Credential credential) {
        RefreshToken.update("revoked = true where credential = ?1 and revoked = false", credential);

        String token = UUID.randomUUID().toString();
        String tokenHash = hashToken(token);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.credential = credential;
        refreshToken.tokenHash = tokenHash;
        refreshToken.expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);
        refreshToken.revoked = false;
        refreshToken.persist();

        refreshToken.originalToken = token;
        return refreshToken;
    }

    @Transactional
    public RefreshToken verifyAndRotate(String token) {
        String tokenHash = hashToken(token);
        RefreshToken oldToken = RefreshToken.find(
                "select rt from RefreshToken rt join fetch rt.credential where rt.tokenHash = ?1 and rt.revoked = false and rt.expiresAt > ?2",
                tokenHash, LocalDateTime.now()
        ).firstResult();

        if (oldToken == null) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String newToken = UUID.randomUUID().toString();
        String newTokenHash = hashToken(newToken);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.credential = oldToken.credential;
        newRefreshToken.tokenHash = newTokenHash;
        newRefreshToken.expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);
        newRefreshToken.revoked = false;
        newRefreshToken.persist();

        oldToken.revoked = true;
        oldToken.persist();

        newRefreshToken.originalToken = newToken;
        return newRefreshToken;
    }

    @Transactional
    public void revokeToken(String token) {
        String tokenHash = hashToken(token);
        RefreshToken.update("revoked = true where tokenHash = ?1", tokenHash);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}