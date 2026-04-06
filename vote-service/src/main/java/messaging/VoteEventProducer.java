package messaging;

import dto.VoteEventDTO;
import entities.Vote;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class VoteEventProducer {
    @Inject
    @Channel("vote-cast")
    Emitter<VoteEventDTO> voteCastEmitter;

    public void send(VoteEventDTO event) {
        voteCastEmitter.send(event);
    }
}
