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

@Path("/auth/social/github")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GithubAuthController {

    private static final Logger LOG = Logger.getLogger(GithubAuthController.class);

    @Inject
    @RestClient
    AuthServiceClient authServiceClient;

    @ConfigProperty(name = "frontend.url")
    String frontendUrl;

    @GET
    @Path("/login")
    @PermitAll
    public Response githubLogin() {
        Response authResponse = authServiceClient.githubLoginRedirect();

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
    public Response githubCallback(@QueryParam("code") String code,
                                   @QueryParam("state") String state,
                                   @CookieParam("github_oauth_state") String storedState) {
        try {
            if (storedState == null || storedState.isBlank()) {
                LOG.error("Cookie github_oauth_state não encontrado");
                return Response.seeOther(URI.create(frontendUrl + "/login?error=missing_state")).build();
            }
            Response authResponse = authServiceClient.githubCallback(code, state, storedState);

            if (authResponse.getStatus() != 200) {
                LOG.errorf("Erro no auth-service: status %d", authResponse.getStatus());
                return Response.seeOther(URI.create(frontendUrl + "/login?error=auth_failed"))
                        .build();
            }

            // Extrai o access token do corpo
            AuthResponseDTO body = authResponse.readEntity(AuthResponseDTO.class);
            String accessToken = body.accessToken();

            // Redireciona para o frontend com token no fragmento
            URI redirectUri = URI.create(frontendUrl + "/login-success#access_token=" + accessToken);
            Response.ResponseBuilder builder = Response.seeOther(redirectUri);

            // Repassa os cookies (refresh token e delete state) vindos do auth-service
            List<String> cookies = authResponse.getStringHeaders().get("Set-Cookie");
            if (cookies != null) {
                cookies.forEach(c -> builder.header("Set-Cookie", c));
            }

            return builder.build();

        } catch (Exception e) {
            LOG.error("Erro no callback do GitHub", e);
            return Response.seeOther(URI.create(frontendUrl + "/login?error=server_error"))
                    .build();
        }
    }
}