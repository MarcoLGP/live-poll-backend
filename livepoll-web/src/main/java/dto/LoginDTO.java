package dto;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Vetoed;
import jakarta.validation.constraints.NotBlank;

@RequestScoped
@Vetoed
public record LoginDTO(
        @NotBlank String email,
        @NotBlank String password
) {
}
