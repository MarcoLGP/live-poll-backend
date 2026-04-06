package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@RegisterForReflection
public record PollCreatedEventDTO(
        Long pollId,
        String title,
        String category,
        Long authorId,
        String authorName,
        String authorGradient,
        String authorAvatarUrl,
        LocalDateTime createdAt,
        LocalDateTime endDate,
        boolean active,
        List<PollOptionDTO> options
) {
    public record PollOptionDTO(
            Long optionId,
            String text,
            int displayOrder
    ) {
    }
}