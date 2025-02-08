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

    /**
     * creates a user based on a UserDTO
     * checks password format
     * @param userDTO to be saved in database and instantiated as actual User
     * @return NopasswordUserDTO
     */
    public User createUser(UserDTO userDTO) {
        if (!passwordValidationIsOk(userDTO.getPassword())) {
            throw new IllegalArgumentException("Password not eligible. Requirements: 5 letters minimum, lower and uppercase " +
                    "characters and at least one digit.");
        }
        User createdUser = new User(UUID.randomUUID(), userDTO.getUsername(), userDTO.getFirstname(),
                userDTO.getLastname(), passwordConfig.passwordEncoder().encode(userDTO.getPassword()));

        return userRepository.save(createdUser);
    }

    /**
     * logs the user and generates token through JWTService
     * @param username to find user in database
     * @param password to be checked in database
     * @return token
     * @throws LoginException
     */
    public String login(String username, String password) throws LoginException {
        User user = userRepository.findByUsername(username).orElseThrow();

        if (!passwordConfig.passwordEncoder().matches(password, user.getPassword())) {
            throw new LoginException("Incorrect username or password");
        }

        return jwtService.generateToken(user.getUserID());
    }

    /**
     * returns NoPasswordDTO so user can see his/her info. The .orEseThrow is purposefully vague for security.
     * @param user to be converted to DTO
     * @return NoPasswordDTO
     */
    public User seeUserInfo(User user) throws Exception {
        User userForInfo = userRepository.findById(user.getUserID()).orElseThrow(()-> new Exception("Error fetching data."));
        return userForInfo;
    }

    /**
     * delete the user account from the database
     * @param user to be deleted
     * @return message
     */
    public User deleteUser(User user) {
        userRepository.delete(user);
        return user;
    }

    /**
     * checks that password has 5 letters minimum, lower and uppercase characters and at least one digit.
     * @param password to be checked
     * @return true if password format is correct
     */
    private boolean passwordValidationIsOk(String password) {
        return (password.length() > 5 && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z0-9]+$"));
    }

    /**
     * sends token to JWTService to check token is valid
     * @param token to be checked
     * @return user (optional, may return empty if the validation fails)
     */
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
