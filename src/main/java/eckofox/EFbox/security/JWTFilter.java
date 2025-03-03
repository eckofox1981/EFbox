package eckofox.EFbox.security;

import eckofox.EFbox.user.User;
import eckofox.EFbox.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Component
public class JWTFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserRepository userRepository;

    /**
     * checks if User is login with OpenID(OAuth2) or JWT, if logging in with OAuth2 and no corresponding user is present
     * in the database it will create the user (it made more sense to have it here than in a separate OAuth2SuccessHandler).
     * If logging in with JWT, the process is handled "normally" through JWTServices.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication potentialOAuth2Auth = SecurityContextHolder.getContext().getAuthentication();

        if (potentialOAuth2Auth != null) {
            if (potentialOAuth2Auth instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
                OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();

                Optional<User> optionalUser = userRepository.findByOpenIDconnectID(oAuth2User.getName());

                if (optionalUser.isEmpty()) {
                    response.sendError(401, "User not found, check OAuth2 token validity");
                    return;
                }

                var localUser = optionalUser.get();

                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        localUser, localUser.getPassword(), localUser.getAuthorities()));
                filterChain.doFilter(request, response);
            }
        }

        if (request.getHeader("Authorization") == null || request.getHeader("Authorization").isBlank()) {
            filterChain.doFilter(request, response);
        }

        UUID userID;
        try {
            userID = jwtService.verifyToken(request.getHeader("Authorization"));
        } catch (Exception e) {
            response.sendError(401, "Authorization token invalid.");
            return;
        }

        var user = userRepository.findById(userID).get();

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user, user.getPassword(), user.getAuthorities()));
        filterChain.doFilter(request, response);
    }
}
