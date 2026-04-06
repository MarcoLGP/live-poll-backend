package controller;

import client.NotificationServiceClient;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class NotificationController {

    @RestClient
    private NotificationServiceClient notificationServiceClient;

    @GET
    public Response getNotifications(@HeaderParam("Authorization") String authorization,
                                     @QueryParam("page") int page,
                                     @QueryParam("size") int size) {
        return notificationServiceClient.getNotifications(authorization, page, size);
    }

    @GET
    @Path("/unread-count")
    public Response countUnReadNotification(@HeaderParam("Authorization") String authorization) {
        return notificationServiceClient.countUnReadNotification(authorization);
    };

    @PUT
    @Path("/{id}/read")
    public Response markAsRead(@HeaderParam("Authorization") String authorization, @PathParam("id") Long notificationId) {
        return notificationServiceClient.markAsRead(authorization, notificationId);
    };

    @PUT
    @Path("/read-all")
    public Response markAllReadNotification(@HeaderParam("Authorization") String authorization) {
        return notificationServiceClient.markAllReadNotification(authorization);
    };
}
