package eckofox.EFbox.logger;

import eckofox.EFbox.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "event_logging")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor
public class LogMsg {
    @Id
    private UUID msgId;
    private LoggEventType type;
    private LocalDateTime timestamp;
    private String logMessage;
    private String username;
    private UUID userID;

    public LogMsg(
            UUID msgId,
            LoggEventType type,
            LocalDateTime timestamp,
            String logMessage,
            User user) {
        this.msgId = msgId;
        this.type = type;
        this.timestamp = timestamp;
        this.logMessage = logMessage;
        this.username = user.getUsername();
        this.userID = user.getUserID();
    }
}
