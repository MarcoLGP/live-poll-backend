package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record GitHubUser(
        Long id,
        String login,
        String name,
        String email,
        @JsonProperty("avatar_url")
        String avatarUrl
) {}
