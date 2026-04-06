package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PollOptionReadFeedDTO(
        Long id,
        String text,
        int displayOrder,
        int totalVotes,
        int percentage
) {}
