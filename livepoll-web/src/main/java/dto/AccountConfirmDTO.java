package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;

@RegisterForReflection
public record AccountConfirmDTO(
        @NotBlank String token
) {}
