package client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/notifications")
@RegisterRestClient(configKey = "notification-service-api")
public interface NotificationServiceClient {

    @GET
    Response getNotifications(@HeaderParam("Authorization") String authorization,
                              @QueryParam("page") int page,
                              @QueryParam("size") int size);

    @GET
    @Path("/unread-count")
    Response countUnReadNotification(@HeaderParam("Authorization") String authorization);

    @PUT
    @Path("/{id}/read")
    Response markAsRead(@HeaderParam("Authorization") String authorization, @PathParam("id") Long notificationId);

    @PUT
    @Path("/read-all")
    Response markAllReadNotification(@HeaderParam("Authorization") String authorization);
}
