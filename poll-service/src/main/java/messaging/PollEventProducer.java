package messaging;

import dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class PollEventProducer {

    @Inject @Channel("poll-created")
    Emitter<PollCreatedEventDTO> pollCreatedEmitter;

    @Inject @Channel("poll-updated")
    Emitter<PollUpdatedEventDTO> pollUpdatedEmitter;

    @Inject @Channel("poll-deleted")
    Emitter<PollDeletedEventDTO> pollDeletedEmitter;

    @Inject @Channel("poll-closed")
    Emitter<PollClosedEventDTO> pollClosedEmitter;

    public void sendPollCreated(PollCreatedEventDTO e)     { pollCreatedEmitter.send(e); }
    public void sendPollUpdated(PollUpdatedEventDTO e)     { pollUpdatedEmitter.send(e); }
    public void sendPollDeleted(PollDeletedEventDTO e)     { pollDeletedEmitter.send(e); }
    public void sendPollClosed(PollClosedEventDTO e)  { pollClosedEmitter.send(e); }
}
