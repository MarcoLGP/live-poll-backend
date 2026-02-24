package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AuthResponseDTO(
        String accessToken,
        String refreshToken
) {}
