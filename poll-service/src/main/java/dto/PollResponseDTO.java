package dto;

import entities.Poll;
import entities.enums.PollStatus;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RegisterForReflection
public record PollResponseDTO(
        Long id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        PollStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PollOptionResponseDTO> options
) {
    public static PollResponseDTO fromEntity(Poll poll) {
        return new PollResponseDTO(
                poll.id,
                poll.title,
                poll.description,
                poll.startDate,
                poll.endDate,
                poll.status,
                poll.createdAt,
                poll.updatedAt,
                poll.options.stream()
                        .map(PollOptionResponseDTO::fromEntity)
                        .collect(Collectors.toList()
                        )
        );
    }
}
