package controller;

import dto.*;
import entities.RefreshToken;
import entities.User;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import service.AuthService;
import service.RefreshTokenService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    AuthService authService;

    @Inject
    RefreshTokenService refreshTokenService;

    @Inject
    @ConfigProperty(name = "quarkus.profile")
    String profile;

    @Inject
    @ConfigProperty(name = "cookie.secure")
    boolean cookieSecure;

    @POST
    @Path("/register")
    public Response register(@Valid RegisterDTO dto) {
        System.out.println("register");
        AuthResponseDTO authResponse = authService.register(dto);
        return buildTokenResponse(authResponse);
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginDTO dto) {
        AuthResponseDTO authResponse = authService.login(dto);
        return buildTokenResponse(authResponse);
    }

    @POST
    @Path("/refresh")
    public Response refresh(@CookieParam("refreshToken") String refreshTokenCookie) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        RefreshToken newRefreshToken = refreshTokenService.verifyAndRotate(refreshTokenCookie);
        String newAccessToken = authService.generateAccessToken(newRefreshToken.user);

        NewCookie cookie = createRefreshTokenCookie(newRefreshToken.originalToken);

        return Response.ok(new AccessTokenResponseDTO(newAccessToken))
                .cookie(cookie)
                .build();
    }

    @POST
    @Path("/logout")
    public Response logout(@CookieParam("refreshToken") String refreshTokenCookie) {
        if (refreshTokenCookie != null) {
            refreshTokenService.revokeToken(refreshTokenCookie);
        }
        NewCookie deleteCookie = createRefreshTokenCookie(null);
        return Response.ok().cookie(deleteCookie).build();
    }

    @GET
    @Path("/me")
    @Authenticated
    public Response me(@Context SecurityContext ctx) {
        String userId = ctx.getUserPrincipal().getName();
        User user = User.findById(Long.parseLong(userId));
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return Response.ok(new UserDTO(user.id, user.username, user.email)).build();
    }

    private Response buildTokenResponse(AuthResponseDTO authResponse) {
        NewCookie cookie = createRefreshTokenCookie(authResponse.refreshToken());
        return Response.ok(new AccessTokenResponseDTO(authResponse.accessToken()))
                .cookie(cookie)
                .build();
    }

    private NewCookie createRefreshTokenCookie(String refreshToken) {
        boolean secure = cookieSecure;

        NewCookie.Builder builder = new NewCookie.Builder("refreshToken")
                .path("/")
                .httpOnly(true)
                .secure(secure);

        if (refreshToken != null && !refreshToken.isEmpty()) {
            builder.value(refreshToken)
                    .maxAge(7 * 24 * 60 * 60);
        } else {
            builder.value("")
                    .maxAge(0);
        }

        return builder.build();
    }
}