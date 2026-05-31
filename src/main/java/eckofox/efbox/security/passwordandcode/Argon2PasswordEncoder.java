package eckofox.efbox.security.passwordandcode;

import de.mkammerer.argon2.Argon2;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Argon2PasswordEncoder implements PasswordEncoder {
    private final Argon2 argon2;
    private final Argon2Properties argon2Properties;

    /**
     * encodes and hash password
     * @param rawPassword
     * @return hashed password in String
     */
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

    /**
     * checks if hashed input for user matches the stored hashed password
     * @param rawPassword the raw password to encode and match
     * @param encodedPassword the encoded password from storage to compare with
     * @return boolean based on if hasehd input equals hashed stored value
     */
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
