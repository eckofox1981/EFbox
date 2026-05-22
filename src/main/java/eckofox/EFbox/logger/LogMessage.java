package eckofox.EFbox.logger;

import eckofox.EFbox.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// https://www.baeldung.com/hibernate-inheritance
//Mapped super class makes it possible to share a table between two entites where one extends from the other
@Entity(name = "event_logging")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor
public class LogMessage {
    @Id
    private UUID msgId;
    private LogEventType type;
    private LocalDateTime timestamp;
    private String logMessage;
    private String username;
    private UUID userID;

    public LogMessage(
            UUID msgId,
            LogEventType type,
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
