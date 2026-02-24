package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.inject.Vetoed;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RegisterForReflection
@Vetoed
public record RegisterDTO(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password
) {}
