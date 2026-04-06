package controller;

import dto.UpdateProfileDTO;
import entities.UserProfile;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import service.UserService;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class UserController {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserService userService;

    @GET
    @Path("/me")
    public Response me() {
        var userId = Long.parseLong(jwt.getSubject());
        var userProfile = userService.getUserProfile(userId);
        return Response.ok(userProfile).build();
    }

    @PUT
    public Response putUser(@Valid UpdateProfileDTO userProfile) {
        var userId = Long.parseLong(jwt.getSubject());
        var userUpdated = userService.updateUserProfile(userId, userProfile);
        return Response.ok(userUpdated).build();
    }

    @DELETE
    public Response deleteUser() {
        Long userId = Long.parseLong(jwt.getSubject());
        userService.deleteUserProfile(userId);
        return Response.noContent().build();
    }
}
