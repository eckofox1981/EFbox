package eckofox.EFbox.exception;

import eckofox.EFbox.logger.LoggEventType;
import eckofox.EFbox.logger.LogMsg;
import eckofox.EFbox.user.User;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class EFBoxErrorMessage extends LogMsg {
    private ExceptionType exceptionType;
    private int code;

    public EFBoxErrorMessage(
            UUID msgId,
            LoggEventType type,
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
