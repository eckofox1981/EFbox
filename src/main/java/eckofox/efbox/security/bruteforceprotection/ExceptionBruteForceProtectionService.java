package eckofox.efbox.security.bruteforceprotection;

import eckofox.efbox.email.EmailSenderService;
import eckofox.efbox.exception.EmailNotSentException;
import eckofox.efbox.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

//based upon
//https://www.geeksforgeeks.org/advance-java/prevent-brute-force-authentication-attempts-with-spring-security/
@Service
@RequiredArgsConstructor
public class ExceptionBruteForceProtectionService {
    private final EmailSenderService emailSenderService;

    private static final int MAX_NBR_OF_EXC = 5;
    private static final long TIME_FROM_FIRST = TimeUnit.MINUTES.toMillis(5);

    private final Map<ExceptionType, Integer> exceptionTypeCache = new ConcurrentHashMap<>();
    private final Map<ExceptionType, Long> firstEventTimeCache = new ConcurrentHashMap<>();
    private final Map<ExceptionType, Boolean> isEmailSentCache = new ConcurrentHashMap<>();

    public void logAccessedExceptionCacheReset() {
        exceptionTypeCache.clear();
        isEmailSentCache.clear();
    }

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

        boolean isEmailSent = isEmailSentCache.get(exceptionType) == null ? false : isEmailSentCache.get(exceptionType);

        if (eventNbr >=MAX_NBR_OF_EXC && !isEmailSent) {
            sendEmailToAdmin(exceptionType, eventNbr);
        }
    }

    private void sendEmailToAdmin(ExceptionType exceptionType, int eventNbr) throws EmailNotSentException {
            emailSenderService.sendRepetitiveExceptionWarningToAdmins(exceptionType, eventNbr);

            isEmailSentCache.put(exceptionType, true);
    }
}
