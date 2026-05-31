package eckofox.efbox.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import eckofox.efbox.exception.NoTokenFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;

/**
 * class not very different from what has been taught in the course
 */

@Component
@Data
public class JWTService {
    private final String secretString = System.getenv("SECRET_STRING");
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    @Autowired
    public JWTService() {
        this.algorithm = Algorithm.HMAC256(secretString);
        this.verifier = JWT.require(algorithm)
                .withIssuer("auth0")
                .build();
    }

    public String generateToken(UUID userID) {
        return JWT.create()
                .withIssuer("auth0")
                .withSubject(userID.toString())
                .withExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .sign(algorithm);
    }

    public UUID verifyToken(String token) {
        DecodedJWT decodedJWT = verifier.verify(token);
        String idString = decodedJWT.getSubject();
        return UUID.fromString(idString);
    }

    /**
     * refreshes the token if only 3 minutes are left on the TTL
     * @param request
     * @param userID
     * @return fresh JWT
     * @throws NoTokenFoundException
     */
    public String tokenRefreshIfThreeMinutesLeft(HttpServletRequest request, UUID userID) throws NoTokenFoundException {
        String token = resolveToken(request);

        long now = System.currentTimeMillis();

        DecodedJWT decodedJWT = verifier.verify(token);
        long expires = decodedJWT.getExpiresAt().getTime();

        long timeLeft = expires - now;
        long threeMinutes = Duration.ofMinutes(3).toMillis();

        if (timeLeft > threeMinutes) {
            return token;
        }

        return generateToken(userID);
    }

    private String resolveToken(HttpServletRequest request) throws NoTokenFoundException {
            return Arrays.stream(request.getCookies())
                    .filter(c -> "efbox-token".equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElseThrow(() -> new NoTokenFoundException("No token found in cookie."));
    }
}
