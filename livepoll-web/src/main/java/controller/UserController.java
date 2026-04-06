package controller;

import client.UserServiceClient;
import dto.UpdateProfileDTO;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class UserController {

    @Inject
    @RestClient
    private UserServiceClient userServiceClient;

    @GET
    @Path("/me")
    public Response getUserMe(@HeaderParam("Authorization") String authorization) {
        return userServiceClient.me(authorization);
    }

    @DELETE
    public Response deleteUser(@HeaderParam("Authorization") String authorization) {
        return userServiceClient.deleteUser(authorization);
    }

    @PUT
    public Response updateUser(@HeaderParam("Authorization") String authorization, @Valid UpdateProfileDTO userProfile) {
        return userServiceClient.updateUser(authorization, userProfile);
    }
}
