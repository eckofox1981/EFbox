package eckofox.efbox.logger;

import eckofox.efbox.exception.EFBoxErrorMessage;
import eckofox.efbox.user.User;
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

    /**
     * saves LogMessage and EFBoxErrorMessage
     * @param logMessage LogMessage or EFBoxErrorMessage
     */
    public void saveInfoLogg(LogMessage logMessage) {
        loggerRepository.save(logMessage);
    }


    /**
     * creates and saves LogMessage
     * @param type LogEventType
     * @param message content of message
     * @param user User - concerned
     */
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

    /**
     * fetches all logs without descrimination
     * @param user User making the request
     * @return HTML in String
     * @throws IOException
     */
    public String retrieveAllLogs(User user) throws IOException {
        saveInfoLogg(LogEventType.INFO_ADMIN, "Exception cache erased for upon log being accessed.", user);

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

    /**
     * converts log data from database to readable HTML
     * @param logs LogMessage & EFBoxErrorMessage
     * @param user User making the request, will be displayed on top
     * @return HTML in String
     */
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

    /**
     * creates the HTML table to display ergonomically the event-logs
     * @param log LogMessage and/or EFBoxErrorMessage
     * @return HTML-table in String
     */
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
                + "<td colspan=\""+ msgColSpan +"\">" + log.getMessage() + "</td>"
                + "</tr>"
                + "</table>";
    }
}
