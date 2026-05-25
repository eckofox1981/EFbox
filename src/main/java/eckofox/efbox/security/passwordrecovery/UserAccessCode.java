package eckofox.efbox.security.passwordrecovery;

import eckofox.efbox.email.EmailType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Getter
@Setter
public class UserAccessCode {
    @Id
    private final UUID codeId;
    private final UUID userID;
    private final String code;
    private final EmailType purpose;
}
