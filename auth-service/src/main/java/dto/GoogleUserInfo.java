package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record GoogleUserInfo(
        String sub,
        String name,
        @JsonProperty("given_name") String givenName,
        @JsonProperty("family_name") String familyName,
        String picture,
        String email,
        @JsonProperty("email_verified") boolean emailVerified
) {}
