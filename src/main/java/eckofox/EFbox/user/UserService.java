package eckofox.EFbox.user;

import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //CREATE
    public NoPasswordUserDTO createUser(UserDTO userDTO) {
        User createdUser = new User(UUID.randomUUID(), userDTO.getUsername(), userDTO.getFirstname(),
                userDTO.getLastname(), userDTO.getPassword());
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
