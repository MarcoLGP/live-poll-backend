package controller;

import dto.PagedResult;
import dto.PollReadFeedDTO;
import dto.PollReadSearchResultDTO;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import service.PollReadService;

import java.util.List;

@Path("/poll-read")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class PollReadController {

    @Inject
    PollReadService pollReadService;

    @Inject
    JsonWebToken jwt;

    @GET
    public Response getPollReadFeed(
            @QueryParam("page") @DefaultValue("0")      int page,
            @QueryParam("size") @DefaultValue("25")     int size,
            @QueryParam("sort") @DefaultValue("recent") String sort
    ) {
        PagedResult<PollReadFeedDTO> result = pollReadService.getPollReadFeed(page, size, sort, getCurrentUserId());
        return Response.ok(result).build();
    }

    @GET
    @Path("/search")
    public Response search(
            @QueryParam("q")    @DefaultValue("") String query,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        List<PollReadSearchResultDTO> result = pollReadService.search(query, size);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    public Response getPollById(@PathParam("id") Long id) {
        PollReadFeedDTO result = pollReadService.findById(id, getCurrentUserId());
        return Response.ok(result).build();
    }

    @GET
    @Path("/my-polls")
    public Response getMyPolls(
            @QueryParam("page") @DefaultValue("0")  int page,
            @QueryParam("size") @DefaultValue("25") int size
    ) {
        PagedResult<PollReadFeedDTO> result = pollReadService.getMyPolls(page, size, getCurrentUserId());
        return Response.ok(result).build();
    }

    @GET
    @Path("/my-votes")
    public Response getMyVotes(
            @QueryParam("page") @DefaultValue("0")  int page,
            @QueryParam("size") @DefaultValue("25") int size
    ) {
        PagedResult<PollReadFeedDTO> result = pollReadService.getMyVotes(page, size, getCurrentUserId());
        return Response.ok(result).build();
    }

    private Long getCurrentUserId() {
        return Long.valueOf(jwt.getSubject());
    }
}