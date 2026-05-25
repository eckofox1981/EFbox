package eckofox.efbox.security.argon2;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class Argon2Config {
    private final Argon2Properties argon2Properties;

    @Bean
    public Argon2 argon2() {
        return Argon2Factory.createAdvanced(argon2Properties.getArgon2Type());
    }
}
