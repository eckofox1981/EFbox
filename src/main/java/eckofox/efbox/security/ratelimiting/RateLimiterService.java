package eckofox.efbox.security.ratelimiting;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * basic rate limiter using Bucket4j
 * refs:
 * https://oneuptime.com/blog/post/2026-01-25-rate-limiting-bucket4j-spring-boot/view (!: deprecated methods included)
 * https://www.baeldung.com/spring-bucket4j
 * see Bucket.java and bandwidth.java
 */

@Service
public class RateLimiterService {

    // Store buckets per client identifier (IP, API key, user ID)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Create a bucket with the default rate limit configuration
     */
    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(10)
                                .refillGreedy(10, Duration.ofSeconds(30))
                                .build()
                ).build();

    }

    /**
     * Get or create a bucket for the given key
     * */
    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * * Check if a request should be allowed
     */
    public boolean tryConsume(String key) {
        Bucket bucket = resolveBucket(key);
        return bucket.tryConsume(1);
    }

    /**
     * Get remaining tokens for the client
     * @param key
     * @return
     */
    public long getAvailableTokens(String key) {
        return resolveBucket(key).getAvailableTokens();
    }
}
