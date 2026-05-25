package eckofox.EFbox.logger;

import eckofox.EFbox.exception.EFBoxErrorMessage;
import eckofox.EFbox.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoggerService {
    private final LoggerRepository loggerRepository;

    public void saveInfoLogg(LogMessage logMessage) {
        loggerRepository.save(logMessage);
    }

    public void saveInfoLogg(LogEventType type, String message, User user)  {
        LogMessage logMessage = new LogMessage(
                UUID.randomUUID(),
                type,
                LocalDateTime.now(),
                message,
                user
        );

        saveInfoLogg(logMessage);
    }

    public String retriveAllLogs(User user) throws IOException {
        LogMessage logMessage = new LogMessage(
                UUID.randomUUID(),
                LogEventType.INFO,
                LocalDateTime.now(),
                "All logs requested by " + user.getUsername() + ".",
                user
        );

        saveInfoLogg(logMessage);

        List<LogMessage> allLogs = loggerRepository.findAll();
        allLogs.sort(Comparator.comparing(LogMessage::getTimestamp).reversed());

        return logsToHTMLCodeConverter(allLogs, user);
    }

    private String logsToHTMLCodeConverter(List<LogMessage> logs, User user) {
        StringBuilder htmlCode = new StringBuilder();
        htmlCode.append("<html><h1>EFBox Event Logs</h1><h2><i>requested by: " + user.getUsername() + "</h2>");

        for (LogMessage msg : logs) {
            htmlCode.append(logTableCreator(msg));
            htmlCode.append("<br>");
        }

        htmlCode.append("</html>");



        return htmlCode.toString();
    }

    private String logTableCreator(LogMessage log) {
        String statusCodeBox ="";
        String exceptionTypeBox ="";
        String msgColSpan = "5";

        if (log instanceof EFBoxErrorMessage errorMessage) {
            String exceptionType = errorMessage.getExceptionType() != null ?
                    errorMessage.getExceptionType().getDescription() : "No Exception Description.";
            statusCodeBox = "<td colspan=\"1\">"
                    + HttpStatusCode.valueOf(errorMessage.getCode())
                    + "<br><i>Status-Code to user</i></td>";
            exceptionTypeBox =
                    "<td colspan=\"1\">"
                            + exceptionType
                            + "<br><i>Type of exception</i></td>";
            msgColSpan = "3";
        }

        return "<table border=\"1\" cellpadding=\"8\" cellspacing=\"0\" style=\" border-collapse: collapse; width: 100%;\">"
                + "<tr style=\"background-color:" + log.getType().getColor() + "; color:lightgrey;\">"
                + "<td><i>Timestamp</i><br><strong>" + log.getTimestamp() +  "</strong></td>"
                + "<td><i>Log ID</i><br><strong>" + log.getMsgId() + "</strong></td>"
                + "<td><i>Type of event</i><br><strong>" + log.getType().getDescription() + "</strong></td>"
                + "<td>"
                    + "<div style=\"display:flex; justify-content:space-between; align-items:flex-start;\">"
                    + "<i style=\"margin-left:0;\">User logged-in:</i>"
                    + "<div style=\"text-align:right;\">"
                    + "<strong style=\"display:block; margin-right:0;\">" + log.getUsername() + "</strong>"
                    + "<strong style=\"display:block; margin-right:0;\">" + log.getUserID() + "</strong>"
                    + "</div>"

                + "</div>"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + statusCodeBox
                + exceptionTypeBox
                + "<td colspan=\""+ msgColSpan +"\">" + log.getLogMessage() + "</td>"
                + "</tr>"
                + "</table>";
    }
}
