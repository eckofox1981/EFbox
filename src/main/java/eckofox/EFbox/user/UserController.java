package eckofox.EFbox.user;

import eckofox.EFbox.fileobjects.effolder.EFFolder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userservice;

    //POST create user
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            return ResponseEntity.ok(userservice.createUser(userDTO));
        } catch (IllegalFormatException e) {
            return ResponseEntity.badRequest().body("Unable to create user.Please check your inputs and try again. "
                    + e.getMessage());
        }
    }

    //PUT login user
    @PutMapping("/login")
    public ResponseEntity<?> login (@RequestBody UserDTO userDTO) {
        try {
            return ResponseEntity.ok(userservice.login(userDTO.getUsername(), userDTO.getPassword()));
        } catch (LoginException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    //TODO: GET user info but do I need it...?

    //DELETE user account and relevant data
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser (@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok().body(userservice.deleteUser(token));
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body("unable to delete account. " + e.getMessage());
        }
    }


}

//UserDTO?
@AllArgsConstructor
@NoArgsConstructor
@Data
class UserDTO {
        private String username;
        private String firstname;
        private String lastname;
        private String password;

        public UserDTO (String username, String password) {
            this.username = username;
            this.password = password;
        }
}

@AllArgsConstructor
@NoArgsConstructor
@Data
class NoPasswordUserDTO {
    private UUID userID;
    private String username;
    private String firstname;
    private String lastname;
    private List<EFFolder> folders;

    public NoPasswordUserDTO fromUser (User user){
        return new NoPasswordUserDTO(
                user.getUserID(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getFolders());
    }
}

