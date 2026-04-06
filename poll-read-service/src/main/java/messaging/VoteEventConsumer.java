package messaging;

import dto.MilestoneEventDTO;
import dto.PollVoteCastEventDTO;
import dto.VoteEventDTO;
import entities.PollOptionRead;
import entities.PollRead;
import entities.PollVotesRead;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class VoteEventConsumer {

    @Inject
    FeedReadyProducer feedReadyProducer;

    @Inject
    MilestoneEventProducer milestoneEventProducer;

    @Inject
    PollVoteCastEventProducer pollVoteCastEventProducer;

    @Incoming("vote-cast")
    @Transactional
    @Blocking
    public void consumeVoteCast(JsonObject json) {
        try {
            VoteEventDTO event = json.mapTo(VoteEventDTO.class);

            PollRead poll = PollRead.findById(event.pollId());
            if (poll == null) {
                return;
            }

            PollOptionRead opt = PollOptionRead.findById(event.optionId());

            switch (event.action()) {
                case ADDED -> {
                    if (opt == null) {
                        return;
                    }
                    opt.totalVotes++;
                    poll.totalVotes++;
                    PollVotesRead v = new PollVotesRead();
                    v.option = opt;
                    v.userId = event.userId();
                    v.persist();
                }
                case REMOVED -> {
                    if (opt == null) {
                        return;
                    }
                    opt.totalVotes = Math.max(0, opt.totalVotes - 1);
                    poll.totalVotes = Math.max(0, poll.totalVotes - 1);
                    PollVotesRead.delete("option.id = ?1 and userId = ?2",
                            event.optionId(), event.userId());
                }
                case CHANGED -> {
                    PollOptionRead oldOpt = PollOptionRead.findById(event.oldOptionId());
                    PollOptionRead newOpt = PollOptionRead.findById(event.optionId());
                    if (oldOpt == null || newOpt == null) {
                        return;
                    }
                    oldOpt.totalVotes = Math.max(0, oldOpt.totalVotes - 1);
                    newOpt.totalVotes++;
                    PollVotesRead voteRead = PollVotesRead
                            .find("option.id = ?1 and userId = ?2",
                                    event.oldOptionId(), event.userId())
                            .firstResult();
                    if (voteRead != null) voteRead.option = newOpt;
                    opt = newOpt;
                }
            }

            recalcularPercentagens(poll);
            poll.persist();

            pollVoteCastEventProducer.send(new PollVoteCastEventDTO(event.userId(), event.userName(), event.pollId(),
                    poll.title, event.optionId(), opt.text, event.oldOptionId(), event.action(), event.authorId()));

            feedReadyProducer.send("VOTE_UPDATED", Map.of(
                    "pollId",     poll.id,
                    "totalVotes", poll.totalVotes,
                    "options",    optionCountsPayload(poll)
            ));

            if (poll.totalVotes > 0 && poll.totalVotes % 100 == 0) {
                milestoneEventProducer.send(new MilestoneEventDTO(poll.id, poll.title, poll.authorId, poll.authorName, poll.totalVotes));
            }
        } catch (Exception e) {
            System.err.println("Erro fatal ao processar mensagem: " + e);
            e.printStackTrace();
            throw e;
        }
    }

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
}