package eckofox.efbox.security.passwordrecovery;

import eckofox.efbox.email.EmailType;
import eckofox.efbox.exception.AccessCodeDoesNotExistsException;
import eckofox.efbox.exception.AccessCodeDoesNotMatchException;
import eckofox.efbox.security.argon2.Argon2PasswordEncoder;
import eckofox.efbox.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccessCodeService {
    private final Argon2PasswordEncoder encoder;
    private final UserAccessCodeRepository accessCodeRepository;

    public int generateAndSaveCode(EmailType purpose, User user) {
        int code = (int) ((Math.random() * 9999) + 1000);

        ifRequestAlreadyExistingDelete(purpose, user);

        UserAccessCode accessCode = new UserAccessCode(
                UUID.randomUUID(),
                user.getUserID(),
                String.valueOf(encoder.encode(String.valueOf(code))),
                purpose
        );
        accessCodeRepository.save(accessCode);


        return code;
    }

    public boolean checkCodeValidity(int code, User user, EmailType purpose) throws AccessCodeDoesNotExistsException {
        UserAccessCode existingRequest =
                accessCodeRepository.findByUserIDAndPurpose(user.getUserID(), purpose).orElse(null);

        if (existingRequest == null) {
            throw new AccessCodeDoesNotExistsException(
                    "User tried non-existing code for purpose:" + purpose + "\nUser:" + user.getUsername()
            );
        }

        String codeChar = String.valueOf(code);

        if (!encoder.matches(codeChar, existingRequest.getCode())) {
            throw new AccessCodeDoesNotMatchException(
                    "User: " + user.getUsername() + " did not have the correct code. Purpose: " + purpose);
        }

        return true;
    }

    private void ifRequestAlreadyExistingDelete(EmailType purpose, User user) {
        UserAccessCode existingRequest = accessCodeRepository
                .findByUserIDAndPurpose(user.getUserID(), purpose).orElse(null);

        if (existingRequest != null) {
            accessCodeRepository.delete(existingRequest);
        } //else nothing
    }
}
