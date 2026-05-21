package eckofox.EFbox.logger;

import lombok.Getter;

@Getter
public enum LoggEventType {
    FATAL("FATAL", "#AB1B00"),
    WARNING("WARNING", "#FF8000"),
    ERROR("ERROR","#B600E8"),
    INFO("INFO", "#0068E8");

    private String description;
    private String color;

    private LoggEventType (String description, String color) {
        this.description = description;
        this.color = color;
    }

}
