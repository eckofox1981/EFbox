package eckofox.EFbox.user;

import eckofox.EFbox.exception.UserNotFoundException;
import eckofox.EFbox.logger.LogEventType;
import eckofox.EFbox.logger.LoggerService;
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

    public String requestAdminStatus(User user, String secret) throws IllegalAccessException {
        //secret hardcoded in .env, a production server would have a more advanced admin status policy
        if (!secret.equals(System.getenv("SECRET_STRING_ADMIN"))) {
            throw new IllegalAccessException("Admin accessed refused, invalid passkey.");
        }

        if (user.getRoles().contains(UserRole.ROLE_ADMIN)) {
            return "You are admin.";
        }

        user.getRoles().add(UserRole.ROLE_ADMIN);

        userRepository.save(user);

        loggerService.saveInfoLogg(LogEventType.INFO_ADMIN, user.getUsername() + " granted ROLE_ADMIN", user);

        //TODO: implement email sending to ROLE_OWNER and LOG IT.

        return "You are now admin.";
    }

    public String requestLogAccess(User user, String secret) throws IllegalAccessException {
        //secret hardcoded in .env, a production server would have a more advanced admin status policy
        if (!secret.equals(System.getenv("SECRET_STRING_ADMIN"))) {
            throw new IllegalAccessException("Admin accessed refused, invalid passkey.");
        }

        if (user.getGrantedAuthorities().contains(GrantedAuthorities.LOG_ACCESS)) {
            return "You have access to event-logs.";
        }

        user.getGrantedAuthorities().add(GrantedAuthorities.LOG_ACCESS);

        userRepository.save(user);

        loggerService.saveInfoLogg(LogEventType.INFO_ADMIN, user.getUsername() + " granted LOG_ACCESS.", user);

        //TODO: implement email sending to ROLE_OWNER and LOG IT.

        return "You have access to the event-logs now.";
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
                "Owner revoked admin status of" + revokedUser.getUsername() + ".",
                user
        );

        return "You have revoked " + revokedUser.getUsername() + "'s log access.";
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.loadUserByUsername(username);
    }
}
