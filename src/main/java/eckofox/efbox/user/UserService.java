package eckofox.efbox.user;

import eckofox.efbox.email.EmailSenderService;
import eckofox.efbox.email.EmailType;
import eckofox.efbox.exception.*;
import eckofox.efbox.logger.LogEventType;
import eckofox.efbox.logger.LogMessage;
import eckofox.efbox.logger.LoggerService;
import eckofox.efbox.security.CookieMaker;
import eckofox.efbox.security.JWTService;
import eckofox.efbox.security.bruteforceprotection.BruteForceProtectionService;
import eckofox.efbox.security.passwordandcode.Argon2PasswordEncoder;
import eckofox.efbox.security.passwordrecovery.UserAccessCodeService;
import eckofox.efbox.security.ratelimiting.RateLimitingInterceptor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final Argon2PasswordEncoder encoder;
    private final CookieMaker cookieMaker;
    private final LoggerService loggerService;
    private final EmailSenderService emailSenderService;
    private final UserAccessCodeService accessCodeService;
    private final BruteForceProtectionService bruteForceProtectionService;
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
        if (!isEmailValid(userDTO.getEmail())) {
            throw new IllegibleEmailFormatException("Email not valid [letters@domain.com].");
        }

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
                userDTO.getEmail(),
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
    public Cookie login(String username, String password, HttpServletRequest request)
            throws LoginException, EmailNotSentException {
        User user = authenticateUponLogin(username, password, request);
        String token = jwtService.generateToken(user.getUserID());

        loggerService.saveInfoLogg(LogEventType.INFO_USER, "User logged in: " + username + ".", user);

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

        loggerService.saveInfoLogg(
                LogEventType.INFO_USER,
                "User deleted account: " + user.getUsername() + ".", user
        );

        return user;
    }

    public String passwordRecovery(String username) throws UsernameNotFoundException, EmailNotSentException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username: " + username + "not found."));
        int code = accessCodeService.generateAndSaveCode(EmailType.PASSWORD_RECOVERY, user);

        try {
            return emailSenderService.sendPasswordRecoveryEmail(EmailType.PASSWORD_RECOVERY, user, code);
        } catch (Exception e) {
            throw new EmailNotSentException("Could not send email to " + user.getUsername() + ".\n" + e.getMessage());
        }
    }

    public String passwordChange(String username, String newPassword, int code)
            throws UsernameNotFoundException, AccessCodeDoesNotExistsException {
        if (!passwordValidationIsOk(newPassword)) {
            throw new IllegiblePasswordException("Password too weak for password policy.");
        }

        if (isPasswordCompromised(newPassword)) {
            throw new UnsafePasswordException("Compromised password given during user creation.");
        }

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username: " + username + "not found."));

        if (accessCodeService.checkCodeValidity(code, user, EmailType.PASSWORD_RECOVERY)) {
            User withNewPassword = new User(
                    user.getUserID(),
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    encoder.encode(newPassword),
                    user.getRoles(),
                    user.getGrantedAuthorities()
            );

            userRepository.save(withNewPassword);

            loggerService.saveInfoLogg(new LogMessage(
                    UUID.randomUUID(),
                    LogEventType.INFO_USER,
                    LocalDateTime.now(),
                    "Password changed by" + user.getUsername() + ".",
                    user
            ));
            return "Password changed. You can now login using the new password.";
        }

        loggerService.saveInfoLogg(new LogMessage(
                UUID.randomUUID(),
                LogEventType.WARNING,
                LocalDateTime.now(),
                user.getUsername() + "provided erroneous code for password recovery.",
                user
        ));
        return "Your code did not work, try again or request a new one.";

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
        String COMPLEXITY_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$€¥!%*?&])[A-Za-z\\d@$€¥!%*?&]{8,64}$";
        //NIST standard: between 8 and 64 chars
        return password.matches(COMPLEXITY_REGEX);
    }

    private boolean isPasswordCompromised(String hash) {
        CompromisedPasswordDecision isCompromised = passwordChecker().check(hash);
        return isCompromised.isCompromised();
    }

    private boolean isEmailValid(String email) {
        String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*+\\.[A-Za-z]{2,}$";
        return email.matches(EMAIL_REGEX);
    }

    private User authenticateUponLogin(String username, String password, HttpServletRequest request)
            throws LoginException, EmailNotSentException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new LoginException("User " + username + " not found." ));

        if (bruteForceProtectionService.isBlocked(user.getUsername())) {
            String clientIp = RateLimitingInterceptor.getClientIP(request);
            emailSenderService.sendRepetitiveLoginAttemptsEMail(
                    EmailType.SYSTEM_WARNING, user, LocalDateTime.now(), clientIp);
            throw new RepetitiveLoginAttemptsException(
                    "IP: " + clientIp + " made multiple login attempts on " + user.getUsername() + "'s account."
            );
        }

        if (!encoder.matches(password, user.getPassword())) {
            bruteForceProtectionService.loginFailed(user.getUsername());
            throw new LoginException("Password didn't match for username: " + user.getUsername());
        }

        bruteForceProtectionService.loginSucceeded(user.getUsername());

        return user;
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
