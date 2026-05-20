package eckofox.EFbox.security;

import eckofox.EFbox.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * class not very different from what has been taught in the course
 * https://www.youngju.dev/blog/architecture/2026-03-08-sso-cookie-jwt-auth-spring-boot.en#cors--credential-transmission-setup
 */

@AllArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authenticationToken = resolveToken(request);

        if(!jwtRegexValidation(authenticationToken)) {
            System.out.println("EXCEPTION");
            //todo for exception handler
        }

        if (authenticationToken == null || authenticationToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authenticationToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        userService.verifyAuthentication(authenticationToken).ifPresent(user -> {
            var authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    user.getPassword(),
                    user.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        });

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            //TODO: add validation (3 points etc, look up examples)
            return Arrays.stream(request.getCookies())
                    .filter(c -> "efbox-token".equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        return null;
    }

    /** https://testregex.com/patterns/jwt-token
     * small guard rail against token tampering
     * @param token
     * @return boolean
     */
    private boolean jwtRegexValidation(String token) {
        if (token.matches("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$")) {
            return true;
        }
        return false;
    }
}
