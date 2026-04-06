package client;

import dto.VoteCastDTO;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/vote")
@RegisterRestClient(configKey = "vote-service-api")
public interface VoteServiceClient {

    @POST
    Response castVote(@HeaderParam("Authorization") String authorization, VoteCastDTO vote);
}
