package eckofox.efbox.exception;

import eckofox.efbox.logger.LogEventType;
import eckofox.efbox.logger.LogMessage;
import eckofox.efbox.user.User;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Log Error Message to be recorded
 * Extends LogMessage
 */
@Entity
@Getter
@NoArgsConstructor
public class EFBoxErrorMessage extends LogMessage {
    private ExceptionType exceptionType;
    private int code;

    public EFBoxErrorMessage(
            UUID msgId,
            LogEventType type,
            LocalDateTime timestamp,
            String logMessage,
            User user,
            ExceptionType exceptionType,
            int code
    ) {
        super(msgId, type, timestamp, logMessage, user);
        this.exceptionType = exceptionType;
        this.code = code;
    }
}
