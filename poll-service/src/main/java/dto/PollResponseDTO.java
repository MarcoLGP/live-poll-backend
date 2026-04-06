package dto;

import entities.Poll;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RegisterForReflection
public record PollResponseDTO(
        Long id,
        String title,
        LocalDateTime endDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PollOptionResponseDTO> options
) {
    public static PollResponseDTO fromEntity(Poll poll) {
        return new PollResponseDTO(
                poll.id,
                poll.title,
                poll.endDate,
                poll.createdAt,
                poll.updatedAt,
                poll.options.stream()
                        .map(PollOptionResponseDTO::fromEntity)
                        .collect(Collectors.toList()
                        )
        );
    }
}
