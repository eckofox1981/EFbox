package eckofox.efbox.email;

import eckofox.efbox.exception.EmailNotSentException;
import eckofox.efbox.exception.ExceptionType;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailSenderService {
    private final JavaMailSender mailSender;
    private final LoggerService loggerService;
    private final UserRepository userRepository;

    private static final String hello = "Hello,";
    private static final String signature = "\n\nCordially,\n\nThe EFBox team";
    private static final String noMessage = "No message.";

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
                .append("Enter the code: ")
                .append(code)
                .append(" with your new password to renew it.")
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
                    ? noMessage
                    : e.getMessage();
            throw new EmailNotSentException(
                    "Email not sent to "
                            + user.getUsername()
                            + ".\n Details:\n"
                            + details
            );
        }
    }

    public void sendRepetitiveLoginAttemptsEMailToUser(EmailType type, User user, LocalDateTime now, String ip)
            throws EmailNotSentException {
        StringBuilder message = new StringBuilder();
        message.append(hello)
                .append("Dear administrator of the EFBox API")
                .append(",\n")
                .append("Multiple unsuccessful attempts to login into your account have been recorded.\n")
                .append("If you didn't try to log in at ")
                .append(now.toLocalTime())
                .append(" on ")
                .append(now.toLocalDate())
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
                    ? noMessage
                    : e.getMessage();
            throw new EmailNotSentException(
                    "Email not sent to "
                            + user.getUsername()
                            + ".\n Details:\n"
                            + details
            );
        } finally {
            sendRepetitiveLoginAttemptsEMailToAdmins(user, now, ip);
        }


    }

    public void sendRepetitiveLoginAttemptsEMailToAdmins(User user, LocalDateTime now, String ip)
            throws EmailNotSentException {
        StringBuilder message = new StringBuilder();
        message.append(hello)
                .append("Dear administrator of the EFBox API")
                .append(",\n")
                .append("Multiple unsuccessful attempts to login into ")
                .append(user.getUsername())
                .append("'s account at ")
                .append(now.toLocalTime())
                .append(" from IP: ")
                .append(ip)
                .append(".\nPlease check the system logs.")
                .append("\n\nEFBOX SYSTEM");

        //better to use findByRole with @Query in repository (out of scope), but this works for demonstration purposes
        List<User> admins =
                userRepository.findAll().stream().filter(a -> a.getRoles().contains(UserRole.ROLE_ADMIN)).toList();

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
                        "Warning email, brute force login attempts sent to:" + user.getUsername() + ".",
                        user
                ));
            } catch (Exception e) {
                String details = e.getMessage() == null
                        ? noMessage
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

    public void sendRepetitiveExceptionWarningToAdmins(ExceptionType exceptionType, int eventNbr)
            throws EmailNotSentException {
        StringBuilder message = new StringBuilder();
        message.append(hello)
                .append("Dear administrator of the EFBox API,\n")
                .append("repetitive ")
                .append(exceptionType.toString())
                .append(" have been recorded (")
                .append(eventNbr)
                .append(" times) under a 5 minutes period.")
                .append("\n\n")
                .append("Please promptly check the logs to confirm non brute force attack is ongoing.")
                .append("\n\nEFBOX SYSTEM");

        //better to use findByRole with @Query in repository (out of scope), but this works for demonstration purposes
        List<User> admins =
                userRepository.findAll().stream().filter(a -> a.getRoles().contains(UserRole.ROLE_ADMIN)).toList();

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
                        "Warning email reguarding repetitive exception sent to info sent to:"
                                + admin.getUsername() + ".",
                        admin
                ));
            } catch (Exception e) {
                String details = e.getMessage() == null
                        ? noMessage
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

    public void sendAdminStatusRequest(User user) throws EmailNotSentException {
        //better with sql query but out of scope for project (quicker to write)
        List<User> allUsers = userRepository.findAll();
        List<User> allOwners = allUsers.stream().filter(a -> a.getRoles().contains(UserRole.ROLE_OWNER)).toList();

        StringBuilder message = new StringBuilder();
        message.append(hello)
                .append("Dear owner of the EFBox API,\n")
                .append("user ")
                .append(user.getUsername())
                .append(" with id:\n")
                .append(user.getUserID())
                .append("\n has request admin status.")
                .append("\n\nEFBOX SYSTEM");

        try {
            allOwners
                    .forEach(a ->
                            sendEmail(a.getEmail(), EmailType.ADMIN_ACCESS_CODE_REQUEST.getSubject(), message.toString()
                            ));
        } catch (Exception e) {
            String details = e.getMessage() == null
                    ? noMessage
                    : e.getMessage();
            throw new EmailNotSentException(
                    "Email not sent to "
                            + user.getUsername()
                            + ".\n Details:\n"
                            + details
            );
        }
    }

    public void sendLogAccessRequest(User user) throws EmailNotSentException {
        //better with sql query but out of scope for project (quicker to write)
        List<User> allUsers = userRepository.findAll();
        List<User> allAdmins = allUsers.stream().filter(a -> a.getRoles().contains(UserRole.ROLE_ADMIN)).toList();
        System.out.println(allAdmins.getFirst().getEmail());

        StringBuilder message = new StringBuilder();
        message.append(hello)
                .append("Dear owner of the EFBox API,\n")
                .append("user ")
                .append(user.getUsername())
                .append(" with id:\n")
                .append(user.getUserID())
                .append("\n has request access to the logs.")
                .append("\n\nEFBOX SYSTEM");

        try {
            allAdmins
                    .forEach(a ->
                            sendEmail(a.getEmail(), EmailType.ADMIN_ACCESS_CODE_REQUEST.getSubject(), message.toString()
                            ));
        } catch (Exception e) {
            String details = e.getMessage() == null
                    ? noMessage
                    : e.getMessage();
            throw new EmailNotSentException(
                    "Email not sent to "
                            + user.getUsername()
                            + ".\n Details:\n"
                            + details
            );
        }
    }

}
