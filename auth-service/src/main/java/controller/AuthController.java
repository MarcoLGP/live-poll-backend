package controller;

import dto.*;
import io.quarkus.runtime.LaunchMode;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import service.AuthService;
import service.AuthSocialService;

import java.net.URI;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class AuthController {

    @Inject
    AuthService authService;

    @Inject
    AuthSocialService authSocialService;

    // ========== Autenticação tradicional ==========

    @POST
    @Path("/register")
    public Response register(@Valid RegisterDTO dto) {
        authService.register(dto);
        return Response.accepted()
                .entity(Map.of("message", "Verifique seu e-mail para confirmar o cadastro"))
                .build();
    }

    @POST
    @Path("/account/confirm")
    public Response confirmAccount(@Valid AccountConfirmDTO dto) {
        AuthResponseDTO response = authService.confirmRegistration(dto.token());
        return buildTokenResponse(response);
    }

    @POST
    @Path("/password/reset/request")
    public Response requestPasswordReset(@Valid PasswordResetRequestDTO dto) {
        authService.requestPasswordReset(dto);
        return Response.ok(Map.of("message", "Se o e-mail existir, você receberá as instruções")).build();
    }

    @POST
    @Path("/password/reset/confirm")
    public Response confirmPasswordReset(@Valid PasswordResetConfirmDTO dto) {
        authService.confirmPasswordReset(dto);
        return Response.ok(Map.of("message", "Senha redefinida com sucesso")).build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginDTO dto) {
        AuthResponseDTO response = authService.login(dto);
        return buildTokenResponse(response);
    }

    @POST
    @Path("/refresh-token")
    public Response refresh(@CookieParam("refreshToken") String refreshTokenCookie) {
        AuthResponseDTO response = authService.refresh(refreshTokenCookie);
        return buildTokenResponse(response);
    }

    @POST  // ← estava faltando
    @Path("/logout")
    public Response logout(@CookieParam("refreshToken") String refreshTokenCookie) {
        authService.logout(refreshTokenCookie);
        return Response.ok()
                .header("Set-Cookie", buildRefreshCookieHeader("") + "; Max-Age=0")
                .build();
    }

    // ========== Login social (GitHub) ==========

    @GET
    @Path("/github")
    public Response githubLogin() {
        AuthorizationResponseDTO authResp = authSocialService.buildGithubAuthorizationUri();
        // stateCookieHeader já é string RFC 6265 — sem Version=1
        return Response.seeOther(URI.create(authResp.authorizationUri()))
                .header("Set-Cookie", authResp.stateCookieHeader())
                .build();
    }

    @GET
    @Path("/github/callback")
    public Response githubCallback(@QueryParam("code") String code,
                                   @QueryParam("state") String state,
                                   @CookieParam("github_oauth_state") String storedState) {
        AuthResponseDTO authResponse = authSocialService.processGithubCallback(code, state, storedState);

        return Response.ok(Map.of("accessToken", authResponse.accessToken()))
                // refreshToken como header bruto — sem Version=1
                .header("Set-Cookie", buildRefreshCookieHeader(authResponse.refreshToken()))
                // limpa o state cookie
                .header("Set-Cookie", buildDeleteStateCookie("github_oauth_state"))
                .build();
    }

    // ========== Login social (Google) ==========

    @GET
    @Path("/google")
    public Response googleLogin() {
        AuthorizationResponseDTO authResp = authSocialService.buildGoogleAuthorizationUri();
        return Response.seeOther(URI.create(authResp.authorizationUri()))
                .header("Set-Cookie", authResp.stateCookieHeader())
                .build();
    }

    @GET
    @Path("/google/callback")
    public Response googleCallback(@QueryParam("code") String code,
                                   @QueryParam("state") String state,
                                   @CookieParam("google_oauth_state") String storedState) {
        AuthResponseDTO authResponse = authSocialService.processGoogleCallback(code, state, storedState);

        return Response.ok(Map.of("accessToken", authResponse.accessToken()))
                .header("Set-Cookie", buildRefreshCookieHeader(authResponse.refreshToken()))
                .header("Set-Cookie", buildDeleteStateCookie("google_oauth_state"))
                .build();
    }

    // ========== Utilitários ==========

    private Response buildTokenResponse(AuthResponseDTO authResponse) {
        return Response.ok(new AccessTokenResponseDTO(authResponse.accessToken()))
                .header("Set-Cookie", buildRefreshCookieHeader(authResponse.refreshToken()))
                .build();
    }

    /**
     * Gera o valor do header Set-Cookie para o refreshToken sem usar NewCookie,
     * evitando o prefixo "Version=1;" que o RESTEasy Reactive injeta e que
     * quebra o Cloudflare Tunnel / browsers modernos (RFC 6265).
     */
    private String buildRefreshCookieHeader(String value) {
        if (!LaunchMode.current().isDevOrTest()) {
            // Produção: Secure + SameSite=None (cross-origin via Cloudflare)
            return "refreshToken=" + value + "; Path=/; Max-Age=604800; HttpOnly; Secure; SameSite=None";
        } else {
            // Dev/Test: sem Secure, SameSite=Lax (localhost)
            return "refreshToken=" + value + "; Path=/; Max-Age=604800; HttpOnly; SameSite=Lax";
        }
    }

    /**
     * Gera header para apagar um cookie de state OAuth (Max-Age=0).
     */
    private String buildDeleteStateCookie(String cookieName) {
        if (!LaunchMode.current().isDevOrTest()) {
            return cookieName + "=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None";
        } else {
            return cookieName + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax";
        }
    }
}