package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public record PollClosedNotificationEventDTO(
        Long pollId,
        String pollTitle,
        Long authorId,
        List<Long> voterIds
) {}
