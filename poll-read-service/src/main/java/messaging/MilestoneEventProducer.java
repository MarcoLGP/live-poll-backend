package messaging;

import dto.MilestoneEventDTO;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class MilestoneEventProducer {

    @Channel("poll-milestone")
    Emitter<MilestoneEventDTO> emitter;

    public void send(MilestoneEventDTO dto) {
        emitter.send(dto);
    }
}
