package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record VoteEventDTO(
        Long userId,
        String userName,
        Long pollId,
        String pollTitle,
        Long optionId,
        Long oldOptionId,
        VoteAction action,
        Long authorId
) {
    public enum VoteAction { ADDED, REMOVED, CHANGED }
}
