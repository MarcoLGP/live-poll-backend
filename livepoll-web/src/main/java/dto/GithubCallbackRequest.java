package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;

@RegisterForReflection
public record GithubCallbackRequest(
        @NotBlank String code,
        @NotBlank String state
) {}
