package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record UserDTO(
        Long id,
        String username,
        String email
) {}
