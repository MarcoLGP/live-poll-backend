package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RegisterForReflection
public record ChangePasswordDTO(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 6) String newPassword
) {}
