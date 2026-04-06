package messaging;

import dto.PollClosedEventDTO;
import dto.PollClosedNotificationEventDTO;
import entities.Vote;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class VoteClosedConsumer {

    @Inject
    @Channel("poll-closed-notification")
    Emitter<PollClosedNotificationEventDTO> notificationEmitter;

    @Incoming("poll-closed")
    @Transactional
    @Blocking
    public void consume(JsonObject json) {
        PollClosedEventDTO event = json.mapTo(PollClosedEventDTO.class);

        List<Long> voterIds = Vote.find("pollId = ?1", event.pollId())
                .list()
                .stream()
                .map(v -> ((Vote) v).userId)
                .distinct()
                .filter(id -> !id.equals(event.authorId()))
                .collect(Collectors.toList());

        if (voterIds.isEmpty()) return;

        notificationEmitter.send(new PollClosedNotificationEventDTO(
                event.pollId(), event.pollTitle(), event.authorId(), voterIds));
    }
}
