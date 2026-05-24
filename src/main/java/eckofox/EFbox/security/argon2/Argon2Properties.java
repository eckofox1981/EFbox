package eckofox.EFbox.security.argon2;

import de.mkammerer.argon2.Argon2Factory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Argon2Properties {
    @Value("${argon2.variant}")
    private String variant;

    @Value("${argon2.memory}")
    private int memory;

    @Value("${argon2.time}")
    private int time;

    @Value("${argon2.parallelism}")
    private int parallelism;

    public Argon2Factory.Argon2Types getArgon2Type() {
        return Argon2Factory.Argon2Types.ARGON2id;
    }
}
