package controller;

import client.AuthServiceClient;
import dto.*;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class AuthController {

    @Inject
    @RestClient
    private AuthServiceClient authServiceClient;

    @POST
    @Path("/register")
    public Response register(@Valid RegisterDTO dto) {
        if (dto.socialLogin() == null || dto.socialLogin().isEmpty()) {
            return authServiceClient.register(
                    new RegisterDTO(dto.username(), dto.email(), dto.password(), dto.language(), "linear-gradient(135deg, #7C6FF7, #A78BFA)", dto.socialLogin(), dto.avatarUrl()));
        }
        return authServiceClient.register(dto);
    }

    @POST
    @Path("/account/confirm")
    public Response confirmAccount(AccountConfirmDTO dto) {
        return proxyWithCookies(authServiceClient.confirmAccount(dto));
    }

    @POST
    @Path("/password/reset/request")
    public Response requestPasswordReset(PasswordResetRequestDTO dto) {
        return authServiceClient.requestPasswordReset(dto);
    }

    @POST
    @Path("/password/reset/confirm")
    public Response confirmPasswordReset(PasswordResetConfirmDTO dto) {
        return authServiceClient.confirmPasswordReset(dto);
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginDTO dto) {
        return proxyWithCookies(authServiceClient.login(dto));
    }

    @POST
    @Path("/logout")
    public Response logout(@CookieParam("refreshToken") String refreshTokenCookie) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Refresh token cookie is required")
                    .build();
        }
        return proxyWithCookies(authServiceClient.logout(refreshTokenCookie));
    }

    @PUT
    @Path("password")
    public Response changePassword(ChangePasswordDTO dto) {
        return authServiceClient.changePassword(dto);
    }

    @POST
    @Path("email/request-change")
    public Response requestEmailChange(EmailChangeRequestDTO dto) {
        return authServiceClient.requestEmailChange(dto);
    }

    @POST
    @Path("email/confirm")
    public Response confirmEmailChange(EmailConfirmDTO dto) {
        return authServiceClient.confirmEmailChange(dto);
    }

    @POST
    @Path("/refresh-token")
    public Response refresh(@CookieParam("refreshToken") String refreshTokenCookie) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            System.err.println("[REFRESH] Cookie ausente no BFF — browser não enviou ou Cloudflare bloqueou");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Refresh token cookie is required")
                    .build();
        }

        RestResponse<Object> upstream = authServiceClient.refreshToken(refreshTokenCookie);

        if (upstream.getStatus() != 200) {
            System.err.printf("[REFRESH] auth-service rejeitou o token: status=%d body=%s%n",
                    upstream.getStatus(), upstream.getEntity());
        }

        Response response = proxyWithCookies(upstream);
        List<String> setCookieHeaders = response.getStringHeaders().get("Set-Cookie");

        return response;
    }

    private Response proxyWithCookies(RestResponse<Object> upstream) {
        Response.ResponseBuilder builder = Response
                .status(upstream.getStatus())
                .entity(upstream.getEntity());

        List<String> rawCookies = upstream.getStringHeaders().get("Set-Cookie");
        if (rawCookies != null) {
            rawCookies.forEach(c -> builder.header("Set-Cookie", c));
        }

        return builder.build();
    }
}