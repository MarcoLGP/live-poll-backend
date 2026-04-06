package service;

import client.GitHubApiClient;
import client.GoogleUserInfoClient;
import dto.*;
import entities.Credential;
import entities.RefreshToken;
import io.quarkus.oidc.client.NamedOidcClient;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.Tokens;
import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AuthSocialService {

    @Inject
    @NamedOidcClient("github")
    OidcClient githubOidcClient;

    @Inject
    @RestClient
    GitHubApiClient gitHubApiClient;

    @Inject
    @NamedOidcClient("google")
    OidcClient googleOidcClient;

    @Inject
    @RestClient
    GoogleUserInfoClient googleUserInfoClient;

    @Inject
    AuthService authService;

    @Inject
    RefreshTokenService refreshTokenService;

    @Inject
    UserRegistrationService userRegistrationService;

    @ConfigProperty(name = "quarkus.oidc-client.github.redirect-uri")
    String githubRedirectUri;

    @ConfigProperty(name = "quarkus.oidc-client.google.redirect-uri")
    String googleRedirectUri;

    @ConfigProperty(name = "quarkus.oidc-client.github.client-id")
    String githubClientId;

    @ConfigProperty(name = "quarkus.oidc-client.google.client-id")
    String googleClientId;

    // ========== Callbacks OAuth ==========

    @Transactional
    public AuthResponseDTO processGithubCallback(String code, String state, String storedState) {
        validateState(state, storedState);

        Tokens tokens = githubOidcClient.getTokens(Map.of(
                "code", code,
                "redirect_uri", githubRedirectUri
        )).await().indefinitely();

        String accessToken = tokens.getAccessToken();
        GitHubUser githubUser = gitHubApiClient.getUser("Bearer " + accessToken);
        String email = extractGitHubEmail(githubUser, accessToken);

        Credential credential = Credential.find("email = ?1 and socialLogin = 'github'", email).firstResult();

        if (credential == null) {
            credential = registerSocialUser(
                    email,
                    "github",
                    githubUser.login(),
                    githubUser.avatarUrl(),
                    githubUser.name()
            );
        }

        return generateAuthResponse(credential);
    }

    @Transactional
    public AuthResponseDTO processGoogleCallback(String code, String state, String storedState) {
        validateState(state, storedState);

        Tokens tokens = googleOidcClient.getTokens(Map.of(
                "code", code,
                "redirect_uri", googleRedirectUri
        )).await().indefinitely();

        GoogleUserInfo userInfo = googleUserInfoClient.getUserInfo("Bearer " + tokens.getAccessToken());

        String email = userInfo.email();
        if (email == null || email.isBlank() || !userInfo.emailVerified()) {
            throw new WebApplicationException("Email não verificado ou não disponível", 400);
        }

        Credential credential = Credential.find("email = ?1 and socialLogin = 'google'", email).firstResult();

        if (credential == null) {
            String baseName = userInfo.givenName() != null ? userInfo.givenName() : userInfo.name();
            credential = registerSocialUser(
                    email,
                    "google",
                    baseName,
                    userInfo.picture(),
                    userInfo.name()
            );
        }

        return generateAuthResponse(credential);
    }

    // ========== Construção da URI de autorização ==========

    /**
     * Retorna a URI de autorização do GitHub e o header Set-Cookie do state
     * como String RFC 6265 pura — sem NewCookie.Builder para evitar o
     * prefixo "Version=1;" que o RESTEasy Reactive injeta e que quebra
     * o Cloudflare Tunnel e browsers modernos.
     *
     * ATENÇÃO: AuthorizationResponseDTO deve ter stateCookieHeader(): String
     * (não mais stateCookie(): NewCookie).
     */
    public AuthorizationResponseDTO buildGithubAuthorizationUri() {
        String state = generateRandomState();

        String authorizationUri = UriBuilder.fromUri("https://github.com/login/oauth/authorize")
                .queryParam("client_id", githubClientId)
                .queryParam("redirect_uri", githubRedirectUri)
                .queryParam("scope", "read:user user:email")
                .queryParam("state", state)
                .build()
                .toString();

        return new AuthorizationResponseDTO(authorizationUri, buildStateCookieHeader("github_oauth_state", state));
    }

    public AuthorizationResponseDTO buildGoogleAuthorizationUri() {
        String state = generateRandomState();

        String authorizationUri = UriBuilder.fromUri("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", googleRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile email")
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .build()
                .toString();

        return new AuthorizationResponseDTO(authorizationUri, buildStateCookieHeader("google_oauth_state", state));
    }

    // ========== Utilitários privados ==========

    /**
     * Gera o header Set-Cookie para o state CSRF do OAuth como string RFC 6265,
     * sem usar jakarta.ws.rs.core.NewCookie (que adiciona Version=1).
     */
    private String buildStateCookieHeader(String cookieName, String value) {
        if (LaunchMode.current().isProduction()) {
            // Cross-origin em produção via Cloudflare: obriga Secure + SameSite=None
            return cookieName + "=" + value + "; Path=/; HttpOnly; Secure; SameSite=None";
        } else {
            // Dev/Test: localhost, sem Secure
            return cookieName + "=" + value + "; Path=/; HttpOnly; SameSite=Lax";
        }
    }

    private String generateRandomState() {
        SecureRandom sr = new SecureRandom();
        byte[] bytes = new byte[32];
        sr.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Credential registerSocialUser(String email, String provider, String baseName,
                                          String avatarUrl, String fullName) {
        List<String> candidates = userRegistrationService.generateUsernameCandidates(baseName, email);
        UserRegistrationResponseDTO response = userRegistrationService.registerWithCandidates(
                candidates, avatarUrl, "pt-BR"
        );

        Credential credential = new Credential();
        credential.email = email;
        credential.socialLogin = provider;
        credential.userProfileId = response.userId();
        credential.persist();

        return credential;
    }

    private AuthResponseDTO generateAuthResponse(Credential credential) {
        String accessToken = authService.generateAccessToken(credential.userProfileId.toString());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(credential);
        return new AuthResponseDTO(accessToken, refreshToken.originalToken);
    }

    private String extractGitHubEmail(GitHubUser githubUser, String accessToken) {
        String email = githubUser.email();
        if (email != null && !email.isBlank()) {
            return email;
        }

        GitHubEmail[] emails = gitHubApiClient.getUserEmails("Bearer " + accessToken);
        for (GitHubEmail e : emails) {
            if (e.primary() && e.verified()) {
                return e.email();
            }
        }
        throw new WebApplicationException("Não foi possível obter e-mail do GitHub", 400);
    }

    private void validateState(String state, String storedState) {
        if (state == null || !state.equals(storedState)) {
            throw new WebApplicationException("Invalid state parameter", 401);
        }
    }
}