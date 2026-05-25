package eckofox.efbox.email;

import lombok.Getter;

@Getter
public enum EmailType {
    ADMIN_ACCESS_CODE("Admin Access Code", "Code for your admin access"),
    ADMIN_ACCESS_CODE_REQUEST("Admin Access Code Request", "Admin access Requested"),
    LOG_ACCESS_CODE("Log Access Code", "Code for your log access"),
    LOG_ACCESS_CODE_REQUEST("Log Access Code Request", "Log access Requested"),
    PASSWORD_RECOVERY("Password Recovery", "Code to renew your password"),
    SYSTEM_WARNING("System Warning", "Warning from EFBox API");


    private String type;
    private String subject;

    private EmailType(String type, String subject) {
        this.subject = subject;
        this.type = type;
    }
}
