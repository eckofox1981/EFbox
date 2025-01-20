package eckofox.EFbox.user;

import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


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
}
