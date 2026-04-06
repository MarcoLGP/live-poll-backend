package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record EmailChangedEventDTO(
        Long userId,
        String newEmail
) {
}
