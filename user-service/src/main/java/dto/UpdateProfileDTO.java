package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RegisterForReflection
public record UpdateProfileDTO(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank String language,
        String gradientAvatar,
        String avatarUrl
) {}
