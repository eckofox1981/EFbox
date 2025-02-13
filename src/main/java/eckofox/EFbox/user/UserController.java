package eckofox.EFbox.user;

import eckofox.EFbox.fileobjects.efboxfolder.EFBoxFolder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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


    /**
     * sends request to Service
     *
     * @param userDTO gives the basic information to convert to a proper user account
     * @return NoPasswordDTO or error
     */
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            User user = userservice.createUser(userDTO);
            return ResponseEntity.ok(NoPasswordUserDTO.fromUser(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Unable to create user.Please check your inputs and try again. "
                    + e.getMessage());
        }
    }

    /**
     * sends request to Service
     *
     * @param userDTO used for login but first- and lastname will not be checked (assumes frontend to send proper format)
     * @return token or error (badRequest purposefully vague)
     */
    @PutMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
        try {
            return ResponseEntity.ok(userservice.login(userDTO.getUsername(), userDTO.getPassword()));
        } catch (LoginException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    /**
     * sends request to Service
     *
     * @param user will be extracted from token to be identified in service and converted to NoPasswordUserDTO
     * @return NoPasswordDTO or error (badRequest purposefully vague)
     */
    @GetMapping("/info")
    public ResponseEntity<?> showUserInfo(@AuthenticationPrincipal User user) {
        try {
            User userForInfo = userservice.seeUserInfo(user);
            return ResponseEntity.ok(NoPasswordUserDTO.fromUser(userForInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
            return ResponseEntity.unprocessableEntity().body("unable to delete account. " + e.getMessage());
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
                    folderNames);
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
    private String password;

    UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}


