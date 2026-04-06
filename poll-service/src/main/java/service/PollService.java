package service;

import dto.*;
import entities.Poll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import messaging.PollEventProducer;

@RequestScoped
public class PollService {

    @Inject
    PollEventProducer pollEventProducer;

    @Transactional
    public PollResponseDTO create(PollCreateDTO dto) {
        Poll poll        = new Poll();
        poll.title       = dto.title();
        poll.category    = dto.category();
        poll.authorId     = dto.userId();
        poll.active      = true;
        poll.addOptionsFromDTOs(dto.options());
        poll.persist();

        pollEventProducer.sendPollCreated(new PollCreatedEventDTO(
                poll.id, poll.title, poll.category,
                poll.authorId, dto.userName(), dto.userGradient(), dto.userAvatarUrl(),
                poll.createdAt, poll.endDate, poll.active,
                poll.options.stream()
                        .map(o -> new PollCreatedEventDTO.PollOptionDTO(o.id, o.text, o.displayOrder))
                        .toList()
        ));

        return PollResponseDTO.fromEntity(poll);
    }

    @Transactional
    public void update(Long id, PollUpdatedEventDTO dto) {
        Poll poll = Poll.findById(id);
        if (poll == null) throw new NotFoundException("Poll not found: " + id);

        boolean wasActive = poll.active;

        if (dto.title()    != null) poll.title    = dto.title();
        if (dto.category() != null) poll.category = dto.category();
        if (dto.active()   != null) poll.active   = dto.active();
        if (dto.endDate()  != null) poll.endDate  = dto.endDate();

        pollEventProducer.sendPollUpdated(new PollUpdatedEventDTO(
                poll.id, poll.title, poll.category, poll.endDate, poll.active));

        if (wasActive && Boolean.FALSE.equals(dto.active())) {
            pollEventProducer.sendPollClosed(
                    new PollClosedEventDTO(poll.id, poll.title, poll.authorId));
        }
    }

    @Transactional
    public void delete(Long id) {
        Poll poll = Poll.findById(id);
        if (poll == null) throw new NotFoundException("Poll not found: " + id);
        poll.delete();
        pollEventProducer.sendPollDeleted(new PollDeletedEventDTO(id));
    }
}