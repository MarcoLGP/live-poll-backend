package controller;

import dto.NotificationDTO;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import service.NotificationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class NotificationController {

    @Inject
    JsonWebToken jwt;

    @Inject
    NotificationService notificationService;

    private Long getCurrentUserId() {
        return Long.parseLong(jwt.getSubject());
    }

    @GET
    public Response getNotifications(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("25") int size) {
        Long userId = getCurrentUserId();
        List<NotificationDTO> notifications = notificationService.listNotifications(userId, page, size);
        long total = notificationService.countNotifications(userId);
        long unread = notificationService.countUnread(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("content", notifications);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        response.put("unread", unread);
        response.put("hasNext", (long) (page + 1) * size < total);

        return Response.ok(response).build();
    }

    @PUT
    @Path("/{id}/read")
    public Response markAsRead(@PathParam("id") Long id) {
        Long userId = getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return Response.noContent().build();
    }

    @PUT
    @Path("/read-all")
    public Response markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Response.noContent().build();
    }

    @GET
    @Path("/unread-count")
    public Response getUnreadCount() {
        Long userId = getCurrentUserId();
        long count = notificationService.countUnread(userId);
        return Response.ok(Map.of("count", count)).build();
    }
}