package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDateTime;
import java.util.List;

@RegisterForReflection
public record PollReadFeedDTO(
        Long id,
        String title,
        String category,
        Long authorId,
        String authorName,
        String authorGradient,
        String authorAvatarUrl,
        LocalDateTime createdAt,
        boolean active,
        int totalVotes,
        Long myVotedOptionId,
        List<PollOptionReadFeedDTO> options
) {
}
