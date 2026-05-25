package eckofox.efbox.email;

import eckofox.efbox.logger.LogEventType;
import eckofox.efbox.logger.LogMessage;
import eckofox.efbox.logger.LoggerService;
import eckofox.efbox.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailSenderService {
    private final JavaMailSender mailSender;
    private final LoggerService loggerService;

    public String sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(System.getenv("EFBOX_MAIL_USERNAME"));
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        return "Email sent.";
    }

    public String sendPasswordRecoveryEmail(EmailType type, User user, int code) {
        StringBuilder message = new StringBuilder();
        message.append("Hello ");
        message.append(user.getUsername());
        message.append(",\n");
        message.append("You have requested a code to renew your password.\n");
        message.append("Enter the code: " + code + " with your new password to renew it.");
        message.append("\n\nCordially,\n\nThe EFBox team");

        sendEmail(user.getEmail(), type.getSubject(), message.toString());

        loggerService.saveInfoLogg(new LogMessage(
                UUID.randomUUID(),
                LogEventType.INFO,
                LocalDateTime.now(),
                "Password recovery code sent to:" + user.getUsername() + ".",
                user
        ));
        return "Email sent to your inbox.";
    }


}
