package client;

import dto.GitHubEmail;
import dto.GitHubUser;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "github.api")
@Path("/")
public interface GitHubApiClient {

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    GitHubUser getUser(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/user/emails")
    @Produces(MediaType.APPLICATION_JSON)
    GitHubEmail[] getUserEmails(@HeaderParam("Authorization") String authorization);
}
