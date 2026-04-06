package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.time.LocalDateTime;

@RegisterForReflection
public record PollUpdatedEventDTO(
        Long pollId,
        String title,
        String category,
        LocalDateTime endDate,
        boolean active
) {
}