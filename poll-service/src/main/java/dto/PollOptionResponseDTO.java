package dto;

import entities.PollOption;

public record PollOptionResponseDTO(
        Long id,
        String text,
        Integer displayOrder
) {
    public static PollOptionResponseDTO fromEntity(PollOption pollOption) {
        return new PollOptionResponseDTO(pollOption.id, pollOption.text, pollOption.displayOrder);
    }
}
