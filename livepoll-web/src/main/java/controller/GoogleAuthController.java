package controller;

import client.AuthServiceClient;
import dto.AuthResponseDTO;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.net.URI;
import java.util.List;

@Path("/auth/social/google")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GoogleAuthController {

    private static final Logger LOG = Logger.getLogger(GoogleAuthController.class);

    @Inject
    @RestClient
    AuthServiceClient authServiceClient;

    @ConfigProperty(name = "frontend.url")
    String frontendUrl;

    @GET
    @Path("/login")
    @PermitAll
    public Response googleLogin() {
        Response authResponse = authServiceClient.googleLoginRedirect();

        Response.ResponseBuilder builder = Response.status(authResponse.getStatus())
                .location(authResponse.getLocation());

        List<String> cookies = authResponse.getStringHeaders().get("Set-Cookie");
        if (cookies != null) {
            cookies.forEach(c -> builder.header("Set-Cookie", c));
        }

        return builder.build();
    }

    @GET
    @Path("/callback")
    @PermitAll
    public Response googleCallback(@QueryParam("code") String code,
                                   @QueryParam("state") String state,
                                   @CookieParam("google_oauth_state") String storedState) {
        try {
            Response authResponse = authServiceClient.googleCallback(code, state, storedState);

            if (authResponse.getStatus() != 200) {
                LOG.errorf("Erro no auth-service: status %d", authResponse.getStatus());
                return Response.seeOther(URI.create(frontendUrl + "/login?error=auth_failed"))
                        .build();
            }

            AuthResponseDTO body = authResponse.readEntity(AuthResponseDTO.class);
            String accessToken = body.accessToken();

            URI redirectUri = URI.create(frontendUrl + "/login-success#access_token=" + accessToken);
            Response.ResponseBuilder builder = Response.seeOther(redirectUri);

            List<String> cookies = authResponse.getStringHeaders().get("Set-Cookie");
            if (cookies != null) {
                cookies.forEach(c -> builder.header("Set-Cookie", c));
            }

            return builder.build();

        } catch (Exception e) {
            LOG.error("Erro no callback do Google", e);
            return Response.seeOther(URI.create(frontendUrl + "/login?error=server_error"))
                    .build();
        }
    }
}
