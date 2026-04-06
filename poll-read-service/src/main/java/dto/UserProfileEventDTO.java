package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record UserProfileEventDTO(
        Long userId,
        String username,
        String email,
        String language,
        String gradientAvatar,
        String avatarUrl) {
}
