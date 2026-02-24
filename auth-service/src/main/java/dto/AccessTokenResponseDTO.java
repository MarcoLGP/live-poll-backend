package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AccessTokenResponseDTO(
        String accessToken
) {}
