package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record GitHubEmail(
        String email,
        boolean primary,
        boolean verified
) {}
