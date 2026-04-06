package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record UserRegistrationRequestDTO(
        String correlationId,
        String email,
        String username,
        String language,
        String gradientAvatar,
        String avatarUrl
) {}