package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.ws.rs.core.NewCookie;

@RegisterForReflection
public record AuthorizationResponseDTO(String authorizationUri, String stateCookieHeader) {}
