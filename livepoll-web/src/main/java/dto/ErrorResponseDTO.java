package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ErrorResponseDTO(
        String message,
        String errorCode,
        int status
) {}
