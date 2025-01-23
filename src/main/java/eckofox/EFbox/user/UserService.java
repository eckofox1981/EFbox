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
        User createdUser = new User(UUID.randomUUID(), userDTO.getUsername(), userDTO.getFirstname(),
                userDTO.getLastname(), passwordEncoder.encode(userDTO.getPassword()));
        userRepository.save(createdUser);
        return new NoPasswordUserDTO(createdUser.getUserID(), createdUser.getUsername(),
                createdUser.getFirstName(), createdUser.getLastName());
    }

    //LOGIN
    public String login(UserDTO userDTO){

        return "TESTING: login";
    }

    //GET?

    //DELETE
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
