package client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/poll-read")
@RegisterRestClient(configKey = "poll-read-service-api")
public interface PollReadServiceClient {
    @GET
    Response getPollRead(@HeaderParam("Authorization") String authorization,
                         @QueryParam("page")   @DefaultValue("0")      int    page,
                         @QueryParam("size")   @DefaultValue("25")     int    size,
                         @QueryParam("sort")   @DefaultValue("recent") String sort);

    @GET
    @Path("/search")
    Response search(@HeaderParam("Authorization") String authorization,
                    @QueryParam("q")    @DefaultValue("") String query,
                    @QueryParam("size") @DefaultValue("10") int size
    );

    @GET
    @Path("/{id}")
    Response getPollById(@HeaderParam("Authorization") String authorization, @PathParam("id") Long id);

    @GET
    @Path("/my-polls")
    Response getMyPolls(@HeaderParam("Authorization") String authorization,
                        @QueryParam("page") @DefaultValue("0")  int page,
                        @QueryParam("size") @DefaultValue("25") int size);

    @GET
    @Path("/my-votes")
    Response getMyVotes(@HeaderParam("Authorization") String authorization,
                        @QueryParam("page") @DefaultValue("0")  int page,
                        @QueryParam("size") @DefaultValue("25") int size);
}
