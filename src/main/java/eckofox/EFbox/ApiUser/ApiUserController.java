package eckofox.EFbox.ApiUser;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ApiUserController {
    private final ApiUserService userservice;

}
