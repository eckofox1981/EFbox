package eckofox.EFbox.user;

import lombok.AllArgsConstructor;
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
            return ResponseEntity.ok("MAPPING API CREATE USER");
        } catch (IllegalFormatException e) {
            return ResponseEntity.badRequest().body("Unable to create user.Please check your inputs and try again. "
                    + e.getMessage());
        }

    }

    //PUT login user
    @PutMapping("/login")
    public ResponseEntity<?> login (@RequestBody UserDTO userDTO) {
        try {
            return ResponseEntity.ok("MAPPING API LOGIN USER");
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
class UserDTO {
        private final String username;
        private final String firstname;
        private final String lastName;
        private final String password;
}

@AllArgsConstructor
class NoPasswordUserDTO {
    private final String username;
    private final String firstname;
    private final String lastName;
}

