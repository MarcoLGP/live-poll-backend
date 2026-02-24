package dto;

import jakarta.validation.constraints.NotBlank;

public record PollOptionCreateDTO(
        @NotBlank String text,
        Integer displayOrder
) {}
