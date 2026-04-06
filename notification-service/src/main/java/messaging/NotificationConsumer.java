package messaging;

import dto.*;
import entities.Notification;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import service.NotificationService;
import java.util.Objects;

@ApplicationScoped
public class NotificationConsumer {

    @Inject
    NotificationService notificationService;

    @Inject
    @Channel("user-notification")
    Emitter<JsonObject> notificationEmitter;

    @Incoming("poll-vote-cast")
    @Transactional
    @Blocking
    public void consumePollVoteCast(JsonObject json) {
        PollVoteCastEventDTO event = json.mapTo(PollVoteCastEventDTO.class);

        if (Objects.equals(event.userId(), event.authorId())) {
            return;
        }

        Notification notif = new Notification();
        notif.receiverUserId = event.authorId();
        notif.actorUserId = event.userId();
        notif.actorUsername = event.userName();
        notif.type = "VOTE_RECEIVED";
        notif.referenceId = event.pollId();
        notif.referenceTitle = event.pollTitle();
        notif.subReferenceTitle = event.optionTitle();
        notif.persist();

        JsonObject out = JsonObject.mapFrom(NotificationDTO.fromEntity(notif));
        notificationEmitter.send(out);
    }

    @Incoming("poll-closed-notification")
    @Transactional
    @Blocking
    public void consumePollClosed(JsonObject json) {
        PollClosedNotificationEventDTO event = json.mapTo(PollClosedNotificationEventDTO.class);

        for (Long voterId : event.voterIds()) {
            Notification notif = new Notification();
            notif.receiverUserId = voterId;
            notif.actorUserId    = event.authorId();
            notif.actorUsername  = "";
            notif.type           = "POLL_CLOSED";
            notif.referenceId    = event.pollId();
            notif.referenceTitle = event.pollTitle();
            notif.persist();

            JsonObject out = JsonObject.mapFrom(NotificationDTO.fromEntity(notif));
            notificationEmitter.send(out);
        }
    }

    @Incoming("poll-milestone")
    @Transactional
    @Blocking
    public void consumePollMilestone(JsonObject json) {
        MilestoneEventDTO event = json.mapTo(MilestoneEventDTO.class);

        Notification notif = new Notification();
        notif.receiverUserId = event.authorId();
        notif.actorUserId = event.authorId();
        notif.actorUsername = event.authorUsername();
        notif.type = "POLL_MILESTONE";
        notif.referenceId = event.pollId();
        notif.referenceTitle = event.pollTitle() + " " + event.totalVotes();
        notif.persist();

        JsonObject out = JsonObject.mapFrom(NotificationDTO.fromEntity(notif));
        notificationEmitter.send(out);
    }
}