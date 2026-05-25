package eckofox.efbox.security.passwordrecovery;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PasswordRecoveryDTO {
    private String username;
    private String newPassword;
    private int code;
}
