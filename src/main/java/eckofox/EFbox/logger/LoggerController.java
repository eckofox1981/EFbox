package eckofox.EFbox.logger;

import eckofox.EFbox.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.text.BadLocationException;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bossmang") //this to avoid classic admin-path
public class LoggerController {
    private final LoggerService loggerService;

    /** https://www.baeldung.com/spring-mvc-return-html
     *
     * @param user
     * @return
     * @throws IOException
     * @throws BadLocationException
     */
    @GetMapping(value="/fetch-all-logs", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> getAllLogs(@AuthenticationPrincipal User user) throws IOException, BadLocationException {
        return ResponseEntity.status(201).body(loggerService.retriveAllLogs(user));
    }
}
