package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RegisterForReflection
public record VoteCastDTO(@NotNull Long userId, @NotBlank String userName, @NotNull Long pollId, @NotNull Long optionId, @NotNull Long authorId) {
}
