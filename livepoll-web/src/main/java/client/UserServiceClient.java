package client;

import dto.UpdateProfileDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/user")
@RegisterRestClient(configKey = "user-service-api")
public interface UserServiceClient {
    @GET
    @Path("/me")
    Response me(@HeaderParam("Authorization") String authorization);

    @DELETE
    Response deleteUser(@HeaderParam("Authorization") String authorization);

    @PUT
    Response updateUser(@HeaderParam("Authorization") String authorization, @Valid UpdateProfileDTO userProfile);
}
