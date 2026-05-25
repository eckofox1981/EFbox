package eckofox.EFbox.user;

import eckofox.EFbox.exception.GlobalExceptionHandler;
import eckofox.EFbox.exception.IllegiblePasswordException;
import eckofox.EFbox.exception.NoTokenFoundException;
import eckofox.EFbox.exception.UserNotFoundException;
import eckofox.EFbox.fileobjects.efboxfolder.EFBoxFolder;
import eckofox.EFbox.security.CookieMaker;
import eckofox.EFbox.security.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userservice;
    private final GlobalExceptionHandler exceptionHandler;
    private final CookieMaker cookieMaker;
    private final JWTService jwtService;


    /**
     * sends request to Service
     *
     * @param userDTO gives the basic information to convert to a proper user account
     * @return NoPasswordDTO or error
     */
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) throws IllegiblePasswordException {
            User user = userservice.createUser(userDTO);

            return ResponseEntity.ok(NoPasswordUserDTO.fromUser(user));

    }

    /**
     * sends request to Service
     *
     * @param userDTO used for login but first- and lastname will not be checked (assumes frontend to send proper format)
     * @return token-cookie or error (badRequest purposefully vague)
     */
    @PutMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO, HttpServletResponse response) throws LoginException {
            response.addCookie(userservice.login(userDTO.getUsername(), userDTO.getPassword()));

            return ResponseEntity.ok("Login successful. Welcome to EFBox!");
    }

    /**
     * https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/cookievalue.html#page-title
     *
     * sends request to Service
     *
     * @param user based on cookie with JWT
     * @return NoPasswordDTO or error (badRequest purposefully vague)
     */
    @GetMapping("/info")
    public ResponseEntity<?> showUserInfo(
            @AuthenticationPrincipal User user,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws UserNotFoundException, NoTokenFoundException {
            User userForInfo = userservice.seeUserInfo(user);

            response.addCookie(cookieMaker
                    .cookieBaker(jwtService.tokenRefreshIfThreeMinutesLeft(request, user.getUserID())));
            return ResponseEntity.ok(NoPasswordUserDTO.fromUser(userForInfo));
    }

    /**
     * sends request to Service
     *
     * @param user based on token to be deleted in service
     * @return message or error
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal User user) {
        try {
            userservice.deleteUser(user);
            return ResponseEntity.status(202).body("Account: " + user.getUsername() + " deleted.");
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body("Unable to delete account.");
        }
    }

    /**
     * NoPasswordDTO for user DTO not showing password hash and displaying user's folders' name only
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class NoPasswordUserDTO {
        private UUID userID;
        private String username;
        private String firstname;
        private String lastname;
        private String email;
        private List<UserRole> roles;
        private List<GrantedAuthorities> grantedAuthorities;
        private List<String> efFolderNames;

        /**
         * converts user to user dto (no password)
         *
         * @param user to be converted
         * @return NopassWordUserDTO
         */
        public static NoPasswordUserDTO fromUser(User user) {
            List<String> folderNames = new ArrayList<>();
            if (user.getRootFolder() == null || user.getRootFolder().isEmpty()) {
                folderNames.add("EMPTY");
            } else {
                folderNames = user.getRootFolder()
                        .stream()
                        .map(EFBoxFolder::getName)
                        .toList();
            }
            return new NoPasswordUserDTO(
                    user.getUserID(),
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRoles(),
                    user.getGrantedAuthorities(),
                    folderNames
            );
        }
    }

}

/**
 * only used for account creation and login since it contains all details about the user.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
class UserDTO {
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private String password;

    UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}


