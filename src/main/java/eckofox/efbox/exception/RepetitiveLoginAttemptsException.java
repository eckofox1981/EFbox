package eckofox.efbox.exception;

import javax.security.auth.login.LoginException;

public class RepetitiveLoginAttemptsException extends LoginException {
    public RepetitiveLoginAttemptsException(String message) {
        super(message);
    }
}
