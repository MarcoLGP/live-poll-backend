package client;

import dto.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/auth")
@RegisterRestClient(configKey = "auth-service-api")
public interface AuthServiceClient {
    @POST
    @Path("/register")
    Response register(@Valid RegisterDTO dto);

    @POST
    @Path("/account/confirm")
    RestResponse<Object> confirmAccount(AccountConfirmDTO dto);

    @POST
    @Path("/password/reset/request")
    Response requestPasswordReset(PasswordResetRequestDTO dto);

    @POST
    @Path("/password/reset/confirm")
    Response confirmPasswordReset(PasswordResetConfirmDTO dto);

    @POST
    @Path("/login")
    RestResponse<Object> login(@Valid LoginDTO dto);

    @POST
    @Path("/logout")
    RestResponse<Object> logout(@CookieParam("refreshToken") String refreshTokenCookie);

    @POST
    @Path("/refresh-token")
    RestResponse<Object> refreshToken(@CookieParam("refreshToken") String refreshTokenCookie);

    @PUT
    @Path("password")
    Response changePassword(ChangePasswordDTO dto);

    @POST
    @Path("/mail/request-change")
    Response requestEmailChange(EmailChangeRequestDTO dto);

    @POST
    @Path("/email/confirm")
    Response confirmEmailChange(EmailConfirmDTO dto);

    @GET
    @Path("/github")
    Response githubLoginRedirect();

    @GET
    @Path("/github/callback")
    @Produces(MediaType.APPLICATION_JSON)
    Response githubCallback(@QueryParam("code") String code,
                            @QueryParam("state") String state,
                            @CookieParam("github_oauth_state") String storedState);

    @GET
    @Path("/google")
    Response googleLoginRedirect();

    @GET
    @Path("/google/callback")
    @Produces(MediaType.APPLICATION_JSON)
    Response googleCallback(@QueryParam("code") String code,
                            @QueryParam("state") String state,
                            @CookieParam("google_oauth_state") String storedState);
}