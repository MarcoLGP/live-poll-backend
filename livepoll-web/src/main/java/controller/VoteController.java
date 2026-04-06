package controller;

import client.VoteServiceClient;
import dto.VoteCastDTO;
import io.quarkus.security.Authenticated;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/vote")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class VoteController {

    @RestClient
    VoteServiceClient voteServiceClient;

    @POST
    public Response castVote(@HeaderParam("Authorization") String authorization, @Valid VoteCastDTO vote) {
        return voteServiceClient.castVote(authorization, vote);
    }
}
