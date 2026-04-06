package controller;

import dto.VoteCastDTO;
import dto.VoteEventDTO;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.VoteService;

@Path("/vote")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class VoteController {

    @Inject
    VoteService voteService;

    @POST
    public Response postVote(@Valid VoteCastDTO vote) {
        VoteEventDTO event = voteService.handleVote(vote.userId(), vote.userName(), vote.pollId(), vote.optionId(), vote.authorId());
        return Response.ok(event).build();
    }
}
