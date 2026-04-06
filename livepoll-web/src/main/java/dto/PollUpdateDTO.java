package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDateTime;

@RegisterForReflection
public record PollUpdateDTO(
        Long pollId,
        String title,
        String category,
        LocalDateTime endDate,
        Boolean active
) {
}
