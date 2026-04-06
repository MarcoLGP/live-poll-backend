package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;

@RegisterForReflection
public record VoteEventDTO(
        Long userId,
        String userName,
        Long pollId,
        Long optionId,
        Long oldOptionId,
        VoteAction action,
        Long authorId
) {
    public enum VoteAction { ADDED, REMOVED, CHANGED }
}
