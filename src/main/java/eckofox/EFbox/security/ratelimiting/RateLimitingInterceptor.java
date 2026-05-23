package eckofox.EFbox.security.ratelimiting;

import eckofox.EFbox.exception.EFBoxErrorMessage;
import eckofox.EFbox.logger.LogEventType;
import eckofox.EFbox.logger.LoggerService;
import eckofox.EFbox.user.User;
import eckofox.EFbox.user.UserRepository;
import eckofox.EFbox.user.UserRole;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;
    private final UserRepository userRepository;
    private final LoggerService loggerService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String JWTToken = resolveToken(request);
        String clientIP = getClientIP(request);
        String userReference;
        if (JWTToken == null) {
            userReference = clientIP;
        } else {
            userReference = JWTToken;
        }

        Bucket bucket = rateLimiterService.resolveBucket(userReference);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        long waitTimeSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.addHeader("X-Rate-Limit-Remaining", "0");
        response.addHeader("Retry-After", String.valueOf(waitTimeSeconds));
        response.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
        response.setContentType("application/json");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = new User(
                null,
                "AnonymousUser",
                "-",
                "-",
                "-",
                List.of(UserRole.ROLE_ANONYMOUS),
                List.of()
        );

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            user = userRepository.findByUsername(currentUserName)
                    .orElse(user);
        }

        EFBoxErrorMessage errorMessage = new EFBoxErrorMessage(
                UUID.randomUUID(),
                LogEventType.WARNING,
                LocalDateTime.now(),
                "Rate-limite excedded by " + user.getUsername() + " IP: " + clientIP,
                user,
                null,
                429
                );

        loggerService.saveInfoLogg(errorMessage);

        return false;
    }

    private String getClientIP(HttpServletRequest request) {
        // Check for X-Forwarded-For header (when behind a proxy)
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> "efbox-token".equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        return null;
    }
}
