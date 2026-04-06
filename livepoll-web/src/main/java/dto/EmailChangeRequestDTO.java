package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RegisterForReflection
public record EmailChangeRequestDTO(@NotBlank @Email String newEmail) {}
