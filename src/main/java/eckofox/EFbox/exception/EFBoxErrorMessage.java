package eckofox.EFbox.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

@AllArgsConstructor
@Getter
@Setter
public class EFBoxErrorMessage {
    private ExceptionType exceptionType;
    private String timestamp;
    private HttpStatusCode code;
    private String logMessage;
}
