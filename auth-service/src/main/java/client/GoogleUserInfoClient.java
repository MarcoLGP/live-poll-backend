package client;

import dto.GoogleUserInfo;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "google.userinfo")
public interface GoogleUserInfoClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    GoogleUserInfo getUserInfo(@HeaderParam("Authorization") String authorization);
}
