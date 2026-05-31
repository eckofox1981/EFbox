package eckofox.efbox.security.passwordandcode;

import org.springframework.stereotype.Component;

/**
 * Used for generation of user code for password recovery
 */
@Component
public class SecureRandom {
    public java.security.SecureRandom secureRandom() {
        return new java.security.SecureRandom();
    }
}
