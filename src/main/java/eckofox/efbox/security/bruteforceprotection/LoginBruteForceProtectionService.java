package eckofox.efbox.security.bruteforceprotection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * caches the number of attempted login on an account
 * when the triggered (based number of attempts during a defined number of minutes) and Email is sent
 * to the concerned user and the ADMINs. The account will be blocked for a defined amount of time.
 *
 * based on https://www.geeksforgeeks.org/advance-java/prevent-brute-force-authentication-attempts-with-spring-security/
 */
@Service
@RequiredArgsConstructor
public class LoginBruteForceProtectionService {
    private static final int MAX_ATTEMPT = 5;
    private static final long LOCK_TIME = TimeUnit.MINUTES.toMillis(15);

    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lockCache = new ConcurrentHashMap<>();

    /**
     * when logging succeeds the cache resets
     * @param key of cached value
     */
    public void loginSucceeded(String key) {
        attemptsCache.remove(key); // Clear failed attempts on successful login
        lockCache.remove(key); // Unlock user on successful login
    }

    /**
     * records a failed login attempt in the cache
     * @param key
     */
    public void loginFailed(String key) {
        int attempts = attemptsCache.getOrDefault(key, 0);
        attempts++;
        attemptsCache.put(key, attempts);
        if (attempts >= MAX_ATTEMPT) {
            lockCache.put(key, System.currentTimeMillis()); // Lock user if max attempts exceeded
        }
    }

    /**
     * checks if account is blocked
     * @param key of cache
     * @return boolean based on if account is blocked or not
     */
    public boolean isBlocked(String key) {
        if (!lockCache.containsKey(key)) {
            return false;
        }

        long lockTime = lockCache.get(key);
        if (System.currentTimeMillis() - lockTime > LOCK_TIME) {
            lockCache.remove(key); // Remove lock if lock time has expired
            return false;
        }

        return true; // User is still locked
    }
}
