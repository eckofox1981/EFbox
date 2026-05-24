package eckofox.EFbox.user;

import eckofox.EFbox.exception.IllegiblePasswordException;
import eckofox.EFbox.exception.UnsafePasswordException;
import eckofox.EFbox.exception.UserNotFoundException;
import eckofox.EFbox.logger.LogEventType;
import eckofox.EFbox.logger.LoggerService;
import eckofox.EFbox.security.CookieMaker;
import eckofox.EFbox.security.JWTService;
import eckofox.EFbox.security.argon2.Argon2PasswordEncoder;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final Argon2PasswordEncoder encoder;
    private final CookieMaker cookieMaker;
    private final LoggerService loggerService;
    //https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/web/authentication/password/HaveIBeenPwnedRestApiPasswordChecker.html
    //https://haveibeenpwned.com/API/v3#PwnedPasswords
    @Bean
    public HaveIBeenPwnedRestApiPasswordChecker passwordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    /**
     * creates a user based on a UserDTO
     * checks password format in passwordValidation
     * efbox_users table is set up for unique username
     *
     * @param userDTO to be saved in database and instantiated as actual User
     * @return NopasswordUserDTO
     */
    public User createUser(UserDTO userDTO) throws IllegiblePasswordException {
        if (!passwordValidationIsOk(userDTO.getPassword())) {
            throw new IllegiblePasswordException("Password too weak for password policy.");
        }

        if (isPasswordCompromised(userDTO.getPassword())) {
            throw new UnsafePasswordException("Compromised password given during user creation. Rejected.");
        }

        User createdUser = new User(
                UUID.randomUUID(),
                userDTO.getUsername(),
                userDTO.getFirstname(),
                userDTO.getLastname(),
                encoder.encode(userDTO.getPassword()),
                List.of(UserRole.ROLE_USER),
                List.of()
        );

        User savedUser = userRepository.save(createdUser);

        loggerService.saveInfoLogg(LogEventType.INFO_USER, "User created: " + savedUser.getUsername() + ".", savedUser);

        return savedUser;
    }

    /**
     * checks the username and password against the database and generates token through JWTService
     *
     * @param username to find user in database
     * @param password to be checked in database
     * @return cookie
     * @throws LoginException purposefully vague for security
     */
    public Cookie login(String username, String password) throws LoginException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new LoginException("User " + username + " not found." ));
        if (!encoder.matches(password, user.getPassword())) {
            throw new LoginException("Password didn't match for username: " + username);
        }

        String token = jwtService.generateToken(user.getUserID());

        loggerService.saveInfoLogg(LogEventType.INFO_USER, "User logged in: " + user.getUsername() + ".", user);

        return cookieMaker.cookieBaker(token);
    }

    /**
     * returns NoPasswordDTO so user can see his/her info. The .orEseThrow is purposefully vague for security.
     *
     * @param user to be converted to DTO
     * @return NoPasswordDTO
     */
    public User seeUserInfo(User user) throws UserNotFoundException {
        return userRepository
                .findById(user.getUserID())
                .orElseThrow(() -> new UserNotFoundException("User:" + user.getUsername()));
    }

    /**
     * delete the user account from the database
     *
     * @param user to be deleted
     * @return message
     */
    public User deleteUser(User user) {
        userRepository.delete(user);

        loggerService.saveInfoLogg(LogEventType.INFO_USER, "User deleted account: " + user.getUsername() + ".", user);

        return user;
    }

    /**
     * regex from https://regexbox.com/regex-templates/password
     * checks that password has 8 letters minimum adn 64 maximum
     * lower, uppercase characters, at least one digit and @$!%*?& characters
     *
     * @param password to be checked
     * @return true if password format is correct
     */
    private boolean passwordValidationIsOk(String password) {
        String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$€¥!%*?&])[A-Za-z\\d@$€¥!%*?&]{8,64}$";
        //NIST standard: between 8 and 64 chars
        return password.matches(PASSWORD_PATTERN);
    }

    private boolean isPasswordCompromised(String hash) {
        CompromisedPasswordDecision isCompromised = passwordChecker().check(hash);
        return isCompromised.isCompromised();
    }

    /**
     * sends token to JWTService to check token is valid
     *
     * @param token to be checked
     * @return user (optional, may return empty if the validation fails)
     */
    public Optional<User> verifyAuthentication(String token) {
        try {
            UUID userID = jwtService.verifyToken(token);
            return userRepository.findById(userID);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    //adapted from https://www.javaguides.net/2024/04/spring-security-granted-authority.html
    // and https://www.baeldung.com/role-and-privilege-for-spring-security-registration
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                user.getAuthorities()
        );
    }
}
