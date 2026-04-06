package messaging;

import dto.PollVoteCastEventDTO;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class PollVoteCastEventProducer {

    @Channel("poll-vote-cast")
    Emitter<PollVoteCastEventDTO> emitter;

    public void send(PollVoteCastEventDTO dto) {
        emitter.send(dto);
    }

}
