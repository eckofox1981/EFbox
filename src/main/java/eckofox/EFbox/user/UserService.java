package eckofox.EFbox.user;

import eckofox.EFbox.security.JWTService;
import eckofox.EFbox.security.PasswordConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final PasswordConfig passwordConfig;

    //CREATE
    public UserController.NoPasswordUserDTO createUser(UserDTO userDTO) {
        if (!passwordValidationIsOk(userDTO.getPassword())) {
            throw new IllegalArgumentException("Password not eligible. Requirements: 5 letters minimum, lower and uppercase " +
                    "characters and at least one digit.");
        }
        User createdUser = new User(UUID.randomUUID(), userDTO.getUsername(), userDTO.getFirstname(),
                userDTO.getLastname(), passwordConfig.passwordEncoder().encode(userDTO.getPassword()));
        userRepository.save(createdUser);
        return UserController.NoPasswordUserDTO.fromUser(createdUser);
    }

    //LOGIN
    public String login(String username, String password) throws LoginException {
        User user = userRepository.findByUsername(username).orElseThrow();

        if (!passwordConfig.passwordEncoder().matches(password, user.getPassword())) {
            throw new LoginException("Incorrect username or password");
        }

        return jwtService.generateToken(user.getUserID());
    }

    public UserController.NoPasswordUserDTO seeUserInfo(User user) {
        User userForInfo = userRepository.findById(user.getUserID()).orElseThrow();
        return UserController.NoPasswordUserDTO.fromUser(userForInfo);
    }

    //DELETE
    public String deleteUser (User user) {
        String username = user.getUsername();
        userRepository.delete(user);
        return "Account: " + username + " deleted.";
    }

    private boolean passwordValidationIsOk(String password) {
        return (password.length() > 5 && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z0-9]+$"));
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
