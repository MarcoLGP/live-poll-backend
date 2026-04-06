package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PollDeletedEventDTO(Long pollId) {}
