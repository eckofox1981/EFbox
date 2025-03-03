package eckofox.EFbox.security;

import eckofox.EFbox.user.User;
import eckofox.EFbox.user.UserRepository;
import eckofox.EFbox.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication potentialOAuth2Authentication = SecurityContextHolder.getContext().getAuthentication();

        var user = new User();

        if (potentialOAuth2Authentication != null) {
            if (potentialOAuth2Authentication instanceof OAuth2AuthenticationToken oAuth2token) {
                OAuth2User oAuth2User = oAuth2token.getPrincipal(); //TODO question: why don't I need to cast it?

                Optional<User> optionalUser = userRepository.//
            }
        }

    }
}
