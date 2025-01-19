package eckofox.EFbox.ApiUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiUserService {
    private final ApiUserRepository userRepository;

    @Autowired
    public ApiUserService(ApiUserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
