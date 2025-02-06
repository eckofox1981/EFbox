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
import java.util.IllegalFormatException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userservice;


    /**
     * sends request to Service
     * @param userDTO
     * @return NoPasswordDTO or error
     */
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            return ResponseEntity.ok(userservice.createUser(userDTO));
        } catch (IllegalFormatException e) {
            return ResponseEntity.badRequest().body("Unable to create user.Please check your inputs and try again. "
                    + e.getMessage());
        }
    }

    /**
     * sends request to Service
     * @param userDTO
     * @return token or error
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
     * @param user
     * @return NoPasswordDTO or error
     */
    @GetMapping("/info")
    public ResponseEntity<?> showUserInfo(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(userservice.seeUserInfo(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * sends request to Service
     * @param user based on token
     * @return message or error
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal User user) {
        try {
            userservice.deleteUser(user);
            return ResponseEntity.ok().body("Account:" + user.getUsername() + "deleted.");
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body("unable to delete account. " + e.getMessage());
        }
    }

    /**
     * NoPasswordDTO for user DTO not showing password and displaying user's folders
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
 * only used for account creation and login (input json will not use firstname and lastname)
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
class UserDTO {
    private String username;
    private String firstname;
    private String lastname;
    private String password;

    public UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}


