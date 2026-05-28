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
