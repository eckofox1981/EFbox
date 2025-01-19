package eckofox.EFbox.ApiUser;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiUserController {
    private final ApiUserService userservice;

    @Autowired
    public ApiUserController(ApiUserService userservice) {
        this.userservice = userservice;
    }
}
