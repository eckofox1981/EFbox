package eckofox.EFbox.security.argon2;

import de.mkammerer.argon2.Argon2;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Argon2PasswordEncoder implements PasswordEncoder {
    private final Argon2 argon2;
    private final Argon2Properties argon2Properties;

    @Override
    public String encode(CharSequence rawPassword) {
        char[] rawChars = rawPassword.toString().toCharArray();
        try {
            return argon2.hash(
                    argon2Properties.getTime(),
                    argon2Properties.getMemory(),
                    argon2Properties.getParallelism(),
                    rawChars);
        } finally {
            argon2.wipeArray(rawChars);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        char[] rawChars = rawPassword.toString().toCharArray();
        try {
            return argon2.verify(encodedPassword, rawChars);
        } finally {
            argon2.wipeArray(rawChars);
        }
    }
}
