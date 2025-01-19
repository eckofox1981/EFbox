package eckofox.EFbox.user;

import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.IllegalFormatException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("user")
public class UserController {
    private final UserService userservice;

    //POST create user
    @PostMapping("/create")
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
            return ResponseEntity.ok(userservice.login(userDTO));
        } catch (IllegalAccessError e) {
            return ResponseEntity.badRequest().body("Unable to login. Please check your inputs and try again. "
                    + e.getMessage());
        }

    }

    //TODO: GET user info but do I need it...?

    //DELETE user account and relevant data
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser (@RequestParam UUID userID) {
        try {
            return ResponseEntity.ok().body("User account deleted.");
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body("unable to delete account. " + e.getMessage());
        }
    }
}

//UserDTO?
@AllArgsConstructor
@Data
class UserDTO {
        private final String username;
        private final String firstname;
        private final String lastname;
        private final String password;
}

@AllArgsConstructor
@Data
class NoPasswordUserDTO {
    private final UUID userID;
    private final String username;
    private final String firstname;
    private final String lastname;
}

