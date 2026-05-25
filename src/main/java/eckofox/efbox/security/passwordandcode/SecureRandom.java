package eckofox.efbox.security.passwordandcode;

import org.springframework.stereotype.Component;

@Component
public class SecureRandom {
    public java.security.SecureRandom secureRandom() {
        return new java.security.SecureRandom();
    }
}
