package service;

import dto.VoteEventDTO;
import entities.Vote;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import messaging.VoteEventProducer;

@ApplicationScoped
public class VoteService {

    @Inject
    VoteEventProducer voteEventProducer;

    @Transactional
    public VoteEventDTO handleVote(Long userId, String userName, Long pollId, Long optionId, Long authorId) {
        Vote existing = Vote.find("userId = ?1 and pollId = ?2", userId, pollId)
                .firstResult();

        VoteEventDTO event;

        if (existing == null) {
            Vote vote = new Vote();
            vote.userId   = userId;
            vote.pollId   = pollId;
            vote.optionId = optionId;
            vote.persist();
            event = new VoteEventDTO(userId, userName, pollId, optionId, null, VoteEventDTO.VoteAction.ADDED, authorId);

        } else if (existing.optionId.equals(optionId)) {
            existing.delete();
            event = new VoteEventDTO(userId, userName, pollId, optionId, null, VoteEventDTO.VoteAction.REMOVED, authorId);

        } else {
            Long oldOptionId = existing.optionId;
            existing.optionId = optionId;
            event = new VoteEventDTO(userId, userName, pollId, optionId, oldOptionId, VoteEventDTO.VoteAction.CHANGED, authorId);
        }

        voteEventProducer.send(event);
        return event;
    }
}