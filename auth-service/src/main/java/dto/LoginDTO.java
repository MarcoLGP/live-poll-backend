package dto;

import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.constraints.NotBlank;
import jakarta.enterprise.inject.Vetoed;

@RequestScoped
@Vetoed
public record LoginDTO(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
) {}
