package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PollReadSearchResultDTO(
        Long id,
        String title,
        String category,
        boolean active,
        int totalVotes,
        String authorName,
        String authorGradient,
        String authorAvatarUrl
) {

}
