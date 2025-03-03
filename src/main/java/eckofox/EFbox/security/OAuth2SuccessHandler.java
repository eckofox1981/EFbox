package eckofox.EFbox.security;

import eckofox.EFbox.user.User;
import eckofox.EFbox.user.UserRepository;
import eckofox.EFbox.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JWTService jwtService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oAuth2Token.getPrincipal();

        Optional<User> optionalUser = userRepository.findByOpenIDconnectID(oAuth2User.getName());

        if (optionalUser.isEmpty()) {
            var user = new User(UUID.randomUUID(), oAuth2User.getAttribute("login"), oAuth2User.getName(),
                    oAuth2Token.getAuthorizedClientRegistrationId());
            userRepository.save(user);
            System.out.println(user.getOpenIDconnectProvider() + " - " + user.getUsername() + " saved as " + user.getUserID());
        } else {
            System.out.println(optionalUser.get().getOpenIDconnectProvider() + " - " + optionalUser.get().getUsername()
                    + " logged in as " + optionalUser.get().getUserID());
        }

    }
}
