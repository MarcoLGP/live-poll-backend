package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@RegisterForReflection()
public record PollCreateDTO(
        @NotBlank String title,
        String description,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate,
        List<PollOptionCreateDTO> options
) {}
