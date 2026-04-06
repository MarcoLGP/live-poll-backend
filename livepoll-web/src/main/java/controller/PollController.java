package controller;

import client.PollServiceClient;
import dto.PollCreateDTO;
import dto.PollUpdateDTO;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/poll")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class PollController {

    @Inject
    @RestClient
    private PollServiceClient pollServiceClient;

    @POST
    public Response postPoll(@HeaderParam("Authorization") String authorization, @Valid PollCreateDTO pollCreateDTO) {
        return pollServiceClient.postPoll(authorization, pollCreateDTO);
    }

    @PUT
    @Path("/{id}")
    public Response updatePoll(@HeaderParam("Authorization") String authorization, @PathParam("id") Long pollId, PollUpdateDTO pollUpdateDTO) {
        return pollServiceClient.updatePoll(authorization, pollId, pollUpdateDTO);
    }

    @DELETE
    @Path("/{id}")
    public Response deletePoll(@HeaderParam("Authorization") String authorization, @PathParam("id") Long pollId) {
        return  pollServiceClient.deletePoll(authorization, pollId);
    }
}
