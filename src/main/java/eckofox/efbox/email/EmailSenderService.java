package eckofox.efbox.email;

import eckofox.efbox.exception.EmailNotSentException;
import eckofox.efbox.logger.LogEventType;
import eckofox.efbox.logger.LogMessage;
import eckofox.efbox.logger.LoggerService;
import eckofox.efbox.user.User;
import eckofox.efbox.user.UserRepository;
import eckofox.efbox.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailSenderService {
    private final JavaMailSender mailSender;
    private final LoggerService loggerService;
    private final UserRepository userRepository;

    private String hello = "Hello,";
    private String signature = "\n\nCordially,\n\nThe EFBox team";

    public String sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(System.getenv("EFBOX_MAIL_USERNAME"));
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        return "Email sent.";
    }

    public String sendPasswordRecoveryEmail(EmailType type, User user, int code) throws EmailNotSentException {
        StringBuilder message = new StringBuilder();
        message.append(hello);
        message.append(user.getUsername());
        message.append(",\n");
        message.append("You have requested a code to renew your password.\n");
        message.append("Enter the code: " + code + " with your new password to renew it.");
        message.append(signature);

        try {
            sendEmail(user.getEmail(), type.getSubject(), message.toString());

            loggerService.saveInfoLogg(new LogMessage(
                    UUID.randomUUID(),
                    LogEventType.INFO,
                    LocalDateTime.now(),
                    "Password recovery code sent to:" + user.getUsername() + ".",
                    user
            ));
            return "Email sent to your inbox.";
        } catch (Exception e) {
            throw new EmailNotSentException(
                    "Email not sent to "
                    + user.getUsername()
                    + ".\n Details:\n"
                    + e.getMessage());
        }
    }

    public void sendRepetitiveLoginAttemptsMail(EmailType type, User user, LocalDateTime now) throws EmailNotSentException {
        StringBuilder message = new StringBuilder();
        message.append(hello);
        message.append(user.getUsername());
        message.append(",\n");
        message.append("Multiple unsuccessful attempts to login in your account have been recorded.\n");
        message.append("If you didn't try to log in at " + now.toLocalDate() + ", we advise you to change password.");
        message.append(signature);

        try {
        sendEmail(user.getEmail(), type.getSubject(), message.toString());

        loggerService.saveInfoLogg(new LogMessage(
                UUID.randomUUID(),
                LogEventType.INFO_USER,
                LocalDateTime.now(),
                "Repetitive login info sent to:" + user.getUsername() + ".",
                user
        ));
        } catch (Exception e) {
            throw new EmailNotSentException(
                    "Email not sent to "
                            + user.getUsername()
                            + ".\n Details:\n"
                            + e.getMessage());
        }

        warningEmailToAdmin(user, now);
    }

    public void warningEmailToAdmin(User user, LocalDateTime now) throws EmailNotSentException {
        StringBuilder message = new StringBuilder();
        message.append(hello);
        message.append(user.getUsername());
        message.append(",\n");
        message.append("Multiple unsuccessful attempts to login into "+ user.getUsername());
        message.append("'s account at " + now.toLocalTime() + ". Please check the system logs.");
        message.append("\n\nEFBOX SYSTEM");

        List<User> admins = userRepository.findByRole(UserRole.ROLE_ADMIN).orElse(Collections.emptyList());

        if (admins.isEmpty()) {
            throw new EmailNotSentException("List of admins was empty.");
        }

        for (User admin : admins) {
            try {
                sendEmail(admin.getEmail(), EmailType.SYSTEM_WARNING.getSubject(), message.toString());
                loggerService.saveInfoLogg(new LogMessage(
                        UUID.randomUUID(),
                        LogEventType.INFO_ADMIN,
                        LocalDateTime.now(),
                        "Warning email, brute force login attempts sent to info sent to:" + user.getUsername() + ".",
                        user
                ));
            } catch (Exception e) {
                throw new EmailNotSentException(
                        "Email not sent to "
                                + admin.getUsername()
                                + ".\n Details:\n"
                                + e.getMessage());
            }
        }
    }




}
