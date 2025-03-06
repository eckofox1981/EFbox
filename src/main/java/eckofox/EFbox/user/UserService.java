package eckofox.EFbox.user;

import eckofox.EFbox.security.JWTService;
import eckofox.EFbox.security.PasswordConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final PasswordConfig passwordConfig;

    /**
     * creates a user based on a UserDTO
     * checks password format in passwordValidation
     * efbox_users table is set up for unique username
     *
     * @param userDTO to be saved in database and instantiated as actual User
     * @return NoPasswordUserDTO
     */
    public User createUser(UserController.UserDTO userDTO) {
        if (!passwordValidationIsOk(userDTO.getPassword())) {
            throw new IllegalArgumentException("Password not eligible. Requirements: 5 letters minimum, lower and uppercase " +
                    "characters and at least one digit.");
        }

        User createdUser = new User(UUID.randomUUID(), userDTO.getUsername(), userDTO.getFirstname(),
                userDTO.getLastname(), passwordConfig.passwordEncoder().encode(userDTO.getPassword()));

        return userRepository.save(createdUser);
    }

    /**
     * checks the username and password against the database and generates token through JWTService
     *
     * @param username to find user in database
     * @param password to be checked in database
     * @return token
     * @throws LoginException purposefully vague for security
     * @throws NoSuchElementException purposefully vague for security
     */
    public String login(String username, String password) throws LoginException, NoSuchElementException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Incorrect username or password"));
        if (!passwordConfig.passwordEncoder().matches(password, user.getPassword())) {
            throw new LoginException("Incorrect username or password");
        }

        return jwtService.generateToken(user.getUserID());
    }

    /**
     * returns NoPasswordDTO so user can see his/her info. The .orEseThrow is purposefully vague for security.
     *
     * @param user to be converted to DTO
     * @return NoPasswordDTO
     */
    public User seeUserInfo(User user) throws Exception {
        return userRepository.findById(user.getUserID()).orElseThrow(() -> new Exception("Error fetching data."));
    }

    /**
     * delete the user account from the database
     *
     * @param user to be deleted
     * @return message
     */
    public User deleteUser(User user) {
        userRepository.delete(user);
        return user;
    }

    /**
     * checks that password has 5 letters minimum, lower and uppercase characters and at least one digit.
     *
     * @param password to be checked
     * @return true if password format is correct
     */
    private boolean passwordValidationIsOk(String password) {
        return (password.length() > 5 && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z0-9]+$"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

}
