package client;

import dto.PollUpdateDTO;
import dto.PollCreateDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/poll")
@RegisterRestClient(configKey = "poll-service-api")
public interface PollServiceClient {

    @POST
    Response postPoll(@HeaderParam("Authorization") String authorization, @Valid PollCreateDTO pollCreateDTO);

    @DELETE
    @Path("/{id}")
    Response deletePoll(@HeaderParam("Authorization") String authorization, @PathParam("id") Long pollId);

    @PUT
    @Path("/{id}")
    Response updatePoll(@HeaderParam("Authorization") String authorization, @PathParam("id") Long pollId, PollUpdateDTO pollUpdateDTO);
}
