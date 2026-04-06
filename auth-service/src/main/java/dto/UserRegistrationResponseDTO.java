package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record UserRegistrationResponseDTO(
        String correlationId,
        Long userId,
        boolean success,
        String errorMessage
) {}