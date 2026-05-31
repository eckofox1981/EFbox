package eckofox.efbox.security.passwordrecovery;

import eckofox.efbox.email.EmailType;
import eckofox.efbox.exception.AccessCodeDoesNotExistsException;
import eckofox.efbox.exception.AccessCodeDoesNotMatchException;
import eckofox.efbox.security.passwordandcode.Argon2PasswordEncoder;
import eckofox.efbox.security.passwordandcode.SecureRandom;
import eckofox.efbox.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccessCodeService {
    private final Argon2PasswordEncoder encoder;
    private final UserAccessCodeRepository accessCodeRepository;
    private final SecureRandom secureRandom;

    /**
     * generate a code linked to a user for password recovery
     * checks if a request has already been made
     * @param purpose for EmailType
     * @param user supposedly making the request
     * @return code in Integer
     */
    public int generateAndSaveCode(EmailType purpose, User user) {
        //OWASP
        //https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html#secure-random-number-generation
        int code = secureRandom.secureRandom().nextInt(9999);

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

    /**
     * controls the username making the request has the corresponding code in the database
     * @param code given by requesting user
     * @param user requesting User
     * @param purpose EmailType for database comparison
     * @return boolean if test passes
     * @throws AccessCodeDoesNotExistsException
     */
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

        accessCodeRepository.delete(existingRequest);

        return true;
    }

    /**
     * controls database to avoid multiple password request, deletes an old one to avoid storing multiple one
     * the idea is that a user might lose the sent code and might need a new one.
     * @param purpose EMailType to check if similar request have been made
     * @param user linked to type of requests
     */
    private void ifRequestAlreadyExistingDelete(EmailType purpose, User user) {
        UserAccessCode existingRequest = accessCodeRepository
                .findByUserIDAndPurpose(user.getUserID(), purpose).orElse(null);

        if (existingRequest != null) {
            accessCodeRepository.delete(existingRequest);
        } //else nothing
    }
}
