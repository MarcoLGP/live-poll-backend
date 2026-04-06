package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PollVoteCastEventDTO(
        Long userId,
        String userName,
        Long pollId,
        String pollTitle,
        Long optionId,
        String optionTitle,
        Long oldOptionId,
        VoteEventDTO.VoteAction action,
        Long authorId
) {
}
