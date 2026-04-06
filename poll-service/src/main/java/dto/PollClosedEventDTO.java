package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PollClosedEventDTO(Long pollId, String pollTitle, Long authorId) {}
