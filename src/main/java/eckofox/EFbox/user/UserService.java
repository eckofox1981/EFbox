package eckofox.EFbox.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final ApiUserRepository userRepository;

    @Autowired
    public UserService(ApiUserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
