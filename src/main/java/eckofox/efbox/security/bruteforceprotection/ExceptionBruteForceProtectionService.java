package eckofox.efbox.security.bruteforceprotection;

import eckofox.efbox.email.EmailSenderService;
import eckofox.efbox.exception.EmailNotSentException;
import eckofox.efbox.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Caches the number of Exception and after the defined number of events during the defined number fo minutes
 * sends a warning email to the ADMINs
 * stores if the warning email has been sent to avoid sending multiple emails
 * NOTE: will reset with server restart
 * based upon //https://www.geeksforgeeks.org/advance-java/prevent-brute-force-authentication-attempts-with-spring-security/
 */

@Service
@RequiredArgsConstructor
public class ExceptionBruteForceProtectionService {
    private final EmailSenderService emailSenderService;

    private static final int MAX_NBR_OF_EXC = 5;
    private static final long TIME_FROM_FIRST = TimeUnit.MINUTES.toMillis(5);

    private final Map<ExceptionType, Integer> exceptionTypeCache = new ConcurrentHashMap<>();
    private final Map<ExceptionType, Long> firstEventTimeCache = new ConcurrentHashMap<>();
    private final Map<ExceptionType, Boolean> isEmailSentCache = new ConcurrentHashMap<>();

    /**
     * resets the cached exception number and timer
     */
    public void logAccessedExceptionCacheReset() {
        exceptionTypeCache.clear();
        isEmailSentCache.clear();
    }

    /**
     * records the number of exceptions and the time of first occurence
     * checks if defined limts have been reached
     * @param exceptionType
     * @throws EmailNotSentException
     */
    public void exceptionTypeRecord(ExceptionType exceptionType) throws EmailNotSentException {
        int eventNbr = exceptionTypeCache.getOrDefault(exceptionType, 0);
        eventNbr++;
        exceptionTypeCache.put(exceptionType, eventNbr);
        firstEventTimeCache.put(exceptionType, System.currentTimeMillis());

        long firstEventTime = firstEventTimeCache.get(exceptionType);

        if (System.currentTimeMillis() - firstEventTime > TIME_FROM_FIRST) {
            // Remove record if time has expired
            exceptionTypeCache.remove(exceptionType);
            firstEventTimeCache.remove(exceptionType);
        }

        boolean isEmailSent = isEmailSentCache.get(exceptionType) != null && isEmailSentCache.get(exceptionType);

        if (eventNbr >=MAX_NBR_OF_EXC && !isEmailSent) {
            sendEmailToAdmin(exceptionType, eventNbr);
        }
    }


    /**
     * calls SendEmailService to send a warning email to ADMINs
     * @param exceptionType that has repeated
     * @param eventNbr number of event that occured
     * @throws EmailNotSentException
     */
    private void sendEmailToAdmin(ExceptionType exceptionType, int eventNbr) throws EmailNotSentException {
            emailSenderService.sendRepetitiveExceptionWarningToAdmins(exceptionType, eventNbr);

            isEmailSentCache.put(exceptionType, true);
    }
}
