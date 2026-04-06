package controller;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestStreamElementType;
import sse.FeedEventBus;
import sse.NotificationEventBus;

import java.time.Duration;

@Path("/stream")
@ApplicationScoped
public class SseResource {

    @Inject
    FeedEventBus eventBus;

    @Inject
    NotificationEventBus notificationEventBus;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/feed")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    @Authenticated
    public Multi<String> streamFeed() {
        Long userId = Long.valueOf(jwt.getSubject());

        Multi<String> heartbeat = Multi.createFrom().ticks()
                .every(Duration.ofSeconds(25))
                .map(t -> "{\"type\":\"ping\"}");

        return Multi.createBy().merging()
                .streams(eventBus.subscribe(userId), heartbeat);
    }

    @GET
    @Path("/notifications")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    @Authenticated
    public Multi<String> streamNotifications() {
        Long userId = Long.valueOf(jwt.getSubject());

        Multi<String> heartbeat = Multi.createFrom().ticks()
                .every(Duration.ofSeconds(25))
                .map(t -> "{\"type\":\"ping\"}");

        return Multi.createBy().merging()
                .streams(notificationEventBus.subscribe(userId), heartbeat);
    }
}