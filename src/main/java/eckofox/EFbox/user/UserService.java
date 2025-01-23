package eckofox.EFbox.user;

import com.fasterxml.jackson.databind.util.JSONPObject;
import eckofox.EFbox.JWTService.JWTService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;


    //CREATE
    public NoPasswordUserDTO createUser(UserDTO userDTO) {
        if (!passwordValidationIsOk(userDTO.getPassword())) {
            throw new IllegalArgumentException("Password not eligible. Requirements: 5 letters minimum, lower and uppercase " +
                    "characters and at least one digit.");
        }
        User createdUser = new User(UUID.randomUUID(), userDTO.getUsername(), userDTO.getFirstname(),
                userDTO.getLastname(), passwordEncoder.encode(userDTO.getPassword()));
        userRepository.save(createdUser);
        return new NoPasswordUserDTO(createdUser.getUserID(), createdUser.getUsername(),
                createdUser.getFirstName(), createdUser.getLastName());
    }

    //LOGIN
    public String login(String username, String password) throws LoginException {
        User user = userRepository.findByUsername(username).orElseThrow();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new LoginException("Incorrect username or password");
        }

        return jwtService.generateToken(user.getUserID());
    }

    //GET?

    //DELETE
    public String deleteUser (UUID userID) {
        User user = userRepository.findById(userID).orElseThrow();
        String username = user.getUsername();
        userRepository.delete(user);
        return "Account: " + username + " deleted.";
    }

    private boolean passwordValidationIsOk(String password) {
        if (password.length() > 5 && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z0-9]+$")) {
            return true;
        }
        return false;
    }

    public Optional<User> verifyAuthentication(String token) {
        try {
            UUID userID = jwtService.verifyToken(token);
            return userRepository.findById(userID);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
