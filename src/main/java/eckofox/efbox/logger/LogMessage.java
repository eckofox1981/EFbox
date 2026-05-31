package eckofox.efbox.logger;

import eckofox.efbox.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;



/**
 * Object used to log various events on the server
 * LogMessage inherited by EFBoxErrorMessage for Exception logging
 * inspired by: https://www.baeldung.com/hibernate-inheritance
 */
@Entity(name = "event_logging")
//Mapped super class makes it possible to share a table between two entites where one extends from the other
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor
public class LogMessage {
    @Id
    private UUID msgId;
    private LogEventType type;
    private LocalDateTime timestamp;
    private String message;
    private String username;
    private UUID userID;

    public LogMessage(
            UUID msgId,
            LogEventType type,
            LocalDateTime timestamp,
            String message,
            User user) {
        this.msgId = msgId;
        this.type = type;
        this.timestamp = timestamp;
        this.message = message;
        this.username = user.getUsername();
        this.userID = user.getUserID();
    }
}
