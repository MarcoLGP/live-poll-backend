package messaging;

import dto.OptionRemovedEventDTO;
import dto.PollCreatedEventDTO;
import dto.PollDeletedEventDTO;
import dto.PollUpdatedEventDTO;
import entities.PollOptionRead;
import entities.PollRead;
import io.quarkus.hibernate.orm.panache.Panache;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class PollEventConsumer {

    @Inject
    FeedReadyProducer feedReadyProducer;

    @Incoming("poll-created")
    @Transactional @Blocking
    public void consumePollCreated(JsonObject json) {
        PollCreatedEventDTO event = json.mapTo(PollCreatedEventDTO.class);

        if (PollRead.findById(event.pollId()) != null) return;

        PollRead poll        = new PollRead();
        poll.id              = event.pollId();
        poll.title           = event.title();
        poll.category        = event.category();
        poll.authorId        = event.authorId();
        poll.authorName      = event.authorName();
        poll.authorGradient  = event.authorGradient();
        poll.authorAvatarUrl = event.authorAvatarUrl();
        poll.createdAt       = event.createdAt();
        poll.endDate         = event.endDate();
        poll.active          = event.active();
        poll.totalVotes      = 0;

        poll.options = event.options().stream().map(dto -> {
            PollOptionRead o = new PollOptionRead();
            o.id           = dto.optionId();
            o.text         = dto.text();
            o.displayOrder = dto.displayOrder();
            o.totalVotes   = 0;
            o.percentage   = 0;
            o.poll         = poll;
            return o;
        }).collect(Collectors.toList());

        Panache.getEntityManager().persist(poll);

        feedReadyProducer.send("POLL_CREATED", buildPollCreatedPayload(poll));
    }

    @Incoming("poll-updated")
    @Transactional @Blocking
    public void consumePollUpdated(JsonObject json) {
        PollUpdatedEventDTO event = json.mapTo(PollUpdatedEventDTO.class);

        PollRead poll = PollRead.findById(event.pollId());
        if (poll == null) return;

        if (event.title()    != null) poll.title    = event.title();
        if (event.category() != null) poll.category = event.category();
        if (event.endDate()  != null) poll.endDate  = event.endDate();
        poll.active = event.active();

        Panache.getEntityManager().merge(poll);

        feedReadyProducer.send("POLL_UPDATED", Map.of(
                "pollId",   poll.id,
                "title",    poll.title,
                "category", poll.category,
                "active",   poll.active,
                "endDate",  poll.endDate != null ? poll.endDate.toString() : ""
        ));
    }

    @Incoming("poll-deleted")
    @Transactional @Blocking
    public void consumePollDeleted(JsonObject json) {
        PollDeletedEventDTO event = json.mapTo(PollDeletedEventDTO.class);

        PollRead poll = PollRead.findById(event.pollId());
        if (poll == null) return;

        poll.delete();

        feedReadyProducer.send("POLL_DELETED", Map.of("pollId", event.pollId()));
    }

    @Incoming("option-removed")
    @Transactional @Blocking
    public void consumeOptionRemoved(JsonObject json) {
        OptionRemovedEventDTO event = json.mapTo(OptionRemovedEventDTO.class);

        PollRead poll         = PollRead.findById(event.pollId());
        PollOptionRead option = PollOptionRead.findById(event.optionId());
        if (poll == null || option == null) return;

        // desconta votos da opção removida do total do poll
        poll.totalVotes = Math.max(0, poll.totalVotes - option.totalVotes);
        option.delete();

        recalcularPercentagens(poll);

        feedReadyProducer.send("OPTION_REMOVED", Map.of(
                "pollId",          event.pollId(),
                "removedOptionId", event.optionId(),
                "totalVotes",      poll.totalVotes,
                "updatedOptions",  optionCountsPayload(poll)
        ));
    }

    // ── helpers ──────────────────────────────────────────────────

    private void recalcularPercentagens(PollRead poll) {
        if (poll.totalVotes == 0) {
            poll.options.forEach(o -> o.percentage = 0);
            return;
        }
        poll.options.forEach(o ->
                o.percentage = (int) Math.round((o.totalVotes * 100.0) / poll.totalVotes));
    }

    private List<Map<String, Object>> optionCountsPayload(PollRead poll) {
        return poll.options.stream()
                .map(o -> Map.<String, Object>of(
                        "id",         o.id,
                        "totalVotes", o.totalVotes,
                        "percentage", o.percentage))
                .toList();
    }

    /**
     * Constrói o payload para o evento POLL_CREATED com os campos do autor
     * no nível raiz, conforme esperado pelo frontend.
     */
    private Map<String, Object> buildPollCreatedPayload(PollRead poll) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", poll.id);
        payload.put("title", poll.title);
        payload.put("category", poll.category);
        payload.put("active", poll.active);
        payload.put("createdAt", poll.createdAt.toString());
        payload.put("totalVotes", 0);
        payload.put("authorId", poll.authorId);
        payload.put("authorName", poll.authorName);
        payload.put("authorGradient", poll.authorGradient != null ? poll.authorGradient : "");
        payload.put("authorAvatarUrl", poll.authorAvatarUrl != null ? poll.authorAvatarUrl : "");

        List<Map<String, Object>> options = poll.options.stream()
                .map(o -> {
                    Map<String, Object> optMap = new HashMap<>();
                    optMap.put("id", o.id);
                    optMap.put("text", o.text);
                    optMap.put("displayOrder", o.displayOrder);
                    optMap.put("totalVotes", 0);
                    optMap.put("percentage", 0);
                    return optMap;
                })
                .collect(Collectors.toList());
        payload.put("options", options);

        return payload;
    }
}