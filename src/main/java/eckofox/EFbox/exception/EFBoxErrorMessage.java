package eckofox.EFbox.exception;

import eckofox.EFbox.logger.LogEventType;
import eckofox.EFbox.logger.LogMessage;
import eckofox.EFbox.user.User;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
