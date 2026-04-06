package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record MilestoneEventDTO(Long pollId, String pollTitle, Long authorId, String authorUsername, int totalVotes) {
}
