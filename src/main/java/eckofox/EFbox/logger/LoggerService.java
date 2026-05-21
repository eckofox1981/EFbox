package eckofox.EFbox.logger;

import eckofox.EFbox.exception.EFBoxErrorMessage;
import eckofox.EFbox.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.swing.text.BadLocationException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoggerService {
    private final LoggerRepository loggerRepository;

    public void saveLogg(LogMsg logMsg) {
        loggerRepository.save(logMsg);
    }

    public String retriveAllLogs(User user) throws IOException, BadLocationException {
        LogMsg logMsg = new LogMsg(
                UUID.randomUUID(),
                LoggEventType.INFO,
                LocalDateTime.now(),
                "All logs requested by " + user.getUsername() + ".",
                user
        );

        saveLogg(logMsg);

        List<LogMsg> allLogs = loggerRepository.findAll();

        return logsToHTMLCodeConverter(allLogs, user);
    }

    private String logsToHTMLCodeConverter(List<LogMsg> logs, User user) throws IOException, BadLocationException {
        StringBuilder htmlCode = new StringBuilder();
        htmlCode.append("<html><h1>EFBox Event Logs</h1><h2><i>requested by: " + user.getUsername() + "</h2>");

        for (LogMsg msg : logs) {
            htmlCode.append(logTableCreator(msg));
            htmlCode.append("<br>");
        }

        htmlCode.append("</html>");



        return htmlCode.toString();
    }

    private String logTableCreator(LogMsg log) {
        String statusCodeBox ="";
        String exceptionTypeBox ="";
        String msgColSpan = "5";

        if (log instanceof EFBoxErrorMessage errorMessage) {
            statusCodeBox = "<td colspan=\"1\""
                    + errorMessage.getCode().toString()
                    + "Type<br><i>Status-Code to user</i></td>";
            exceptionTypeBox =
                    "<td colspan=\"1\">"
                            + errorMessage.getExceptionType().toString()
                            + "Type<br><i>Type of exception</i></td>";
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
