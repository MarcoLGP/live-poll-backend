package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@RegisterForReflection
public record PollCreateDTO(
        @NotBlank String title,
        @NotBlank String category,
        @NotNull Long userId,
        @NotBlank String userName,
        String userGradient,
        String userAvatarUrl,
        @NotNull List<PollOptionCreateDTO> options
) {
}
