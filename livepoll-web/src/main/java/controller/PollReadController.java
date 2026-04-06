package controller;

import client.PollReadServiceClient;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/poll-read")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class PollReadController {

    @RestClient
    private PollReadServiceClient pollReadServiceClient;

    @GET
    public Response getPollRead(@HeaderParam("Authorization") String authorization,
                                @QueryParam("page")   @DefaultValue("0")      int    page,
                                @QueryParam("size")   @DefaultValue("25")     int    size,
                                @QueryParam("sort")   @DefaultValue("recent") String sort) {
        return pollReadServiceClient.getPollRead(authorization, page, size, sort);
    }

    @GET
    @Path("/search")
    public Response search(@HeaderParam("Authorization") String authorization,
            @QueryParam("q")    @DefaultValue("") String query,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        return pollReadServiceClient.search(authorization, query, size);
    }

    @GET
    @Path("/{id}")
    public Response getPollById(@HeaderParam("Authorization") String authorization, @PathParam("id") Long id) {
        return pollReadServiceClient.getPollById(authorization, id);
    }

    @GET
    @Path("/my-polls")
    public Response getMyPolls(@HeaderParam("Authorization") String authorization,
                               @QueryParam("page") @DefaultValue("0")  int page,
                               @QueryParam("size") @DefaultValue("25") int size) {
        return pollReadServiceClient.getMyPolls(authorization, page, size);
    }

    @GET
    @Path("/my-votes")
    public Response getMyVotes(@HeaderParam("Authorization") String authorization,
                               @QueryParam("page") @DefaultValue("0")  int page,
                               @QueryParam("size") @DefaultValue("25") int size) {
        return pollReadServiceClient.getMyVotes(authorization, page, size);
    }
}
