package eckofox.efbox.user;

import eckofox.efbox.email.EmailSenderService;
import eckofox.efbox.exception.EmailNotSentException;
import eckofox.efbox.exception.UserNotFoundException;
import eckofox.efbox.logger.LogEventType;
import eckofox.efbox.logger.LoggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final LoggerService loggerService;
    private final EmailSenderService emailSenderService;

    /**
     * sends a admin-status request to OWNER
     * @param user making the request
     * @param secret extra security, the user must know a secret string or number (here predefined as env-var)
     * @return response in string
     * @throws IllegalAccessException
     * @throws EmailNotSentException
     */
    public String requestAdminStatus(User user, String secret) throws IllegalAccessException, EmailNotSentException {
        if (!secret.equals(System.getenv("SECRET_STRING_ADMIN"))) {
            throw new IllegalAccessException("Admin accessed refused, invalid passkey.");
        }

        if (user.getRoles().contains(UserRole.ROLE_ADMIN)) {
            return "You are admin.";
        }

        emailSenderService.sendAdminStatusRequest(user);

        loggerService.saveInfoLogg(
                LogEventType.INFO_ADMIN, user.getUsername() + " requested ROLE_ADMIN. Email sent.", user
        );

        return "You have requested admin status.";
    }

    /**
     * sends a admin-status request to ADMINs
     * @param user making the request
     * param secret extra security, the user must know a secret string or number (here predefined as env-var)
     * @return response in string
     * @throws IllegalAccessException
     * @throws EmailNotSentException
     */
    public String requestLogAccess(User user, String secret) throws IllegalAccessException, EmailNotSentException {
        if (!secret.equals(System.getenv("SECRET_STRING_LOG_ACCESS"))) {
            throw new IllegalAccessException("Admin accessed refused, invalid passkey.");
        }

        if (user.getGrantedAuthorities().contains(GrantedAuthorities.LOG_ACCESS)) {
            return "You have access to event-logs.";
        }

        emailSenderService.sendLogAccessRequest(user);

        loggerService.saveInfoLogg(
                LogEventType.INFO_ADMIN, user.getUsername() + " requested LOG_ACCESS. Email sent.", user
        );

        return "You have requested access to the event-logs now.";
    }

    /**
     * only available to OWNER (see SecurityConfig)
     * @param owner granting the user admin status
     * @param userId beeing granted the status
     * @return response in String
     */
    public String grantAdminStatus(User owner, UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Could not find " + userId));

        if (user.getRoles().contains(UserRole.ROLE_ADMIN)) {
            return user.getUsername() + "is already admin";
        }

        user.getRoles().add(UserRole.ROLE_ADMIN);
        userRepository.save(user);

        loggerService.saveInfoLogg(
                LogEventType.INFO_ADMIN,
                user.getUsername() + " granted AdminStatus by " + owner.getUsername(),
                        owner
        );

        return user.getUsername() + " is now admin.";
    }

    /**
     * only available to ADMINs (see SecurirityConfig)
     * @param admin granting log access
     * @param userId being granted log access
     * @return repsonse in String
     */
    public String grantLogAccess(User admin, UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Could not find " + userId));

        if (user.getGrantedAuthorities().contains(GrantedAuthorities.LOG_ACCESS)) {
            return user.getUsername() + "has already LOG-ACCESS";
        }

        user.getGrantedAuthorities().add(GrantedAuthorities.LOG_ACCESS);
        userRepository.save(user);

        loggerService.saveInfoLogg(
                LogEventType.INFO_ADMIN,
                user.getUsername() + " granted log access by " + admin.getUsername(),
                admin
        );

        return user.getUsername() + " is has access to the log.";
    }

    /**
     * self-explanatory
     * @param user revoking
     * @param revokedId of User being revoked admin status
     * @return
     */
    public String revokeAdminStatus(User user, UUID revokedId) {
        User revokedUser = userRepository.findById(revokedId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!revokedUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            return revokedUser.getUsername() + "doesn't have Admin status. \n ROLES:" + revokedUser.getRoles()
                    + " \n GRANTEDAUTHORITIES: " + revokedUser.getGrantedAuthorities();
        }

        revokedUser.getRoles().remove(UserRole.ROLE_ADMIN);
        revokedUser.getGrantedAuthorities().clear();

        userRepository.save(revokedUser);

        loggerService.saveInfoLogg(
                LogEventType.INFO_ADMIN,
                "Owner revoked admin status of" + revokedUser.getUsername() + ".",
                user
        );

        return "You have revoked " + revokedUser.getUsername() + "'s admin status.";
    }


    /**
     * self-explanatory
     * @param user revoking access
     * @param revokedId
     * @return
     */
    public String revokeLogAccess(User user, UUID revokedId) {
        User revokedUser = userRepository.findById(revokedId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!revokedUser.getGrantedAuthorities().contains(GrantedAuthorities.LOG_ACCESS)) {
            return revokedUser.getUsername() + "doesn't have access to event logs. \n ROLES:" + revokedUser.getRoles()
                    + " \n GRANTEDAUTHORITIES: " + revokedUser.getGrantedAuthorities();
        }

        revokedUser.getGrantedAuthorities().remove(GrantedAuthorities.LOG_ACCESS);

        userRepository.save(revokedUser);

        loggerService.saveInfoLogg(LogEventType.INFO_ADMIN,
                "Owner revoked log access of " + revokedUser.getUsername() + ".",
                user
        );

        return "You have revoked " + revokedUser.getUsername() + "'s log access.";
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.loadUserByUsername(username);
    }
}
