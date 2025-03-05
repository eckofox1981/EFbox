package eckofox.EFbox.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * class not very different from what has been taught in the course
 */

@Component
@Data
public class JWTService {
    private final String secretString;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    @Autowired
    public JWTService() {
        this.secretString = setSecretString();
        this.algorithm = Algorithm.HMAC256(secretString);
        this.verifier = JWT.require(algorithm)
                .withIssuer("auth0")
                .build();
    }

    public String generateToken(UUID userID) {
        return JWT.create()
                .withIssuer("auth0")
                .withSubject(userID.toString())
                .withExpiresAt(Instant.now().plus(60, ChronoUnit.MINUTES))
                .sign(algorithm);
    }

    public UUID verifyToken(String token) {
        DecodedJWT decodedJWT = verifier.verify(token);
        String idString = decodedJWT.getSubject();
        return UUID.fromString(idString);
    }

    /**
     * fetches secret string in separate text file that (in theory) should not be shared on GitHub. Shared here for simplicity
     * if the secret string is not found the application will exit
     *
     * @return secret string for algorithm
     */
    private String setSecretString() {
        try {
            var secret = getClass().getResourceAsStream("/secret.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(secret));
            return reader.readLine();
        } catch (IOException e) {
            System.err.println("Error: secret string not found. The Application has shutdown.\n" + e.getMessage());
            System.exit(0);
            return null; //will never be accessed (shutdown above) but the IDE requires a return statement.
        }
    }
}
