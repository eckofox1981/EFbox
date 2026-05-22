package eckofox.EFbox.logger;

import lombok.Getter;

@Getter
public enum LogEventType {
    ERROR("ERROR","#B600E8"),
    FATAL("FATAL", "#AB1B00"),
    INFO("INFO", "#0068E8"),
    INFO_ADMIN("INFO ADMIN", "#006E11"),
    INFO_FILE("INFO FILE", "#8B00E8"),
    INFO_FOLDER("INFO FOLDER", "#5900E8"),
    INFO_USER("INFO USER", "#000CE8"),
    WARNING("WARNING", "#FF8000");

    private String description;
    private String color;

    private LogEventType(String description, String color) {
        this.description = description;
        this.color = color;
    }

}
