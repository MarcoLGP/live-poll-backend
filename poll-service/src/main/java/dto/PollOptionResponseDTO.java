package dto;

import entities.PollOption;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PollOptionResponseDTO(
        Long id,
        String text,
        Integer displayOrder
) {
    public static PollOptionResponseDTO fromEntity(PollOption pollOption) {
        return new PollOptionResponseDTO(pollOption.id, pollOption.text, pollOption.displayOrder);
    }
}
