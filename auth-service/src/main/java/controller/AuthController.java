package controller;

import dto.AccessTokenResponseDTO;
import dto.AuthResponseDTO;
import dto.LoginDTO;
import dto.RegisterDTO;
import entities.RefreshToken;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import service.AuthService;
import service.RefreshTokenService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    private AuthService authService;

    @Inject
    private RefreshTokenService refreshTokenService;

    @POST
    @Path("/register")
    public Response register(@Valid RegisterDTO dto) {
        System.out.println("register");
        try {
            AuthResponseDTO authResponse = authService.register(dto);
            return buildTokenResponse(authResponse);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
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
        String newAccessToken = authService.generateAccessToken(newRefreshToken.credential.userProfileId.toString());

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

    private Response buildTokenResponse(AuthResponseDTO authResponse) {
        NewCookie cookie = createRefreshTokenCookie(authResponse.refreshToken());
        return Response.ok(new AccessTokenResponseDTO(authResponse.accessToken()))
                .cookie(cookie)
                .build();
    }

    private NewCookie createRefreshTokenCookie(String refreshToken) {
        NewCookie.Builder builder = new NewCookie.Builder("refreshToken")
                .value(refreshToken)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .httpOnly(true);

        if (io.quarkus.runtime.LaunchMode.current().isDevOrTest()) {
            builder.secure(false).sameSite(NewCookie.SameSite.LAX);
        } else {
            builder.secure(true).sameSite(NewCookie.SameSite.NONE);
        }
        return builder.build();
    }
}