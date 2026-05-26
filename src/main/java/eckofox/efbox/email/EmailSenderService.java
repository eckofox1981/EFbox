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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailSenderService {
    private final JavaMailSender mailSender;
    private final LoggerService loggerService;
    private final UserRepository userRepository;

    private final String hello = "Hello,";
    private final String signature = "\n\nCordially,\n\nThe EFBox team";

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
        message.append(hello)
                .append(user.getUsername())
                .append(",\n")
                .append("You have requested a code to renew your password.\n")
                .append("Enter the code: ").append(code).append(" with your new password to renew it.")
                .append(signature);

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
            String details = e.getMessage() == null
                    ? "No message."
                    : e.getMessage();
            throw new EmailNotSentException(
                    "Email not sent to "
                            + user.getUsername()
                            + ".\n Details:\n"
                            + details
            );
        }
    }

    public void sendRepetitiveLoginAttemptsEMail(EmailType type, User user, LocalDateTime now, String ip)
            throws EmailNotSentException {
        StringBuilder message = new StringBuilder();
        message.append(hello)
                .append(user.getUsername())
                .append(",\n")
                .append("Multiple unsuccessful attempts to login into your account have been recorded.\n")
                .append("If you didn't try to log in at " + now.toLocalTime()  + " on " + now.toLocalDate())
                .append(", we advise you to consider changing password.")
                .append(signature);

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
            String details = e.getMessage() == null
                    ? "No message."
                    : e.getMessage();
            throw new EmailNotSentException(
                    "Email not sent to "
                            + user.getUsername()
                            + ".\n Details:\n"
                            + details
            );
        }

        warningEmailToAdmins(user, now, ip);
    }

    public void warningEmailToAdmins(User user, LocalDateTime now, String ip) throws EmailNotSentException {
        StringBuilder message = new StringBuilder();
        message.append(hello)
                .append(user.getUsername())
                .append(",\n")
                .append("Multiple unsuccessful attempts to login into ")
                .append(user.getUsername())
                .append("'s account at " + now.toLocalTime() + " from IP: " + ip + ".\nPlease check the system logs.")
                .append("\n\nEFBOX SYSTEM");

        //better to use findByRole with @Query in repository (out of scope), but this works for demonstration purposes
        List<User> admins =
                userRepository.findAll().stream().filter(a -> a.getRoles().contains(UserRole.ROLE_ADMIN)).toList();

        if (admins.isEmpty()) {
            System.out.println("empty list");
            throw new EmailNotSentException("List of admins was empty.");
        }

        for (User admin : admins) {
            System.out.println(admin.getUsername());
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
                e.printStackTrace();
                String details = e.getMessage() == null
                        ? "No message."
                        : e.getMessage();
                throw new EmailNotSentException(
                        "Email not sent to "
                                + admin.getUsername()
                                + ".\n Details:\n"
                                + details
                );
            }
        }
    }




}
